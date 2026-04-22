from flask import Flask, request, render_template, jsonify, abort, send_from_directory
from werkzeug.utils import secure_filename
import os
import re
import json
import unicodedata
import uuid
import shutil
from urllib.parse import unquote
from datetime import datetime
from pathlib import Path

app = Flask(__name__)

# ==================== Configuration ====================
app.config['MAX_CONTENT_LENGTH'] = 30 * 1024 * 1024 * 1024  # 30GB (legacy endpoint)
app.config['UPLOAD_FOLDER'] = os.path.join(app.root_path, 'uploads')
app.config['TEMP_FOLDER'] = os.path.join(app.root_path, 'temp_uploads')
app.config['CHUNK_SIZE'] = 5 * 1024 * 1024  # 5MB default chunk size

os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
os.makedirs(app.config['TEMP_FOLDER'], exist_ok=True)

# Clean up orphaned temp uploads from previous sessions on startup
for entry in os.listdir(app.config['TEMP_FOLDER']):
    path = os.path.join(app.config['TEMP_FOLDER'], entry)
    if os.path.isdir(path):
        shutil.rmtree(path, ignore_errors=True)

# In-memory upload sessions (use Redis/DB for production persistence across restarts)
upload_sessions: dict = {}

# ==================== Filename Helpers ====================
_WINDOWS_RESERVED_NAMES = {
    'CON', 'PRN', 'AUX', 'NUL',
    *(f'COM{i}' for i in range(1, 10)),
    *(f'LPT{i}' for i in range(1, 10)),
}


def _extract_filename_from_content_disposition(content_disposition: str | None) -> str | None:
    """Best-effort filename extraction supporting RFC5987 filename*."""
    if not content_disposition:
        return None

    m_star = re.search(r"filename\*=(?P<value>[^;]+)", content_disposition, flags=re.IGNORECASE)
    if m_star:
        value = m_star.group('value').strip().strip('"')
        parts = value.split("''", 1)
        if len(parts) == 2:
            charset, encoded = parts
            try:
                raw = unquote(encoded)
                if charset.lower() == 'utf-8':
                    return raw
                return raw.encode('latin-1', 'backslashreplace').decode(charset, 'ignore')
            except Exception:
                pass

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
    """Sanitize filename for Windows/Linux while keeping unicode characters."""
    if not name:
        return ''

    name = name.replace('\\', '/')
    name = name.split('/')[-1]

    name = unicodedata.normalize('NFC', name).strip()
    name = ''.join(ch for ch in name if ch not in {'\x00'} and (ord(ch) >= 32 or ch in {'\t'}))

    name = re.sub(r'[<>:"/\\|?*]', '_', name)

    name = re.sub(r'\s+', ' ', name).strip()
    name = re.sub(r'_+', '_', name)

    name = name.rstrip(' .')

    if name in {'', '.', '..'}:
        return ''

    base, ext = os.path.splitext(name)
    if base.upper() in _WINDOWS_RESERVED_NAMES:
        base = f'_{base}'

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


def _safe_upload_relpath(filename: str) -> str | None:
    """Validate and normalize a relpath inside UPLOAD_FOLDER.

    Allows nested paths, blocks absolute paths and traversal.
    Returns a POSIX-style relpath or None if invalid.
    """
    if not filename:
        return None

    try:
        filename = unquote(filename)
    except Exception:
        pass

    filename = filename.replace('\\', '/')

    if filename.startswith('/'):
        return None

    p = Path(filename)
    if p.is_absolute():
        return None

    parts: list[str] = []
    for part in p.parts:
        if part in {'', '.'}:
            continue
        if part == '..':
            return None
        parts.append(part)

    rel = '/'.join(parts)
    if not rel:
        return None

    root = os.path.realpath(app.config['UPLOAD_FOLDER'])
    full = os.path.realpath(os.path.join(root, rel))
    if not (full == root or full.startswith(root + os.sep)):
        return None

    return rel


def format_file_size(size_bytes: int) -> str:
    """Human-readable file size."""
    if size_bytes <= 0:
        return '0 B'
    units = ['B', 'KB', 'MB', 'GB', 'TB']
    k = 1024
    i = 0
    while size_bytes >= k and i < len(units) - 1:
        size_bytes /= k
        i += 1
    return f'{size_bytes:.2f} {units[i]}'


# ==================== Routes: Upload Form ====================
@app.route('/')
def upload_form():
    return render_template('submit.html')


# ==================== Routes: Legacy Single Upload ====================
@app.route('/upload', methods=['POST'])
def upload_file():
    """Legacy single-request upload endpoint. Kept for backward compatibility.
    For large files, prefer the chunked upload endpoints instead."""
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
        except Exception as exc:
            results.append({
                'original_filename': original_filename,
                'saved_filename': safe_name,
                'status': 'failed',
                'message': str(exc),
            })

    status_code = 200 if any(item['status'] == 'success' for item in results) else 400
    return jsonify({'results': results}), status_code


# ==================== Routes: Chunked Upload ====================
@app.route('/upload/init', methods=['POST'])
def upload_init():
    """Initialize a chunked upload session.

    JSON body: { "filename": str, "total_size": int }
    Returns: { "session_id", "chunk_size", "total_chunks", "final_filename" }
    """
    data = request.get_json(silent=True) or {}
    filename = data.get('filename', '')
    total_size = data.get('total_size', 0)

    if not filename or total_size <= 0:
        return jsonify({'error': 'Missing filename or total_size'}), 400

    safe_name = sanitize_filename_keep_unicode(filename)
    if not safe_name:
        safe_name = secure_filename(filename) or f"upload_{uuid.uuid4().hex}"

    final_name = resolve_name_conflict(app.config['UPLOAD_FOLDER'], safe_name)

    session_id = uuid.uuid4().hex
    chunks_dir = os.path.join(app.config['TEMP_FOLDER'], session_id)
    os.makedirs(chunks_dir, exist_ok=True)

    chunk_size = app.config['CHUNK_SIZE']
    total_chunks = max(1, (total_size + chunk_size - 1) // chunk_size)

    session_info = {
        'session_id': session_id,
        'original_filename': filename,
        'final_filename': final_name,
        'total_size': total_size,
        'chunk_size': chunk_size,
        'total_chunks': total_chunks,
        'received_chunks': set(),
        'chunks_dir': chunks_dir,
        'created_at': datetime.now().isoformat(),
    }

    # Persist session metadata to disk for crash recovery
    meta_path = os.path.join(chunks_dir, '_meta.json')
    with open(meta_path, 'w', encoding='utf-8') as f:
        # Convert set to list for JSON serialization
        saveable = dict(session_info)
        saveable['received_chunks'] = list(saveable['received_chunks'])
        json.dump(saveable, f, ensure_ascii=False)

    upload_sessions[session_id] = session_info

    return jsonify({
        'session_id': session_id,
        'chunk_size': chunk_size,
        'total_chunks': total_chunks,
        'final_filename': final_name,
    })


@app.route('/upload/chunk', methods=['POST'])
def upload_chunk():
    """Upload a single chunk for a session.

    Form data: session_id, chunk_index, chunk (file)
    Returns: { "session_id", "chunk_index", "received", "total_chunks" }
    """
    session_id = request.form.get('session_id', '')
    chunk_index_str = request.form.get('chunk_index', '-1')

    try:
        chunk_index = int(chunk_index_str)
    except ValueError:
        return jsonify({'error': 'Invalid chunk_index'}), 400

    session = upload_sessions.get(session_id)
    if not session:
        return jsonify({'error': 'Invalid or expired session'}), 404

    if chunk_index < 0 or chunk_index >= session['total_chunks']:
        return jsonify({'error': f'Chunk index {chunk_index} out of range [0, {session["total_chunks"]})'}), 400

    chunk_file = request.files.get('chunk')
    if not chunk_file:
        return jsonify({'error': 'No chunk data'}), 400

    chunk_path = os.path.join(session['chunks_dir'], str(chunk_index))
    chunk_file.save(chunk_path)

    session['received_chunks'].add(chunk_index)

    # Update persisted metadata periodically (every 10 chunks or on last chunk)
    if len(session['received_chunks']) % 10 == 0 or len(session['received_chunks']) == session['total_chunks']:
        meta_path = os.path.join(session['chunks_dir'], '_meta.json')
        try:
            saveable = dict(session)
            saveable['received_chunks'] = list(saveable['received_chunks'])
            with open(meta_path, 'w', encoding='utf-8') as f:
                json.dump(saveable, f, ensure_ascii=False)
        except Exception:
            pass  # Non-critical; in-memory state is authoritative

    return jsonify({
        'session_id': session_id,
        'chunk_index': chunk_index,
        'received': len(session['received_chunks']),
        'total_chunks': session['total_chunks'],
    })


@app.route('/upload/status/<session_id>')
def upload_status(session_id):
    """Check upload session progress. Used for resume after interruption."""
    session = upload_sessions.get(session_id)
    if not session:
        # Try to recover from persisted metadata
        meta_path = os.path.join(app.config['TEMP_FOLDER'], session_id, '_meta.json')
        if os.path.isfile(meta_path):
            try:
                with open(meta_path, 'r', encoding='utf-8') as f:
                    saved = json.load(f)
                saved['received_chunks'] = set(saved['received_chunks'])
                upload_sessions[session_id] = saved
                session = saved
            except Exception:
                return jsonify({'error': 'Session not recoverable'}), 404
        else:
            return jsonify({'error': 'Invalid session'}), 404

    return jsonify({
        'session_id': session_id,
        'final_filename': session['final_filename'],
        'original_filename': session['original_filename'],
        'total_size': session['total_size'],
        'chunk_size': session['chunk_size'],
        'total_chunks': session['total_chunks'],
        'received_chunks': sorted(session['received_chunks']),
        'progress': round(len(session['received_chunks']) / session['total_chunks'] * 100, 2),
    })


@app.route('/upload/complete', methods=['POST'])
def upload_complete():
    """Finalize a chunked upload by assembling all chunks into the final file.

    JSON body: { "session_id": str }
    """
    data = request.get_json(silent=True) or {}
    session_id = data.get('session_id', '')

    session = upload_sessions.get(session_id)
    if not session:
        return jsonify({'error': 'Invalid session'}), 404

    expected = set(range(session['total_chunks']))
    missing = expected - session['received_chunks']

    if missing:
        return jsonify({
            'error': 'Missing chunks',
            'missing_chunks': sorted(missing),
            'total_chunks': session['total_chunks'],
            'received': len(session['received_chunks']),
        }), 400

    # Assemble chunks into final file
    final_path = os.path.join(app.config['UPLOAD_FOLDER'], session['final_filename'])
    BUFFER_SIZE = 64 * 1024 * 1024  # 64MB buffer for fast assembly

    try:
        with open(final_path, 'wb') as out:
            for i in range(session['total_chunks']):
                chunk_path = os.path.join(session['chunks_dir'], str(i))
                with open(chunk_path, 'rb') as chunk:
                    shutil.copyfileobj(chunk, out, BUFFER_SIZE)

        # Verify assembled file size
        actual_size = os.path.getsize(final_path)
        if actual_size != session['total_size']:
            # Size mismatch - log warning but don't delete (might still be usable)
            app.logger.warning(
                f"Size mismatch for {session['final_filename']}: "
                f"expected {session['total_size']}, got {actual_size}"
            )

        # Clean up temp chunks
        shutil.rmtree(session['chunks_dir'], ignore_errors=True)

        result = {
            'original_filename': session['original_filename'],
            'saved_filename': session['final_filename'],
            'size': actual_size,
            'status': 'success',
            'message': 'Upload complete',
        }
        del upload_sessions[session_id]

        return jsonify(result)

    except Exception as exc:
        # Don't clean up chunks on failure - allow retry
        app.logger.error(f"Failed to assemble chunks for session {session_id}: {exc}")
        return jsonify({
            'original_filename': session['original_filename'],
            'saved_filename': session['final_filename'],
            'status': 'failed',
            'message': f'Assembly failed: {exc}',
        }), 500


@app.route('/upload/cancel', methods=['POST'])
def upload_cancel():
    """Cancel an upload session and clean up temp files."""
    data = request.get_json(silent=True) or {}
    session_id = data.get('session_id', '')

    session = upload_sessions.get(session_id)
    if not session:
        return jsonify({'error': 'Invalid session'}), 404

    shutil.rmtree(session['chunks_dir'], ignore_errors=True)
    del upload_sessions[session_id]

    return jsonify({'status': 'cancelled', 'session_id': session_id})


# ==================== Routes: File Listing ====================
@app.route('/uploads')
def list_uploads():
    """List files in uploads folder with disk usage stats."""
    folder = app.config['UPLOAD_FOLDER']
    entries = []
    total_size = 0

    try:
        with os.scandir(folder) as it:
            for e in it:
                if not e.is_file():
                    continue
                st = e.stat()
                entries.append({
                    'name': e.name,
                    'size': st.st_size,
                    'size_display': format_file_size(st.st_size),
                    'mtime': datetime.fromtimestamp(st.st_mtime),
                })
                total_size += st.st_size
    except FileNotFoundError:
        os.makedirs(folder, exist_ok=True)

    entries.sort(key=lambda x: x['mtime'], reverse=True)

    # Disk usage info
    disk_usage = shutil.disk_usage(folder)

    return render_template('uploads.html', files=entries,
                           total_size=total_size,
                           total_size_display=format_file_size(total_size),
                           disk_total=disk_usage.total,
                           disk_total_display=format_file_size(disk_usage.total),
                           disk_used=disk_usage.used,
                           disk_used_display=format_file_size(disk_usage.used),
                           disk_free=disk_usage.free,
                           disk_free_display=format_file_size(disk_usage.free))


# ==================== Routes: File Download ====================
@app.route('/uploads/<path:filename>')
def download_upload(filename):
    """Download a file with Range request support for resume."""
    rel = _safe_upload_relpath(filename)
    if not rel:
        abort(404)

    directory = app.config['UPLOAD_FOLDER']
    # conditional=True enables ETag + Range request handling (resume support)
    return send_from_directory(
        directory, rel,
        as_attachment=True,
        download_name=os.path.basename(rel),
        conditional=True,
    )


# ==================== Routes: File Deletion ====================
@app.route('/delete/<path:filename>', methods=['POST'])
def delete_file(filename):
    """Delete a file from uploads folder."""
    rel = _safe_upload_relpath(filename)
    if not rel:
        abort(404)

    full_path = os.path.realpath(os.path.join(app.config['UPLOAD_FOLDER'], rel))

    # Double-check it's still inside UPLOAD_FOLDER
    root = os.path.realpath(app.config['UPLOAD_FOLDER'])
    if not (full_path == root or full_path.startswith(root + os.sep)):
        abort(403)

    if not os.path.isfile(full_path):
        abort(404)

    try:
        os.remove(full_path)
        return jsonify({'status': 'deleted', 'filename': rel})
    except Exception as exc:
        return jsonify({'error': str(exc)}), 500


# ==================== Routes: Disk Info API ====================
@app.route('/api/disk-info')
def disk_info():
    """API endpoint for disk usage stats (useful for monitoring)."""
    folder = app.config['UPLOAD_FOLDER']
    disk = shutil.disk_usage(folder)

    # Count files and total upload size
    total_size = 0
    file_count = 0
    try:
        with os.scandir(folder) as it:
            for e in it:
                if e.is_file():
                    total_size += e.stat().st_size
                    file_count += 1
    except Exception:
        pass

    return jsonify({
        'disk_total': disk.total,
        'disk_used': disk.used,
        'disk_free': disk.free,
        'uploads_size': total_size,
        'uploads_count': file_count,
        'disk_total_display': format_file_size(disk.total),
        'disk_used_display': format_file_size(disk.used),
        'disk_free_display': format_file_size(disk.free),
        'uploads_size_display': format_file_size(total_size),
    })


# ==================== Main ====================
if __name__ == '__main__':
    # For production, use: gunicorn -w 4 -k gevent app:app
    app.run(host='::', port=80, debug=True)
