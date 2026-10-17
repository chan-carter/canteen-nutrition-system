#!/usr/bin/env bash
# 日常更新：只替换后端 jar 与前端 dist，不动 canteen.env 和 nginx 配置
# 用法： sudo bash deploy.sh [上传目录]     默认 /root/canteen-upload
set -euo pipefail

SRC="${1:-/root/canteen-upload}"
APP_DIR=/opt/canteen
WEB_DIR=/var/www/canteen

[ "$(id -u)" = 0 ] || { echo '请用 root 或 sudo 运行'; exit 1; }
[ -f "$SRC/canteen-server-1.0.0.jar" ] || { echo "!! 缺少 $SRC/canteen-server-1.0.0.jar"; exit 1; }

cp -f "$SRC/canteen-server-1.0.0.jar" "$APP_DIR/canteen-server-1.0.0.jar.new"
chown canteen:canteen "$APP_DIR/canteen-server-1.0.0.jar.new"
mv -f "$APP_DIR/canteen-server-1.0.0.jar.new" "$APP_DIR/canteen-server-1.0.0.jar"

if [ -d "$SRC/dist" ]; then
  rm -rf "$WEB_DIR/dist.old"
  [ -d "$WEB_DIR/dist" ] && mv "$WEB_DIR/dist" "$WEB_DIR/dist.old"
  cp -a "$SRC/dist" "$WEB_DIR/dist"
  chown -R canteen:canteen "$WEB_DIR/dist"
fi

systemctl restart canteen
# 缓存键带角色与商户范围，重启后 Redis 里的旧缓存仍然可读，无需清库；
# 若要强制全新一轮统计口径，取消下一行注释（会让所有已登录会话掉线重新登录）
# redis-cli flushdb

sleep 5
systemctl is-active --quiet canteen && echo 'canteen 服务正常' || { echo '!! 服务未起来：journalctl -u canteen -n 50'; exit 1; }
curl -s -o /dev/null -w '  http://127.0.0.1/          -> %{http_code}\n' http://127.0.0.1/
curl -s -m 10 http://127.0.0.1/api/captcha | grep -q '"code":1' \
  && echo '  http://127.0.0.1/api/captcha -> code 1（nginx/后端/MySQL/Redis 全通）' \
  || echo '  !! 接口自检未通过：journalctl -u canteen -n 50 --no-pager'
