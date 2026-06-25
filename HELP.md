# 🏫 校园失物招领平台 — 项目文档

## 📋 项目简介

基于 Spring Boot + MyBatis + PageHelper + JWT 的校园失物招领平台后端项目。提供用户注册登录、失物/招领物品发布、评论互动、公告管理等功能。

## 🚀 快速启动

### 环境要求

| 环境 | 版本 |
|------|------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |

### 启动步骤

```bash
# 1. 进入项目目录
cd compus-lost-find

# 2. 初始化数据库（使用 MySQL 客户端执行）
mysql -u root -p < src/sql/init_mysql.sql

# 3. 修改数据库连接配置
#    编辑 src/main/resources/application.yml：
#    - spring.datasource.username → 你的MySQL用户名
#    - spring.datasource.password → 你的MySQL密码

# 4. 编译 & 启动
mvn spring-boot:run
```

启动成功后访问：`http://localhost:8081`

---

## 🧱 项目结构

```
compus-lost-find/
├── pom.xml                              # Maven 依赖配置
├── HELP.md                              # ← 本文件
├── src/
│   ├── sql/
│   │   └── init_mysql.sql               # 数据库建表 + 初始化数据
│   └── main/
│       ├── java/org/zhangjiamin/
│       │   ├── CompusLostFindApplication.java  # 启动类
│       │   ├── common/Result.java              # 统一响应体
│       │   ├── config/
│       │   │   ├── WebMvcConfig.java           # CORS + 拦截器配置
│       │   │   └── GlobalExceptionHandler.java # 全局异常处理
│       │   ├── controller/                     # REST API 控制器
│       │   │   ├── UserController.java
│       │   │   ├── ItemController.java
│       │   │   ├── CommentController.java
│       │   │   └── AnnouncementController.java
│       │   ├── dto/                            # 请求参数封装
│       │   ├── entity/                         # 数据库实体
│       │   │   ├── User.java
│       │   │   ├── Item.java
│       │   │   ├── Comment.java
│       │   │   └── Announcement.java
│       │   ├── interceptor/
│       │   │   └── JwtInterceptor.java         # JWT 登录校验
│       │   ├── mapper/                         # MyBatis 接口
│       │   ├── service/ + impl/                # 业务逻辑层
│       │   └── util/
│       │       ├── JwtUtil.java                # JWT 令牌工具
│       │       ├── PasswordUtil.java           # 密码加密工具
│       │       └── ThreadLocalUtil.java        # 用户上下文
│       └── resources/
│           ├── application.yml                 # 应用配置
│           └── mapper/                         # MyBatis XML
```

---

## 🔧 技术栈

| 技术 | 用途 |
|------|------|
| Spring Boot 3.2.x | 框架基础 |
| MyBatis + Spring Boot Starter 3.0.3 | 数据持久化 |
| PageHelper 2.1.0 | 分页插件 |
| JJWT 0.12.6 | JWT 令牌生成/解析 |
| MySQL Connector | 数据库驱动 |
| Lombok | 简化实体类 |
| Spring Validation | 参数校验 |

---

## 📡 API 接口文档

### 基础信息

- **Base URL**: `http://localhost:8081/api`
- **鉴权方式**: `Authorization: Bearer <token>`
- **响应格式**:

```json
{
    "code": 200,        // 200=成功, 401=未登录, 403=无权限, 500=服务器错误
    "message": "success",
    "data": { ... }
}
```

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | `/api/user/login` | 登录，返回JWT Token | ❌ |
| POST | `/api/user/register` | 注册新用户 | ❌ |
| GET | `/api/user/info` | 获取当前用户信息 | ✅ |
| PUT | `/api/user/update` | 更新个人信息 | ✅ |
| PUT | `/api/user/password` | 修改密码 | ✅ |

**登录请求体：**
```json
{ "username": "test001", "password": "123456" }
```

**登录成功响应：**
```json
{
    "code": 200,
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 2,
        "username": "test001",
        "nickname": "测试用户",
        "role": 0,
        "avatar": null
    }
}
```

### 物品模块 `/api/items`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| GET | `/api/items/list` | 分页查询物品列表 | ❌ |
| GET | `/api/items/detail/{itemId}` | 物品详情 | ❌ |
| POST | `/api/items/create` | 发布失物/招领 | ✅ |
| PUT | `/api/items/update` | 编辑物品信息 | ✅ |
| DELETE | `/api/items/delete/{itemId}` | 删除物品 | ✅ |
| PUT | `/api/items/status` | 更新状态(已找回/已归还) | ✅ |
| GET | `/api/items/types` | 获取物品分类列表 | ❌ |

**分页查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码，默认1 |
| pageSize | int | 每页条数，默认10 |
| publishType | int | 发布类型：0-丢失, 1-捡到 |
| itemType | int | 物品分类：1-证件, 2-钥匙, 3-电子设备, 4-衣物, 5-钱包, 6-其他 |
| status | int | 状态：0-未找回, 1-已找回 |
| keyword | String | 关键词(标题/描述/特征) |
| publisherId | Long | 发布者ID |

**发布物品请求体：**
```json
{
    "itemType": 3,
    "publishType": 0,
    "title": "丢失黑色华为手机",
    "description": "今早在图书馆二楼丢失黑色华为Mate60手机",
    "features": "黑色手机壳，背面贴有贴纸",
    "images": "",
    "location": "图书馆二楼自习区",
    "eventTime": "2026-06-25T08:30:00",
    "contact": "13800138000",
    "storageLocation": ""
}
```

**更新状态请求体：**
```json
{ "itemId": 1, "status": 1 }
```

### 评论模块 `/api/comments`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| GET | `/api/comments/item/{itemId}` | 获取物品的所有评论 | ❌ |
| POST | `/api/comments/create` | 发表评论 | ✅ |
| DELETE | `/api/comments/delete/{commentId}` | 删除评论 | ✅ |

**发表评论请求体：**
```json
{
    "itemId": 1,
    "content": "我在二楼看到过类似的手机，请联系我",
    "parentId": 0
}
```

### 公告模块 `/api/announcements`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| GET | `/api/announcements/list` | 获取公告列表 | ❌ |
| GET | `/api/announcements/detail/{id}` | 公告详情 | ❌ |
| POST | `/api/announcements/create` | 发布公告（管理员） | ✅ |
| PUT | `/api/announcements/update` | 编辑公告（管理员） | ✅ |
| DELETE | `/api/announcements/delete/{id}` | 删除公告（管理员） | ✅ |

---

## 🔐 权限说明

| 角色 | 值 | 说明 |
|------|----|------|
| 普通用户 | `role=0` | 可发布物品、评论、编辑自己的内容 |
| 管理员 | `role=1` | 继承普通用户权限 + 管理公告、管理所有物品 |

- 注册时默认角色为 `0`（普通用户）
- 若要设置为管理员，需直接在数据库中修改 `users.role = 1`
- 初始化 SQL 已创建一个管理员账号：`admin` / `admin123`（但密码是明文，建议登录后修改或直接通过注册接口建新号后修改数据库）

---

## ⚙️ 配置文件说明

### `application.yml` 关键配置项

```yaml
server:
  port: 8081                                  # 服务端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found
    username: root                             # ← 改为你的MySQL用户名
    password: 123456                           # ← 改为你的MySQL密码

jwt:
  secret: campus-lost-found-secret-key-...     # JWT签名密钥（建议修改）
  expiration: 86400000                         # Token有效期（毫秒，默认24h）
```

---

## 🧪 使用 Apifox 测试

### 环境变量配置

| 变量名 | 初始值 |
|--------|--------|
| `base_url` | `http://localhost:8081` |
| `token` | （登录后自动填充） |

### 登录接口后自动提取 Token

在登录接口的「后操作」脚本中添加：

```javascript
pm.environment.set("token", pm.response.json().data.token);
```

后续接口 Header 统一填写：`Authorization: Bearer {{token}}`

---

## ⚠️ 常见问题

### Q: 启动报错 `Access denied for user 'root'@'localhost'`
> 检查 `application.yml` 中的数据库用户名和密码是否正确。

### Q: 登录提示 `用户名或密码错误`
> - 确认用户已注册（调用 `/api/user/register` 注册）
> - 初始化 SQL 中的 `admin` 密码是明文，不兼容密码SHA-256加密格式，建议通过注册接口创建新账号

### Q: 接口返回 `401 未登录`
> - 确认请求头中携带了 `Authorization: Bearer <token>`
> - Token 已过期（默认24小时），重新登录获取新 Token

### Q: 前端跨域问题
> 后端已配置 CORS，全局允许跨域访问。确认 `WebMvcConfig.java` 中的 CORS 配置已启用。

### Q: PageHelper 分页不生效
> 检查 `pageNum` 和 `pageSize` 参数名是否正确，PageHelper 默认从第1页开始。

---

## 📦 Maven 命令速查

```bash
mvn compile              # 编译
mvn test                 # 运行测试
mvn package              # 打包成 JAR
mvn spring-boot:run      # 启动开发服务器
mvn clean                # 清理 target
```

打包后的 JAR 文件位于 `target/compus-lost-find-0.0.1-SNAPSHOT.jar`，可直接运行：

```bash
java -jar target/compus-lost-find-0.0.1-SNAPSHOT.jar
```
