from flask import *

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 30 * 1024 * 1024 * 1024

@app.route('/')
def upload_form():
    return render_template('submit.html')

@app.route('/upload', methods=['POST'])
def upload_file():
    if request.method == 'POST':
        # 检查是否有文件对象
        if 'file' not in request.files:
            return "No file part"
        file = request.files['file']
        # 如果用户没有选择文件，浏览器也会提交一个空的部分没有文件名
        if file.filename == '':
            return "No selected file"
        if file:
            # 保存文件到服务器上的某个位置
            file.save(file.filename)
            return "File successfully uploaded"

if __name__ == '__main__':
    app.run(host='::', port=80, debug=True)