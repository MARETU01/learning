from flask import Flask, request, render_template

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 30 * 1024 * 1024 * 1024

@app.route('/')
def upload_form():
    return render_template('submit.html')

@app.route('/upload', methods=['POST'])
def upload_file():
    if request.method == 'POST':
        if 'file' not in request.files:
            return "No file part", 400
        file = request.files['file']
        if file.filename == '':
            return "No selected file", 400
        if file:
            file.save(file.filename)
            return "File successfully uploaded"

if __name__ == '__main__':
    app.run(host='::', port=80, debug=True)