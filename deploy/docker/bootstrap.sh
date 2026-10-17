#!/usr/bin/env bash
# 在轻量服务器上把上传的产物装配成 Docker Compose 栈并启动
set -euo pipefail

HOME_DIR=/root/canteen-docker
STAGE=/root/canteen-staging

echo "==> $(date '+%F %T') 装配 $HOME_DIR"
mkdir -p "$HOME_DIR/artifacts" "$HOME_DIR/initdb" "$HOME_DIR/letsencrypt/.well-known/acme-challenge"

install -m 0644 "$STAGE/docker-compose.yml" "$HOME_DIR/"
install -m 0644 "$STAGE/nginx.canteen.conf" "$HOME_DIR/"
install -m 0644 "$STAGE/schema.sql" "$HOME_DIR/initdb/01_schema.sql"
install -m 0644 "$STAGE/data.sql" "$HOME_DIR/initdb/02_data.sql"
install -m 0644 "$STAGE/canteen-server-1.0.0.jar" "$HOME_DIR/artifacts/"

rm -rf "$HOME_DIR/artifacts/dist"
tar -xzf "$STAGE/dist.tar.gz" -C "$HOME_DIR/artifacts"
# 绑定挂载记的是目录 inode：先 rm -rf 再重建会让容器仍指向旧目录，静态文件全 403，必须重启 web 重挂
docker compose -f "$HOME_DIR/docker-compose.yml" restart web >/dev/null 2>&1 || true

# 1.6G 内存的机器：MySQL 容器 + JVM 同时起，没有 swap 很容易被 OOM killer 挑掉
if [ ! -f /swapfile ]; then
  echo "==> 创建 2G swap"
  fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile >/dev/null && swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
free -m | sed -n '1,3p'

if [ ! -f "$HOME_DIR/.env" ]; then
  echo "==> 生成 .env（compose 用的数据库口令，只在服务器上生成）"
  ROOTPW=$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-24)
  APPPW=$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-22)
  printf 'MYSQL_ROOT_PASSWORD=%s\nMYSQL_PASSWORD=%s\n' "$ROOTPW" "$APPPW" > "$HOME_DIR/.env"
  chmod 600 "$HOME_DIR/.env"
  echo "$APPPW" > /root/.canteen_dbpw
  chmod 600 /root/.canteen_dbpw
else
  echo "==> 复用已有 .env"
fi
APPPW=$(grep '^MYSQL_PASSWORD=' "$HOME_DIR/.env" | cut -d= -f2-)

if [ ! -f "$HOME_DIR/canteen.env" ]; then
  echo "==> 生成 canteen.env（后端环境变量，含随机 JWT 密钥）"
  JWT=$(openssl rand -base64 48 | tr -d '\n' | cut -c1-64)
  cat > "$HOME_DIR/canteen.env" <<ENV
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=canteen_nutrition
MYSQL_USERNAME=canteen
MYSQL_PASSWORD=${APPPW}
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=
JWT_SECRET=${JWT}
AI_BASE_URL=
AI_MODEL=
OPENAI_API_KEY=
OSS_ENDPOINT=
OSS_REGION=
OSS_BUCKET=
OSS_ACCESS_KEY_ID=
OSS_ACCESS_KEY_SECRET=
JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -Djava.net.preferIPv4Stack=true -Dai.timeout-seconds=90 -Xms256m -Xmx512m
ENV
  chmod 600 "$HOME_DIR/canteen.env"
  echo "   AI 与 OSS 暂留空：营养分析自动降级为内置食材估算，图片上传需补密钥"
else
  echo "==> 复用已有 canteen.env"
fi

echo "==> 启动容器"
cd "$HOME_DIR"
docker compose up -d

echo "==> 等待后端就绪（最多 150 秒）"
BODY=''
for _ in $(seq 1 50); do
  BODY=$(curl -s -m 5 http://127.0.0.1/api/captcha || true)
  case "$BODY" in *'"code":1'*) break ;; esac
  sleep 3
done
docker compose ps
echo "   首页：$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1/)"
case "$BODY" in
  *'"code":1'*) echo '   nginx -> backend -> MySQL/Redis：通' ;;
  *) echo "   异常，最后返回：${BODY:0:200}"; docker compose logs --tail=40 backend ;;
esac
echo "==> $(date '+%F %T') 完成"
touch /root/up.done
