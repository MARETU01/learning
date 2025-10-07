from PIL import Image

# ASCII字符集，从深色到浅色排列
ascii_chars = list("@#S%?*+;:,. ")

# 打开图片
img = Image.open("pic.jpg")

# 调整图片大小（固定宽度为150像素，高度按比例缩放）
width = 150
w_percent = width / float(img.size[0])
# 乘以0.5是因为字符的高宽比，使输出更协调
height = int(float(img.size[1]) * float(w_percent * 0.5))
img = img.resize((width, height), Image.NEAREST)

# 转换为灰度图
img = img.convert("L")

# 获取像素数据
pixels = img.load()

# 生成ASCII艺术
result = ""
for i in range(height):
    for j in range(width):
        # 获取当前像素的灰度值 (0-255)
        gray = pixels[j, i]
        # 将灰度值映射到ASCII字符索引 (0-12)
        # 255/11 ≈ 23.18，所以除以23得到0-11的索引
        index = int(gray / 23)
        # 确保索引不越界
        index = min(index, len(ascii_chars) - 1)
        result += ascii_chars[index]
    result += "\n"

# 输出结果
print(result)

# 可选：保存到文件
# with open("ascii_art.txt", "w") as f:
#     f.write(result)