#!/usr/bin/env bash
# 一次性初始化 Ubuntu/Debian 服务器：装 JDK17+nginx、放置产物与配置、注册 systemd 服务
#
# 上传目录约定（默认 /root/canteen-upload，可用第一个参数改）：
#   canteen-server-1.0.0.jar   后端可执行 jar
#   dist/                      前端 npm run build 产物
#   canteen.env                由 canteen.env.example 复制并填好真实值
#   deploy/                    本目录整体
#
# 用法： sudo bash /root/canteen-upload/deploy/install.sh [上传目录] [你的域名]
#        例：sudo bash /root/canteen-upload/deploy/install.sh /root/canteen-upload example.com
# 重复执行安全：env 文件已存在时不会被覆盖，证书就位后重跑即可启用 443。
set -euo pipefail

SRC="${1:-/root/canteen-upload}"
DOMAIN="${2:-}"
APP_DIR=/opt/canteen
WEB_DIR=/var/www/canteen
CRT=/etc/nginx/ssl/canteen/fullchain.pem
KEY=/etc/nginx/ssl/canteen/privkey.pem

[ "$(id -u)" = 0 ] || { echo '请用 root 或 sudo 运行'; exit 1; }
DEPLOY_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

for f in canteen-server-1.0.0.jar dist canteen.env; do
  [ -e "$SRC/$f" ] || { echo "!! 上传目录缺少 $SRC/$f，请先按 README 第 2 步上传"; exit 1; }
done

echo '==> 安装 JDK17 与 nginx'
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq openjdk-17-jre-headless nginx curl

# 云主机镜像默认 UTC：SQL 里的 now()、date_format() 会按 UTC 取值，与北京时间差 8 小时
if [ "$(cat /etc/timezone 2>/dev/null)" != 'Asia/Shanghai' ]; then
  timedatectl set-timezone Asia/Shanghai && echo '   时区已设为 Asia/Shanghai'
  echo '   注意：MySQL 若在本脚本之前已启动，需 systemctl restart mysql 才会跟随时区'
fi

id -u canteen >/dev/null 2>&1 || useradd --system --home-dir "$APP_DIR" --shell /usr/sbin/nologin canteen
mkdir -p "$APP_DIR/logs" "$WEB_DIR" /etc/nginx/ssl/canteen /etc/nginx/snippets
# ACME 校验专用 webroot，独立于前端 dist，nginx worker 需可读
mkdir -p /var/www/letsencrypt/.well-known/acme-challenge
chown -R www-data:www-data /var/www/letsencrypt && chmod 755 /var/www/letsencrypt /var/www/letsencrypt/.well-known /var/www/letsencrypt/.well-known/acme-challenge
# logback.xml 里 AI_FILE 会往工作目录下的 logs/ 写滚动日志，目录必须归服务用户可写
chown canteen:canteen "$APP_DIR" "$APP_DIR/logs"

echo '==> 放置后端 jar / 前端 dist / 环境变量'
install -m 0644 -o canteen -g canteen "$SRC/canteen-server-1.0.0.jar" "$APP_DIR/"
rm -rf "$WEB_DIR/dist.new"
cp -a "$SRC/dist" "$WEB_DIR/dist.new"
rm -rf "$WEB_DIR/dist.old"
[ -d "$WEB_DIR/dist" ] && mv "$WEB_DIR/dist" "$WEB_DIR/dist.old"
mv "$WEB_DIR/dist.new" "$WEB_DIR/dist"
chown -R canteen:canteen "$WEB_DIR"
if [ ! -f "$APP_DIR/canteen.env" ]; then
  install -m 0600 -o canteen -g canteen "$SRC/canteen.env" "$APP_DIR/canteen.env"
  echo '   已放置 canteen.env（权限 600，属主 canteen）'
else
  echo '   canteen.env 已存在，保留服务器上的版本（要改密钥请直接编辑该文件）'
fi

echo "==> 配置 nginx（域名：${DOMAIN:-未提供，仅按 IP 访问}）"
install -m 0644 "$DEPLOY_DIR/nginx/canteen-app.conf" /etc/nginx/snippets/canteen-app.conf
install -m 0644 "$DEPLOY_DIR/nginx/canteen-upstream.conf" /etc/nginx/conf.d/canteen-upstream.conf
sed "s|<DOMAIN>|${DOMAIN:-_}|g" "$DEPLOY_DIR/nginx/canteen-http.site" > /etc/nginx/sites-available/canteen-http
sed "s|<DOMAIN>|${DOMAIN:-_}|g" "$DEPLOY_DIR/nginx/canteen-https.site" > /etc/nginx/sites-available/canteen-https
rm -f /etc/nginx/sites-enabled/default
ln -sfn /etc/nginx/sites-available/canteen-http /etc/nginx/sites-enabled/canteen-http
if [ -s "$CRT" ] && [ -s "$KEY" ] && [ -n "$DOMAIN" ]; then
  ln -sfn /etc/nginx/sites-available/canteen-https /etc/nginx/sites-enabled/canteen-https
  echo '   证书已就位，443 已启用'
else
  rm -f /etc/nginx/sites-enabled/canteen-https
  echo '   暂无证书，只启用 80；按 README 第 4 步签发后重跑本脚本即可启用 443'
fi
nginx -t
systemctl reload nginx

echo '==> 注册并启动后端服务'
install -m 0644 "$DEPLOY_DIR/systemd/canteen.service" /etc/systemd/system/canteen.service
systemctl daemon-reload
systemctl enable canteen
# 用 restart 而不是 --now：服务已在运行时 --now 不会重启，本次放进去的新 jar 与新 env 就不会生效
systemctl restart canteen

echo
echo '==> 自检'
echo "   服务器时间：$(date '+%F %T %Z')（应为北京时间，与云厂商控制台相差 8 小时说明时区没生效）"
echo "   后端进程：  $(systemctl is-active canteen)"
# SpringBoot 在 1G 小机器上要十几秒才监听端口，轮询到接口真正可用为止（最多 90 秒）
BODY=''
for _ in $(seq 1 45); do
  BODY=$(curl -s -m 5 http://127.0.0.1/api/captcha || true)
  case "$BODY" in *'"code":1'*) break ;; esac
  sleep 2
done
case "$BODY" in
  *'"code":1'*) echo '   nginx->后端->Redis：通' ;;
  *) echo "   nginx->后端->Redis：异常，返回 ${BODY:0:120}"; echo '   排查：journalctl -u canteen -n 50 --no-pager' ;;
esac
if grep -q 'CHANGE_ME' "$APP_DIR/canteen.env"; then
  echo '   !! canteen.env 里还有 CHANGE_ME 占位值（数据库口令/ JWT_SECRET / OSS bucket），必须改完再 systemctl restart canteen'
fi
echo '   浏览器验证： http://<服务器IP>/  （云安全组需放行 80，启用 https 后放行 443）'
