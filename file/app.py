from flask import Flask, request, render_template, jsonify
from werkzeug.utils import secure_filename
import os

# +++ added imports +++
import re
import unicodedata
import uuid
from urllib.parse import unquote

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 30 * 1024 * 1024 * 1024
app.config['UPLOAD_FOLDER'] = os.path.join(app.root_path, 'uploads')
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

# --- filename helpers ---
_WINDOWS_RESERVED_NAMES = {
    'CON', 'PRN', 'AUX', 'NUL',
    *(f'COM{i}' for i in range(1, 10)),
    *(f'LPT{i}' for i in range(1, 10)),
}


def _extract_filename_from_content_disposition(content_disposition: str | None) -> str | None:
    """Best-effort filename extraction supporting RFC5987 filename*."""
    if not content_disposition:
        return None

    # RFC5987: filename*=UTF-8''%E4%B8%AD%E6%96%87.txt
    m_star = re.search(r"filename\*=(?P<value>[^;]+)", content_disposition, flags=re.IGNORECASE)
    if m_star:
        value = m_star.group('value').strip().strip('"')
        # expected: <charset>''<urlencoded>
        parts = value.split("''", 1)
        if len(parts) == 2:
            charset, encoded = parts
            try:
                raw = unquote(encoded)
                # unquote returns str; to honor charset, re-encode as latin-1 bytes when possible
                # but in practice UTF-8 is most common; fall back safely.
                if charset.lower() == 'utf-8':
                    return raw
                return raw.encode('latin-1', 'backslashreplace').decode(charset, 'ignore')
            except Exception:
                pass

    # Plain filename="..."
    m = re.search(r"filename=(?P<value>[^;]+)", content_disposition, flags=re.IGNORECASE)
    if m:
        return m.group('value').strip().strip('"')

    return None


def get_client_filename(file_storage) -> str:
    """Try to get the best client-provided filename."""
    cd = getattr(file_storage, 'content_disposition', None)
    extracted = _extract_filename_from_content_disposition(cd)
    return extracted or file_storage.filename or ''


def sanitize_filename_keep_unicode(name: str, *, max_length: int = 200) -> str:
    """Sanitize filename for Windows/Linux while keeping unicode characters.

    - removes path components
    - strips control chars
    - replaces characters invalid on Windows
    - avoids Windows reserved device names
    - trims trailing spaces/dots
    """
    if not name:
        return ''

    # Some browsers may send full paths (e.g. C:\fakepath\a.txt)
    name = name.replace('\\', '/')
    name = name.split('/')[-1]

    name = unicodedata.normalize('NFC', name).strip()
    # Remove nulls and other control chars
    name = ''.join(ch for ch in name if ch not in {'\x00'} and (ord(ch) >= 32 or ch in {'\t'}))

    # Windows forbidden: <>:"/\|?* plus control chars. We already normalized slashes above.
    name = re.sub(r'[<>:"/\\|?*]', '_', name)

    # Collapse whitespace and underscores a bit
    name = re.sub(r'\s+', ' ', name).strip()
    name = re.sub(r'_+', '_', name)

    # Trim trailing spaces and dots (Windows)
    name = name.rstrip(' .')

    # Prevent empty / dot names
    if name in {'', '.', '..'}:
        return ''

    # Split base/ext to check reserved names
    base, ext = os.path.splitext(name)
    if base.upper() in _WINDOWS_RESERVED_NAMES:
        base = f'_{base}'

    # Enforce length limit while keeping extension
    if len(base) + len(ext) > max_length:
        keep = max(1, max_length - len(ext))
        base = base[:keep]

    name = (base + ext).rstrip(' .')
    return name


def resolve_name_conflict(folder: str, filename: str) -> str:
    """Avoid overwriting. Uses a short random suffix when needed."""
    candidate = filename
    base, ext = os.path.splitext(filename)
    while os.path.exists(os.path.join(folder, candidate)):
        suffix = uuid.uuid4().hex[:8]
        candidate = f"{base}_{suffix}{ext}"
    return candidate

@app.route('/')
def upload_form():
    return render_template('submit.html')

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'files' not in request.files:
        return jsonify({'error': 'No file part'}), 400

    files = [f for f in request.files.getlist('files') if f]
    if not files:
        return jsonify({'error': 'No files attached'}), 400

    results = []
    for uploaded in files:
        original_filename = get_client_filename(uploaded)
        if not original_filename:
            results.append({'filename': '', 'status': 'failed', 'message': 'Empty filename'})
            continue

        safe_name = sanitize_filename_keep_unicode(original_filename)
        if not safe_name:
            # fallback to werkzeug behavior; if still empty, use uuid
            safe_name = secure_filename(original_filename) or f"upload_{uuid.uuid4().hex}"

        safe_name = resolve_name_conflict(app.config['UPLOAD_FOLDER'], safe_name)

        target_path = os.path.join(app.config['UPLOAD_FOLDER'], safe_name)
        try:
            uploaded.save(target_path)
            results.append({
                'original_filename': original_filename,
                'saved_filename': safe_name,
                'status': 'success',
                'message': 'Uploaded successfully',
            })
        except Exception as exc:  # pragma: no cover - defensive path
            results.append({
                'original_filename': original_filename,
                'saved_filename': safe_name,
                'status': 'failed',
                'message': str(exc),
            })

    status_code = 200 if any(item['status'] == 'success' for item in results) else 400
    return jsonify({'results': results}), status_code

if __name__ == '__main__':
    app.run(host='::', port=80, debug=True)