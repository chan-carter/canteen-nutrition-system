# 服务器部署（Docker Compose）

线上：`http://wzbc.webchan.cn/`（解析到 121.41.92.16，阿里云轻量服务器 Ubuntu 24.04 / 2核1.6G，域名已备案）。
四个容器 `mysql` / `redis` / `backend`（eclipse-temurin 直接跑 jar）/ `web`（nginx 托管 dist 并反代 `/api`），
配置都在本目录的 `docker/` 下。**不构建自定义镜像**：jar 与 dist 以文件挂载进容器，发版只换文件再 restart。

演示账号 `zhangsan / canteen01 / admin`，口令统一 `123456`。

## 目录

服务器上的 `/root/canteen-docker/`（口令与密钥只在服务器上随机生成、权限 600，绝不进仓库）：

```
docker-compose.yml   nginx.canteen.conf
.env                 MySQL root 与应用账号口令（bootstrap 首次生成）
canteen.env          后端环境变量：MYSQL_HOST=mysql、REDIS_HOST=redis、JWT_SECRET、AI/OSS 密钥
artifacts/canteen-server-1.0.0.jar   artifacts/dist/
initdb/01_schema.sql 02_data.sql     # 只在数据卷为空时执行一次
letsencrypt/                          # 将来签证书用
```

仓库里的 `deploy/docker/`：`docker-compose.yml`、`nginx.canteen.conf`、`bootstrap.sh`（首次装配）、
`env.example`（compose 的 `.env`）、`canteen.env.example`（后端变量）。

MySQL 与 Redis **不映射宿主机端口**，只在 compose 内部网络可达；后端 8080 也只对 nginx 暴露，外部唯一入口是 80。
1.6G 内存必须留 swap，并把 InnoDB 缓冲池压到 192M、关掉 performance_schema（compose 里已配）。

## 首次装配

```bash
# 服务器上装 docker（apt 装完服务已在跑）
apt-get update && apt-get install -y docker.io docker-compose-plugin
# 境内直连 Docker Hub 会被拒，先配镜像加速；改完必须重启才生效
printf '{"registry-mirrors":["https://docker.m.daocloud.io","https://docker.1ms.run"]}\n' > /etc/docker/daemon.json
systemctl restart docker          # apt 装完服务已在跑，不重启等于没配

# 本机把产物传到 /root/canteen-staging/：docker-compose.yml、nginx.canteen.conf、bootstrap.sh、
# schema.sql、data.sql、canteen-server-1.0.0.jar、dist.tar.gz（打包与上传命令见下一节）
bash /root/canteen-staging/bootstrap.sh
```

bootstrap 做的事：装 compose 与 nginx 配置 → 解 dist → 没有 swap 就建 2G →
首次生成 `.env`（随机 MySQL 口令，应用口令另存 `/root/.canteen_dbpw`）和 `canteen.env`（随机 JWT_SECRET，AI/OSS 留空）
→ `docker compose up -d` → 轮询 `/api/captcha` 自检。**可重复执行，不会覆盖服务器上已有的口令与密钥。**

`OPENAI_API_KEY` 留空也能跑：营养分析自动降级为内置食材估算，页面标注"估算、仅供参考"；
图片上传需要 OSS 密钥，没配就没有配图。

## 发版

```bash
# 本机打包
mvn -q -DskipTests package -f canteen-nutrition/pom.xml
npm run build --prefix canteen-nutrition-web
tar -czf dist.tar.gz -C canteen-nutrition-web dist   # 必须带 dist/ 前缀，解到 artifacts/ 下才是 artifacts/dist/

# 上传到 /root/canteen-staging/：dist.tar.gz 与 canteen-server-1.0.0.jar
# 这台服务器没有 SFTP 子系统，scp/sftp 传不上去；70MB 的 jar 走 paramiko 的 exec_command + cat，
# 3MB 一块、块级重试、传完比 md5（自己的小脚本，放本地不入库）
# Git Bash 会把 /root/... 这类远程路径改写成 D:/App/Git/root/...，命令前要加
# MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL="*"

# 服务器装配
cd /root/canteen-docker/artifacts && rm -rf dist && tar -xzf /root/canteen-staging/dist.tar.gz -C .
cp /root/canteen-staging/canteen-server-1.0.0.jar /root/canteen-docker/artifacts/
cd /root/canteen-docker && docker compose restart web backend
```

只改前端就 `restart web`，只改后端就 `restart backend`（改的是同名 jar，绑定挂载不受 inode 影响）。

## 运维

```bash
cd /root/canteen-docker
docker compose ps                     # 四个都应是 Up/healthy
docker compose logs -f backend
docker exec canteen-mysql mysql -ucanteen -p"$(cat /root/.canteen_dbpw)" canteen_nutrition
redis-cli 只在容器里：docker exec canteen-redis redis-cli keys 'canteen:login:token:*'

# 备份（数据都在命名卷里，docker compose down 别加 -v，加了就删库）
docker exec canteen-mysql mysqldump -ucanteen -p"$(cat /root/.canteen_dbpw)" --single-transaction \
  canteen_nutrition | gzip > /root/canteen-$(date +%F).sql.gz
```

跑含中文的 SQL 文件时同样要带 `--default-character-set=utf8mb4`；在服务器上手敲容易踩引号与字符集，
稳妥做法是先 `docker cp` 进容器再 `mysql < 文件`。

## HTTPS（当前未开）

备案已生效，Let's Encrypt 的 HTTP-01 校验可直接通过，不再需要 DNS-01。现在只监听 80，
代价是登录令牌与口令明文走公网。要上 HTTPS 三步：

```bash
certbot certonly --webroot -w /root/canteen-docker/letsencrypt -d wzbc.webchan.cn \
  --non-interactive --agree-tos -m 你的邮箱 --deploy-hook "docker compose -f /root/canteen-docker/docker-compose.yml restart web"
```

2. 把证书目录挂进 web 容器（`/etc/letsencrypt` 只读），compose 的 `ports` 加 `"443:443"`，安全组放行 443；
3. `nginx.canteen.conf` 里补 443 server 块，并把 80 的 `location ^~ /.well-known/acme-challenge/` 保留
   （它已经指向 `./letsencrypt`，**不要把 webroot 指向 dist**，前端每次发版会轮换该目录，校验文件会被一起删掉）。

签完先 `certbot renew --dry-run` 确认能续（必须 0 renew failure）；真验一次用 `--force-renewal`，
同一组域名每周上限 5 张，别反复跑。

## 已经踩过的坑

- 绑定挂载记的是目录 inode。`rm -rf artifacts/dist` 重建后，容器里的 nginx 仍指向被删掉的旧目录，
  静态文件全部 403（`/api` 却正常，很容易误判成后端问题）。必须 `docker compose restart web` 重挂。
- 后端健康检查要打 `http://127.0.0.1:8080/captcha`，不是 `/api/captcha`——`/api` 前缀是 nginx 剥掉的。
  写错会让 backend 永远停在 `health: starting`，`web` 因依赖条件不满足而根本不启动。
- 改 `canteen.env`（比如补 AI 密钥）之后 `docker compose restart backend` **不会**重读 env_file，
  `up -d` 也不会。必须 `docker compose up -d --force-recreate backend`，否则容器里还是旧变量，
  现象是密钥明明写了、后端仍走内置估算降级。
- 容器里访问 Cloudflare 边缘（AI 网关）时 JVM 会优先试 IPv6 而出不去，报 `HttpConnectTimeoutException`。
  `JAVA_OPTS` 里加 `-Djava.net.preferIPv4Stack=true`（host 上 `curl` 正常，只有容器内 Java 超时，容易误判成密钥或网关问题）。
- 从 Git Bash 传大文件时远程路径会被 MSYS 改写，paramiko 侧只报一句 `Socket is closed`，看着像网络问题。
  命令前加 `MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL="*"`。
- 导入含中文的 SQL 必须带 `--default-character-set=utf8mb4`，否则种子中文按 latin1 存成双重编码，
  页面显示 `éº¸è´¨`。
- 服务器在 cn-hangzhou、OSS bucket 在 cn-beijing，**跨地域只能用公网 endpoint**，
  `oss-cn-beijing-internal` 探测不可达。

## 验证清单

```bash
docker compose ps                                        # 四个 Up/healthy
curl -sI http://127.0.0.1/ | head -1                     # 200
curl -s  http://127.0.0.1/api/captcha | head -c 80       # {"code":1,...,"key":...}
curl -sI http://wzbc.webchan.cn/ | head -1               # 备案已生效，这里应是 nginx 而不是阿里云拦截页
```

再在浏览器里过一遍真实链路：
学生登录 → 菜品浏览加购 → 下单 → 商户接单/出餐/完成（出学生取餐码）→ 学生评价；
管理员数据看板有数、公告发布后学生端红点亮、点"全部标为已读"后红点消失且刷新不复现。
手机宽度（≤767px）另有一套布局：表格不固定右列、筛选条两列排布，改样式时两个断点都要看。

## 常见坑

| 现象 | 原因与处理 |
| --- | --- |
| 页面能开但接口 502 | `docker compose logs backend`；多半是 `canteen.env` 里 MySQL/Redis 口令与 `.env` 不一致 |
| web 一直不启动 | backend 健康检查没过，看上一条；健康检查地址必须是 `/captcha` 不是 `/api/captcha` |
| 静态文件全 403 但 `/api` 正常 | 重建 dist 后绑定挂载仍指向旧 inode，`docker compose restart web` |
| AI 分析一直显示"内置食材估算" | `canteen.env` 改了没 `--force-recreate`，或容器出网被 IPv6 拖死（见坑第 4 条） |
| 上传的图片在页面打不开 | OSS bucket 需公共读；`OSS_BUCKET`/`OSS_ENDPOINT` 要与实际一致，密钥只在 env 里 |
| 统计数字比库里少 | 统计接口有 3 分钟 Redis 缓存，看板右上角"刷新数据"，或 `docker exec canteen-redis redis-cli --scan --pattern 'reportCache*'` 删键 |
| 所有接口 401 | Redis 重启或执行过 FLUSHDB，登录态键没了；重新登录即可（登录态是硬依赖，无降级） |
| 订单时间、日报统计整体差 8 小时 | 容器时区。compose 里 MySQL `--default-time-zone=+08:00`、JVM `-Duser.timezone=Asia/Shanghai`，别删 |
| 手机访问不了 | 轻量服务器控制台"防火墙"没放行 80（不是 ECS 安全组，位置不同） |

## 附：裸机方案

`deploy/install.sh`、`deploy/deploy.sh`、`deploy/nginx/`、`deploy/systemd/` 是重装系统之前的
apt + systemd 单机方案，已被上面的 Docker 方案取代，留着仅作对照，不再维护。
