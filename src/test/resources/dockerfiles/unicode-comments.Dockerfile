# 使用官方 Python 3.9 作为基础镜像
FROM python:3.9-slim

# 设置工作目录
WORKDIR /app

# 复制 requirements.txt 到工作目录
COPY requirements.txt .

# 安装 Python 依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制整个应用代码
COPY . .

# 暴露端口 5000
EXPOSE 5000

# 运行 Flask 应用
CMD ["python", "app.py"]
