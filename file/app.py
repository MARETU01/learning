from flask import Flask, request, render_template, jsonify
from werkzeug.utils import secure_filename
import os

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 30 * 1024 * 1024 * 1024
app.config['UPLOAD_FOLDER'] = os.path.join(app.root_path, 'uploads')
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

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
        if uploaded.filename == '':
            results.append({'filename': '', 'status': 'failed', 'message': 'Empty filename'})
            continue

        filename = secure_filename(uploaded.filename)
        if not filename:
            results.append({'filename': uploaded.filename, 'status': 'failed', 'message': 'Invalid filename'})
            continue

        target_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        try:
            uploaded.save(target_path)
            results.append({'filename': filename, 'status': 'success', 'message': 'Uploaded successfully'})
        except Exception as exc:  # pragma: no cover - defensive path
            results.append({'filename': filename, 'status': 'failed', 'message': str(exc)})

    status_code = 200 if any(item['status'] == 'success' for item in results) else 400
    return jsonify({'results': results}), status_code

if __name__ == '__main__':
    app.run(host='::', port=80, debug=True)