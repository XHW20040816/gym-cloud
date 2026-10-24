# 🏋️ 健身房管理系统（Spring Cloud Alibaba 微服务样板）

## 1. 项目简介

本项目是一个教学 / 演示级的健身房全栈微服务系统：后端 Spring Cloud Alibaba（12 个微服务，16 个 Maven 子模块），前端 Vue 3 + Vite + Element Plus，中间件由 Docker Compose 一键编排。覆盖登录鉴权、用户 / 教练 / 商品 / 餐饮 / 会员卡 / 课程预约、订单、余额支付、限时秒杀、Elasticsearch 搜索、Sentinel 流控与 Seata 分布式事务。

本文档中的所有端口、库名、Redis key、接口返回、版本号均与仓库代码和 Nacos 配置一致（按实际实现整理）。

***

## 2. 服务列表

| 服务            |  端口  | 数据库          | 说明                                        |
| :------------ | :--: | :----------- | :---------------------------------------- |
| gym-gateway   | 8080 | -            | 路由转发（11 条）、JWT 校验、CORS、Sentinel 网关流控      |
| gym-user      | 8081 | gym\_user    | 用户信息、余额扣减（Feign 内部接口）                     |
| gym-auth      | 8082 | gym\_user    | Spring Security + JWT 登录 / 刷新 / 注销        |
| gym-goods     | 8083 | gym\_goods   | 商品 / 分类 CRUD、库存乐观锁、RabbitMQ 同步 ES         |
| gym-food      | 8084 | gym\_food    | 营养餐                                       |
| gym-member    | 8085 | gym\_member  | 会员卡、用户会员                                  |
| gym-coach     | 8086 | gym\_user    | 教练与排班（与 user/auth 共库）                     |
| gym-course    | 8087 | gym\_course  | 课程、排期、报名（容量控制）                            |
| gym-order     | 8088 | gym\_order   | 订单主表 / 明细、TTL 超时取消、跨服务 Seata 事务           |
| gym-seckill   | 8089 | gym\_seckill | Lua 扣库存 + MQ 异步建单 + TTL 回滚                |
| gym-payment   | 8090 | gym\_payment | 策略模式支付、Seata AT 全局事务                      |
| gym-search    | 8092 | gym\_goods   | ES 搜索 / 高亮 / 自动补全（8091 为 Seata Server 端口） |
| gym-admin-web | 5173 | -            | Vue 3 管理后台（`npm run dev`）                 |
| gym-user-web  | 5174 | -            | Vue 3 会员前台（`npm run dev`）                 |

> gym-user 只负责用户本身，不再聚合 coach / course / member；教练、课程、会员卡分别由独立服务提供。

***

## 3. 技术栈（实际版本）

| 分类    | 技术                                          | 版本                                             |
| :---- | :------------------------------------------ | :--------------------------------------------- |
| JDK   | 编译目标                                        | **1.8**（pom `java.version=1.8`，实测可用 JDK 17 运行） |
| 构建    | Maven                                       | 3.8+                                           |
| 基础    | Spring Boot                                 | 2.7.18                                         |
| 微服务   | Spring Cloud                                | 2021.0.8（Gateway 3.1.8）                        |
| 微服务   | Spring Cloud Alibaba                        | 2021.0.5.0                                     |
| 注册/配置 | Nacos                                       | 2.2.3 standalone（**Derby 持久化**，未使用 MySQL）      |
| ORM   | MyBatis-Plus                                | 3.5.5                                          |
| 连接池   | Druid                                       | 1.2.20                                         |
| 缓存/锁  | Redis 7 + Redisson                          | Redisson 3.25.0（使用 `redisson-spring-data-27`）  |
| 消息    | RabbitMQ                                    | 3.12-management（TTL + DLX、Fanout）              |
| 搜索    | Elasticsearch / Kibana                      | 7.17.15（需自行安装 IK 分词插件）                         |
| 分布式事务 | Seata                                       | 1.7.1 AT 模式                                    |
| 流控    | Sentinel Dashboard                          | 1.8.6                                          |
| 认证    | Spring Security + jjwt                      | 5.7.x + 0.11.x                                 |
| 工具    | Hutool / Fastjson2 / Lombok                 | 5.8.25 / 2.0.43 / 1.18.x                       |
| 前端    | Vue / Element Plus / Vite / Pinia / ECharts | 3.4 / 2.5 / 5.0 / 2.1 / 5.4                    |

> 项目**没有**集成 Knife4j / springdoc UI，仅有 `swagger-annotations` 注解。

***

## 4. 配置体系（唯一口径）

### 4.1 本地 `bootstrap.yml`

12 个服务的 `bootstrap.yml` 只保留三件事：

```yaml
spring:
  application:
    name: gym-xxx
  profiles:
    active: dev
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      config:
        file-extension: yml
        shared-configs:
          - data-id: application.yml
            group: DEFAULT_GROUP
```

**端口、数据源、Redis、RabbitMQ、Seata、JWT 全部来自 Nacos**，本地不再有第二套配置。

### 4.2 Nacos 中的配置

`DEFAULT_GROUP` 共 **13 个 yml + 1 个 json**：

- `application.yml`（公共：Redis / RabbitMQ / Seata / Sentinel / Jackson / 日志）
- `gym-{user,auth,goods,food,member,coach,course,order,seckill,payment,search,gateway}-dev.yml`
- `sentinel-gateway-flow-rules.json`（网关流控规则）

`SEATA_GROUP` 还需要一条脚本型配置（Seata 客户端按分组名查询）：

| dataId                                   | group         | 内容        |
| :--------------------------------------- | :------------ | :-------- |
| `service.vgroupMapping.default_tx_group` | `SEATA_GROUP` | `default` |

所有服务统一使用 `seata.tx-service-group: default_tx_group`（公共配置提供，各服务不再单独声明）。

***

## 5. 环境准备

| 软件             | 要求                                 |
| :------------- | :--------------------------------- |
| JDK            | 8 或 17（pom 编译目标 1.8，实测 JDK 17 可运行） |
| Maven          | 3.8+                               |
| Node.js        | 18+                                |
| Docker Desktop | 支持 `docker compose`                |
| 内存             | 建议 ≥ 6GB（ES 约占 1GB）                |

### 5.1 中间件容器（`docker compose -p gym`）

| 容器                     | 镜像                              | 端口                 | 账号 / 密码                 |
| :--------------------- | :------------------------------ | :----------------- | :---------------------- |
| gym-mysql              | mysql:8.0                       | 3306               | root / root             |
| gym-redis              | redis:7-alpine                  | 6379               | 无                       |
| gym-rabbitmq           | rabbitmq:3.12-management-alpine | 5672 / 15672       | guest / guest           |
| gym-elasticsearch      | elasticsearch:7.17.15           | 9200 / 9300        | 无                       |
| gym-kibana             | kibana:7.17.15                  | 5601               | 无（可选）                   |
| gym-nacos              | nacos/nacos-server:v2.2.3       | 8848 / 9848 / 9849 | nacos / nacos           |
| gym-seata-server       | seataio/seata-server:1.7.1      | 8091 / 7091        | 控制台 seata / seata       |
| gym-sentinel-dashboard | bladex/sentinel-dashboard:1.8.6 | 8858               | sentinel / sentinel（可选） |

- Nacos 数据落盘在 `docker/nacos/data`（**Derby**），换机器部署时该目录可清空重来。
- Seata Server 注册到 Nacos `SEATA_GROUP`，会话存储 `file`。
- **Windows Docker Desktop 注意事项**：容器 IP（172.x）宿主机不可达，而 `SEATA_IP=127.0.0.1` 会被 Seata 当作回环地址拒绝，最终回落到容器 IP，导致客户端连不上 TC。`docker-compose.yml` 中已用 `SEATA_IP=${SEATA_IP:-192.168.1.3}` 指定宿主机 LAN IP；**本机 IP 变化时**需要：

```powershell
SEATA_IP=<你的宿主机IP> docker compose -p gym up -d --force-recreate seata-server
```

***

## 6. 快速启动

### 步骤 1：启动中间件

```powershell
cd d:\idea项目\健身房\健身房
docker compose -p gym up -d
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### 步骤 2：建库建表（容器首次启动自动执行）

`docker-compose.yml` 已把 SQL 挂到 initdb 目录，并**按文件名升序执行**：

```yaml
- ../sql/init.sql:/docker-entrypoint-initdb.d/00-init.sql
- ../sql/data.sql:/docker-entrypoint-initdb.d/01-data.sql
```

- `init.sql`：创建 **8 个库**（每个库都含 `undo_log`）：
  `gym_user`（t\_user、t\_coach）、`gym_goods`（t\_goods\_category、t\_goods）、
  `gym_order`（t\_orders、t\_order\_item）、`gym_payment`（t\_payment）、
  `gym_course`（t\_course、t\_course\_schedule、t\_course\_enrollment）、
  `gym_food`（t\_food）、`gym_member`（t\_member\_card、t\_user\_member）、
  `gym_seckill`（t\_seckill\_activity、t\_seckill\_goods、t\_seckill\_order）。
- `data.sql`：admin / coach / user1（密码均为 `123456`）、7 个商品、4 种会员卡、3 门课程 + 3 个排期、3 条餐饮、2 个秒杀活动（活动 1 有 1 个秒杀商品，库存 100、秒杀价 149）。

手工执行（可选）：

```powershell
Get-Content sql/init.sql | docker exec -i gym-mysql mysql -uroot -proot
Get-Content sql/data.sql | docker exec -i gym-mysql mysql -uroot -proot
```

> `t_seckill_order` 的唯一键是 `uk_user_goods (user_id, goods_id)`，与 Redis Lua 的「按商品限购」口径一致。

### 步骤 3：导入 Nacos 配置

方式 A（控制台）：<http://127.0.0.1:8848/nacos> → 配置管理 → 导入 `nacos-config/` 下的 13 个 yml + 1 个 json 到 `DEFAULT_GROUP`；再在 `SEATA_GROUP` 新建 `service.vgroupMapping.default_tx_group = default`。

方式 B（HTTP API）：

```powershell
$dir='nacos-config'
Get-ChildItem $dir -Filter *.yml | ForEach-Object {
  Invoke-RestMethod -Uri 'http://127.0.0.1:8848/nacos/v1/cs/configs' -Method POST -Body @{
    dataId=$_.Name; group='DEFAULT_GROUP'; type='yaml'; content=[IO.File]::ReadAllText($_.FullName)
  }
}
Invoke-RestMethod -Uri 'http://127.0.0.1:8848/nacos/v1/cs/configs' -Method POST -Body @{
  dataId='sentinel-gateway-flow-rules.json'; group='DEFAULT_GROUP'; type='json';
  content=[IO.File]::ReadAllText("$dir\sentinel-gateway-flow-rules.json")
}
Invoke-RestMethod -Uri 'http://127.0.0.1:8848/nacos/v1/cs/configs' -Method POST -Body @{
  dataId='service.vgroupMapping.default_tx_group'; group='SEATA_GROUP'; type='text'; content='default'
}
```

### 步骤 4：构建并启动服务

```powershell
mvn -B clean package -DskipTests
```

启动顺序（jar 位于各模块 `target/`）：

1. gym-user、gym-auth、gym-goods、gym-food、gym-member、gym-coach
2. gym-course、gym-order、gym-seckill、gym-payment、gym-search
3. **最后** gym-gateway

验证：Nacos 服务列表 12 个服务各 1 实例；`8080-8092` 端口全部监听；网关日志无 Sentinel / CORS 异常。

### 步骤 5：前端

```powershell
# 管理后台
cd gym-admin-web
npm install
npm run dev     # http://localhost:5173

# 会员前台（新终端）
cd gym-user-web
npm install
npm run dev     # http://localhost:5174
```

> 两个前端目录下的 `.npmrc` 均已内置 `registry=https://registry.npmmirror.com`，无需再手动配置镜像。

***

## 7. 网关路由

全部路由 `StripPrefix=1`（即 `/api/xxx/**` → 下游 `/xxx/**`）：

| 路由          | 匹配                | 下游               |
| :---------- | :---------------- | :--------------- |
| gym-auth    | `/api/auth/**`    | lb://gym-auth    |
| gym-user    | `/api/user/**`    | lb://gym-user    |
| gym-goods   | `/api/goods/**`   | lb://gym-goods   |
| gym-food    | `/api/food/**`    | lb://gym-food    |
| gym-member  | `/api/member/**`  | lb://gym-member  |
| gym-coach   | `/api/coach/**`   | lb://gym-coach   |
| gym-course  | `/api/course/**`  | lb://gym-course  |
| gym-order   | `/api/order/**`   | lb://gym-order   |
| gym-payment | `/api/payment/**` | lb://gym-payment |
| gym-seckill | `/api/seckill/**` | lb://gym-seckill |
| gym-search  | `/api/search/**`  | lb://gym-search  |

JWT 白名单在 `gym-gateway-dev.yml → gym.auth.white-list`（登录 / 刷新 / 注销 / 秒杀公开接口 / 文档路径）。

CORS 收敛在 `gym-gateway-dev.yml → spring.cloud.gateway.globalcors`，只允许 `http://localhost:5173`、`http://127.0.0.1:5173`、`http://localhost:5174`、`http://127.0.0.1:5174`（`allow-credentials=true` 时不能用 `"*"`）。新增前端地址需在此追加；非白名单 Origin 的预检请求返回 403。网关代码里不再有 `CorsWebFilter` Bean，避免 Java 配置覆盖 yml。

***

## 8. 核心接口

统一入口 `http://127.0.0.1:8080`，除白名单外需 `Authorization: Bearer {access_token}`。

### 8.1 登录

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "user1", "password": "123456" }
```

```json
{ "code": 200, "msg": "操作成功", "success": true,
  "data": { "access_token": "eyJ...", "refresh_token": "eyJ...", "token_type": "Bearer", "expires_in": 7200 } }
```

- access\_token TTL 7200s，refresh\_token TTL 604800s。
- 刷新：`POST /api/auth/refresh`（Header `refresh_token` 或 `Authorization`）。
- 注销：`POST /api/auth/logout`（删除 Redis 中 access / refresh 两个 key）。

### 8.2 用户信息

```http
GET /api/user/info
Authorization: Bearer {access_token}
```

返回 `data.balance`（user1 初始 5000.00）等字段。

### 8.3 创建订单

```http
POST /api/order/create
{ "items": [ { "goodsId": 1, "quantity": 20 } ] }
```

返回 `data` 为**订单号字符串**（如 `371833483829579776`）。创建时通过 Feign 调 gym-goods 扣库存，方法上同时有 `@GlobalTransactional` 与 `@Transactional`。

查询订单：`GET /api/order/{orderNo}` → `data.payAmount`（本例 299 × 20 = 5980.00）。

### 8.4 余额支付

```http
POST /api/payment/pay
{ "orderNo": "371833483829579776", "payType": 1, "amount": 5980.00 }
```

- `payType`：1 余额、2 支付宝、3 微信；**金额必须等于订单** **`payAmount`**，否则返回「支付金额不符」。
- 余额支付走 `BalancePayTxService`（独立 Bean，`@GlobalTransactional(name="balance-pay-tx")`）：
  1. Feign 调 gym-user 扣余额（`UPDATE t_user SET balance = balance - ? WHERE id = ? AND balance >= ?`）
  2. 本地写入 `gym_payment.t_payment`
  3. Feign 把订单状态改为 `PAID`
     任一步失败 → Seata TC 回滚三个库的 undo\_log。
- 订单状态只有 `0 UNPAID / 1 PAID / 2 SHIPPED / 3 COMPLETED / 4 CANCELLED / 5 REFUNDED`，**没有 PAYING 中间态**。
- 回滚验证专用接口：`POST /api/payment/test/rollback`（传入大于余额的金额即可触发）。

### 8.5 秒杀

```http
GET  /api/seckill/public/activities
GET  /api/seckill/public/goods/{activityId}
POST /api/seckill/order?seckillGoodsId=1
GET  /api/seckill/order/result/{seckillGoodsId}
GET  /api/seckill/order/my
```

- `POST /seckill/order` 返回 `data` 为 **秒杀商品 ID**（不是订单号），异步建单后通过 `GET /seckill/order/result/{seckillGoodsId}` 轮询 `data.order`。
- 重复下单返回「已达限购数量」。

### 8.6 搜索

```http
GET /api/search/goods?keyword=蛋白&page=1&size=10
GET /api/search/suggest?prefix=蛋&size=5
```

### 8.7 其他常用接口

- 商品：`GET/POST/PUT/DELETE /goods/**`、`/goods/category/**`
- 课程：`GET /course/page`、`GET /course/{id}`、`POST /course/schedule/enroll`
  - 报名防重：DB 唯一键 `uk_schedule_user (schedule_id, user_id)` 是唯一事实来源；Redis `course:enroll:*` 只是 3 秒瞬时锁，请求结束即删除，因此取消报名后可立即重新预约。
  - 容量控制：`UPDATE ... SET enrollment_count = enrollment_count + 1 WHERE id = ? AND enrollment_count < capacity`，并发下只有一个请求能成功。
- 会员：`GET /member/card/list`、`POST /member/open`、`GET /member/user/myCards`
- 秒杀管理：`/seckill/admin/activity/**`、`/seckill/admin/goods/**`

***

## 9. 数据与中间件清单

### 9.1 Redis key（代码实际值）

| Key                                            | 类型     | 用途                       |
| :--------------------------------------------- | :----- | :----------------------- |
| `auth:token:access:{token}`                    | String | 登录 access token → userId |
| `auth:token:refresh:{token}`                   | String | refresh token → userId   |
| `seckill:stock:{seckillGoodsId}`               | String | 秒杀库存（启动预热）               |
| `seckill:user:{seckillGoodsId}`                | Hash   | 该商品已购用户（field=userId）    |
| `seckill:soldout:{seckillGoodsId}`             | String | 售罄标记                     |
| `seckill:lock:{userId}:{seckillGoodsId}`       | String | Redisson 防重锁             |
| `course:enroll:{scheduleId}:{userId}`          | String | 课程预约瞬时防重（3s，请求结束即删除）       |
| `pay:lock:{orderNo}`                           | String | 支付防重锁                    |
| `alipay:pay:{orderNo}` / `wxpay:pay:{orderNo}` | String | Mock 预支付链接               |

### 9.2 RabbitMQ

| 交换机                      | 队列                    | 说明                                   |
| :----------------------- | :-------------------- | :----------------------------------- |
| `seckill.order.exchange` | `seckill.order.queue` | 秒杀异步建单                               |
| `seckill.ttl.exchange`   | `seckill.ttl.queue`   | 秒杀 30 分钟 TTL，过期进 `seckill.dlx.queue` |
| `order.ttl.exchange`     | `order.ttl.queue`     | 普通订单 30 分钟 TTL，过期进 `order.dlx.queue` |
| `goods.exchange.fanout`  | `goods.es.sync.queue` | 商品增删改同步 ES                           |

TTL **只由队列级 `x-message-ttl` 提供（1800000ms / 30 分钟，单一来源）**，消息上不再单独设置过期时间，避免「队列级 + 消息级」两个值取最小值导致行为不确定。

### 9.3 秒杀一致性设计

- Lua 按 **商品维度** 限购（`seckill:user:{seckillGoodsId}`），DB 唯一键 `uk_user_goods(user_id, goods_id)`，两边口径一致。
- `SeckillOrderCreateConsumer` 消费流程：
  1. 订单号已存在 → ACK（幂等）
  2. 商品不存在 → 回滚 Redis 后 ACK
  3. **先发 TTL 消息，再在同一本地事务里插订单 + 扣 DB 库存**
  4. 订单号唯一键冲突 → 查询确认是重复投递还是 `uk_user_goods` 冲突；后者回滚本次 Redis 扣减
  5. 其他异常：首次 `requeue=true` 重试一次；重投仍失败则回滚 Redis 并 `NACK(false)` 丢弃，打印包含 orderNo/userId/goodsId 的 ERROR 日志供人工补偿（**不是无限重试**）

***

## 10. 验收自测

| AC                | 步骤                                                                                     | 期望                                                                                                                                                      |
| :---------------- | :------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------ |
| AC-1 启动           | `docker ps` + 按顺序启动 12 服务                                                              | 中间件 healthy；Nacos 12 个服务各 1 实例；8080-8092 监听                                                                                                             |
| AC-2 认证           | 登录拿 token → `GET /api/user/info`；不带 token 再请求                                          | 200 返回用户；无 token 网关 401                                                                                                                                 |
| AC-3 路由           | 带 token 访问 `/api/user/info`、`/api/goods/page`、`/api/course/page`、`/api/order/my`       | 均 200                                                                                                                                                   |
| AC-4 限流           | 压测 `/api/seckill/**` 超过 10 QPS                                                         | 部分请求被 Sentinel 拦截                                                                                                                                       |
| AC-5 秒杀           | `redis-cli GET seckill:stock:1`；200 个不同用户并发 `POST /api/seckill/order?seckillGoodsId=1` | Redis 与 DB 库存一致、无负库存；超卖请求快速失败；`t_seckill_order` 行数 ≤ 库存                                                                                                 |
| **AC-6 Seata 回滚** | ① user1 创建数量 20 的订单（5980 > 余额 5000）② 用 `amount=5980.00` 调 `/api/payment/pay`           | 返回「用户余额不足」；`t_orders.status=0`、`t_user.balance=5000.00`、`t_payment` 无记录；gym-payment 日志出现 `Begin new global transaction` + `rollback status: Rollbacked` |
| AC-7 课程容量         | 容量 20、已报 19 的排期并发报名                                                                    | 仅 1 人成功，`enrollment_count=20`，无重复报名                                                                                                                     |
| AC-8 搜索高亮         | 商品同步后 `GET /api/search/goods?keyword=蛋白`                                               | 命中并带 `<em>` 高亮                                                                                                                                          |
| AC-9 自动补全         | `GET /api/search/suggest?prefix=蛋`                                                     | 返回 ≥3 条补全                                                                                                                                               |
| AC-10 支付策略        | BALANCE / ALIPAY / WECHAT 三种 payType                                                   | 日志打印对应策略，`t_payment.pay_type` 正确                                                                                                                        |
| AC-11 前端          | `npm run dev` 后 admin/123456 登录并操作 6 个页面                                               | 页面与 CRUD 正常，角色菜单隔离                                                                                                                                      |

***

## 11. FAQ

### 11.1 端口冲突

MySQL 3306 / Redis 6379 / RabbitMQ 5672 被占用时，改 `docker-compose.yml` 的映射端口，并同步修改 Nacos 各 `*-dev.yml` 中的连接地址（**不要再改本地 bootstrap.yml**）。

### 11.2 服务启动报数据库不存在

确认使用最新 `sql/init.sql`（8 库）。旧版本只有 4 库，会导致 course/food/member/seckill 启动失败。重建空库最稳妥：

```powershell
docker compose -p gym down
Remove-Item -Recurse -Force docker/mysql/data
docker compose -p gym up -d
```

### 11.3 Seata 报 `can not connect to services-server`

- 查 Nacos 服务列表 `SEATA_GROUP / seata-server` 中的 IP 是否为宿主机可达地址（应为 `192.168.x.x:8091`，不应是 `172.x`）。
- 修改 `SEATA_IP` 后强制重建：

```powershell
SEATA_IP=<宿主机IP> docker compose -p gym up -d --force-recreate seata-server
```

- 确认 Nacos `SEATA_GROUP` 存在 `service.vgroupMapping.default_tx_group`。

### 11.4 Nacos 配置不生效

- `bootstrap.yml` 的 `spring.application.name` 必须与 dataId 前缀一致，profile 必须为 `dev`，后缀 `yml`。
- 读取校验：`http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=gym-user-dev.yml&group=DEFAULT_GROUP`。
- Nacos 控制台账号 `nacos/nacos`。

### 11.5 ES 启动 / 搜索不可用

- Exit 137：内存不足，给 Docker 分配 ≥ 4GB。
- `max virtual memory areas too low`：`wsl -d docker-desktop -u root sysctl -w vm.max_map_count=262144`。
- IK 分词：把 IK 7.17.15 解压到 `docker/es/plugins/ik` 后重启 ES；未安装 IK 时索引创建会失败。
- 健康检查依赖：`elasticsearch:7.17.15` 镜像内**实测自带 `curl`**，其 healthcheck 可直接用 curl；`kibana:7.17.15` 不保证有 curl，已改为 `bash` 的 `/dev/tcp` 端口探测。宿主机排查仍可用 `http://127.0.0.1:9200/_cluster/health`。

### 11.6 秒杀少卖 / 丢单

- 确认 gym-seckill 启动日志中库存预热已执行，`redis-cli GET seckill:stock:1` 有值。
- 消费者失败策略为「首次重投一次，仍失败回滚 Redis 并丢弃」，因此出现 `SeckillOrderCreateConsumer` ERROR 日志时需按日志中的 orderNo/userId/goodsId 人工核对补偿。
- 同一活动多商品场景已由 `uk_user_goods(user_id, goods_id)` 兜底，不会再出现「库存扣了、订单没有」且 Redis 不回滚的问题（唯一键冲突会回滚 Redis）。

***

## 12. 账号一览

| 系统        | 用户名      | 密码       | 地址                            |
| :-------- | :------- | :------- | :---------------------------- |
| 前端 / 接口   | admin    | 123456   | <http://localhost:5173>       |
| 前端 / 接口   | coach    | 123456   | 同上                            |
| 会员前台 / 接口 | user1    | 123456   | <http://localhost:5174>       |
| MySQL     | root     | root     | 127.0.0.1:3306                |
| Nacos     | nacos    | nacos    | <http://127.0.0.1:8848/nacos> |
| RabbitMQ  | guest    | guest    | <http://127.0.0.1:15672>      |
| Sentinel  | sentinel | sentinel | <http://127.0.0.1:8858（可选）>   |
| Seata 控制台 | seata    | seata    | <http://127.0.0.1:7091>       |

***

> 排障入口：各服务控制台日志（启动脚本重定向到 `%TEMP%\gym-logs\gym-xxx.out.log`）与 `docker logs gym-xxx`。

