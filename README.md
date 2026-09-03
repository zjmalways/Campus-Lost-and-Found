# 🏫 校园失物招领平台 — 后端接口文档

完整的校园失物招领平台后端，基于 Spring Boot 3.2.9 + MyBatis-Plus + JWT + 阿里云 OSS 构建。

---

## 一、技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| JDK | 17 | 运行环境 |
| Spring Boot | 3.2.9 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM / 分页 / CRUD |
| MySQL | 8.0+ | 数据库 |
| JJWT | 0.12.6 | JWT 令牌 |
| Aliyun OSS SDK | 3.18.4 | 文件存储（头像/物品图片） |
| springdoc-openapi | 2.6.0 | Swagger 接口文档 |
| Lombok / Validation | - | 简化代码 / 参数校验 |
| AOP | - | 公共字段自动填充 |
| JUC | - | 登录高并发限流 |

---

## 二、项目结构

```
compus-lost-find/
├── pom.xml                          # 父 POM（依赖管理 + 模块声明）
├── compus-lost-find-pojo/           # 实体模块
│   └── com.zhangjiaming
│       ├── common/Result.java       # 统一响应封装
│       ├── context/                 # ErrorContext / MessageContext 常量
│       ├── dto/                     # 请求参数
│       └── entity/                  # 数据库实体
├── compus-lost-find-util/           # 工具模块
│   └── com.zhangjiaming.util
│       ├── JwtUtil.java             # JWT 生成/解析
│       ├── PasswordUtil.java        # SHA-256 + 盐 密码加密
│       ├── ThreadLocalUtil.java     # 请求级用户上下文
│       ├── AliyunOSSUtil.java       # 阿里云 OSS 上传
│       └── LoginRateLimiter.java    # 登录限流（JUC）
└── compus-lost-find-main/           # 主模块
    └── com.zhangjiaming
        ├── annotation/              # @Autofill 注解（含 OperationType 枚举）
        ├── aspect/                  # AutoFillAspect 切面
        ├── config/                  # WebMvc / 异常 / MyBatis-Plus / OpenAPI 配置
        ├── controller/              # REST 控制器
        ├── interceptor/             # JWT 拦截器
        ├── mapper/                  # MyBatis-Plus Mapper
        ├── service/ + impl/         # 业务逻辑
        └── resources/
            ├── application.yml
            └── mapper/              # （已迁移到 MyBatis-Plus，无需 XML）
```

---

## 三、快速启动

### 环境要求

| 环境 | 版本 |
|------|------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |

### 1. 初始化数据库

```bash
mysql -u root -p < compus-lost-find-main/src/sql/init_mysql.sql
```

脚本会创建数据库 `campus_lost_found` 及 4 张表（users / items / comments / announcements），并初始化默认管理员。

> 默认管理员：用户名 `admin`，密码 `admin123`（已按 SHA-256+盐 存储）。

### 2. 配置

编辑 `compus-lost-find-main/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 你的MySQL密码

aliyun:
  oss:
    access-key-id:     你的阿里云 AccessKey ID   # 或设置环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID
    access-key-secret: 你的阿里云 AccessKey Secret
```

### 3. 启动

```bash
mvn clean compile
mvn spring-boot:run
# 服务运行在 http://localhost:8081
```

### 4. Swagger 接口文档

启动后访问：

- Swagger UI：`http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8081/v3/api-docs`

---

## 四、鉴权说明

- 认证方式：JWT（无状态）。登录/注册成功返回 `token`，前端保存到 Cookie（键名 `token`）。
- 后续请求拦截器会依次从 **Cookie `token`** 或 **`Authorization: Bearer <token>`** 头读取并校验。
- 校验通过后把 `userId / username / role` 写入 `ThreadLocal`，请求结束自动清理。
- 退出登录调用 `POST /logout`，后端清除 Cookie。

### 角色权限

| 角色 | 值 | 权限 |
|------|:---:|------|
| 普通用户 | `role=0` | 发布/编辑/删除自己的物品、发表/删除自己的评论、上传头像 |
| 管理员 | `role=1` | 继承普通用户权限 + 管理公告（增删改）、删除任意评论/物品 |

> 设置管理员：`UPDATE users SET role = 1 WHERE username = 'xxx';`

### 登录限流（防暴力破解）

同一用户名连续登录失败 **5 次**后，**锁定 15 分钟**（基于 JUC `ConcurrentHashMap` 原子计数）。

---

## 五、统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

### 状态码

| code | 说明 |
|:----:|------|
| 200 | 成功 |
| 400 | 参数错误 / 校验失败 |
| 401 | 未登录 / token 无效 |
| 403 | 无权限 |
| 500 | 业务错误 / 服务器内部错误 |

### 分页响应结构（MyBatis-Plus `Page`）

```json
{
  "code": 200,
  "data": {
    "records": [ { "...": "..." } ],
    "total": 100,
    "size": 12,
    "current": 1,
    "pages": 9
  }
}
```

> 列表在 `data.records`，总数在 `data.total`。

---

## 六、API 接口文档

Base URL：`http://localhost:8081`（**无 `/api` 前缀**，`/api` 由前端代理处理）

### 认证模块

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:---:|
| POST | `/logout` | 登出（清除 Cookie） | ❌ |

### 用户模块 `/user`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:---:|
| POST | `/user/login` | 登录 | ❌ |
| POST | `/user/register` | 注册 | ❌ |
| GET | `/user/info` | 获取个人信息 | ✅ |
| PUT | `/user/update` | 更新资料 | ✅ |
| PUT | `/user/password` | 修改密码 | ✅ |
| POST | `/user/avatar` | 上传头像到 OSS | ✅ |

**登录** `POST /user/login`

请求：
```json
{ "username": "admin", "password": "admin123" }
```

响应：
```json
{ "code": 200, "message": "success", "data": { "token": "...", "userId": 1, "username": "admin", "nickname": "系统管理员", "role": 1, "avatar": null } }
```

### 物品模块 `/items`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:---:|
| GET | `/items/list` | 分页查询物品列表 | ❌ |
| GET | `/items/detail/{itemId}` | 物品详情 | ❌ |
| POST | `/items/create` | 发布物品 | ✅ |
| PUT | `/items/update` | 编辑物品 | ✅ |
| DELETE | `/items/delete/{itemId}` | 删除物品 | ✅ |
| PUT | `/items/status` | 更新状态（找回/归还） | ✅ |
| GET | `/items/types` | 获取物品分类 | ❌ |
| POST | `/items/upload/image` | 上传单张图片 | ✅ |
| POST | `/items/upload/images` | 批量上传图片 | ✅ |
| PUT | `/items/images/{itemId}` | 更新图片列表 | ✅ |
| DELETE | `/items/upload/image` | 删除 OSS 图片 | ✅ |

**分页查询参数** `GET /items/list`：

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码，默认 1 |
| pageSize | int | 每页条数，默认 12 |
| publishType | int | 0-丢失, 1-捡到 |
| itemType | int | 1-证件 2-钥匙 3-电子设备 4-衣物 5-钱包 6-其他 |
| status | int | 0-未找回, 1-已找回 |
| keyword | String | 关键词（标题/描述/特征/地点） |
| publisherId | Long | 按发布者筛选 |

### 评论模块 `/comments`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:---:|
| GET | `/comments/item/{itemId}` | 获取评论列表 | ❌ |
| POST | `/comments/create` | 发表评论 | ✅ |
| DELETE | `/comments/delete/{commentId}` | 删除评论（管理员及本人） | ✅ |

### 公告模块 `/announcements`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:---:|
| GET | `/announcements/list` | 公告列表 | ❌ |
| GET | `/announcements/detail/{id}` | 公告详情 | ❌ |
| POST | `/announcements/create` | 发布公告（管理员） | ✅ |
| PUT | `/announcements/update` | 编辑公告（管理员） | ✅ |
| DELETE | `/announcements/delete/{id}` | 删除公告（管理员） | ✅ |

---

## 七、常见问题

- **`Access key id should not be null or empty`**：OSS 未配置，填写 yml 的 access-key-id/secret 或设置环境变量 `ALIBABA_CLOUD_ACCESS_KEY_ID/SECRET`（未配置时服务照常启动，仅上传接口返回友好提示）。
- **登录提示「用户名或密码错误」**：先注册，或确认账号未被禁用/未触发登录限流锁定。
- **接口返回 401**：token 缺失或过期，重新登录。
- **物品/评论/公告表不存在**：重新执行 `init_mysql.sql`。
