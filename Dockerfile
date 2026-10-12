FROM python:3.12-slim
WORKDIR /app
COPY backend/requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r requirements.txt && useradd -u 10001 -m mentor && mkdir /data && chown mentor /data
COPY backend /app/backend
COPY web /app/web
USER mentor
ENV DATABASE_PATH=/data/mentor.db
EXPOSE 8000
CMD ["python","-m","uvicorn","backend.app:app","--host","0.0.0.0","--port","8000","--no-proxy-headers"]
