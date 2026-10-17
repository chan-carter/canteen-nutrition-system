# 校园食堂菜品营养分析与推荐系统

面向校园食堂的点餐 + 营养分析后台，按 **学生 / 商户 / 管理员** 三种角色分工：
学生填身体档案算出每日热量与三大营养素目标，系统据此推荐菜品并可下单、追踪摄入；
商户维护档口与菜品、发起 AI 营养分析、处理订单；管理员负责审核、统计与运营。

> 课程设计演示项目：菜品、订单与用户数据均为模拟生成，不提供真实交易。
> 营养数值由 AI 模型或内置食材估算表得出，**仅供参考，不作为医学或营养学建议**。

## 目录结构

```
canteen-nutrition/          后端（Maven 多模块）
  canteen-common/           常量、工具、JWT、AOP 切面
  canteen-pojo/             实体 / DTO / VO
  canteen-server/           启动模块：controller 按端分包、service、mapper + XML
canteen-nutrition-web/      前端（Vue3 + Vite + Element Plus + Pinia + ECharts）
deploy/                     Docker Compose 部署包与说明（见 deploy/README.md）
docs/sql/                   建库脚本与演示数据
check.sh                    本地质量门：跑单测 + 前端构建
```

## 技术栈

| 层 | 选型 |
| --- | --- |
| 后端 | SpringBoot 3.2 / JDK 17 / MyBatis + PageHelper / Redis（登录态与缓存）/ JWT / EasyExcel |
| 前端 | Vue 3 / Vite 5 / Element Plus / Pinia / Vue Router / ECharts |
| 部署 | Docker Compose（mysql + redis + backend + nginx），jar 与 dist 以文件挂载 |
| AI | OpenAI 兼容协议调用大模型生成营养分析与问答，无密钥时自动降级为内置估算表 |

## 快速开始

```bash
# 1) 建库（MySQL 5.7/8.0 均可，脚本会重建 canteen_nutrition 库）
mysql --default-character-set=utf8mb4 -uroot -p < docs/sql/schema.sql
mysql --default-character-set=utf8mb4 -uroot -p < docs/sql/data.sql

# 2) 后端：需要 MYSQL_* / REDIS_* / JWT_SECRET 等环境变量，可参考 deploy/docker/canteen.env.example
cd canteen-nutrition && mvn -DskipTests package
java -jar canteen-server/target/canteen-server-1.0.0.jar

# 3) 前端（开发期 /api 由 Vite 代理到本机 8080）
cd canteen-nutrition-web && npm install && npm run dev
```

打开 `http://localhost:5173`，演示账号 `zhangsan`（学生）/ `canteen01`（商户）/ `admin`，口令统一 `123456`。
上线部署、发版与运维命令见 [deploy/README.md](deploy/README.md)。

## 主要功能

- **营养与健康**：Mifflin-St Jeor 估算 BMR/TDEE，按减脂/增肌/均衡给出热量与蛋白·脂肪·碳水目标；
  饮食日记、每日摄入进度、体重趋势、周期餐计划。
- **AI 能力**：菜品营养分析与多轮营养问答，调用留痕（`ai_call_log`），失败自动降级为内置食材估算。
- **推荐**：基于历史订单的协同过滤 + 健康目标/忌口/收藏评分重打分，支持"合口味 / 不喜欢"反馈闭环。
- **点餐流程**：购物车下单、库存与每日限量扣减、待接单→已接单→待取餐（生成取餐码）→已完成，
  超时未接单由定时任务自动取消并通知学生。
- **运营后台**：菜品与商户审核、评价管理、公告与站内消息（服务端已读状态）、数据看板与大屏、
  操作日志（AOP 记录）、Excel 导出。

## 测试与校验

```bash
./check.sh          # 后端单元测试 + 前端生产构建
```

后端单测覆盖营养计算、订单状态机、库存扣减与越权校验、推荐打分排序（`canteen-server/src/test`）。
`.github/workflows/ci.yml` 在 push 时执行同样的两步。

## 说明

数据库共 24 张表，后端 97 个接口，前端 20 余个页面；Redis 是运行期硬依赖（登录态与缓存），
本地启动需先跑起 MySQL 与 Redis。
