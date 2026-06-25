# 🏫 校园失物招领平台 — 项目文档

## 📋 项目简介

完整的校园失物招领平台，包含 **Spring Boot 后端**（多模块）和 **Vue 3 前端**（Vite + Element Plus）。

- 后端：Spring Boot 3.2 + MyBatis + PageHelper + JWT + 阿里云 OSS
- 前端：Vue 3 + Pinia + Vue Router + Element Plus + Axios

---

## 🚀 快速启动

### 环境要求

| 环境 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端编译运行 |
| MySQL | 8.0+ | 数据库 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18+ | 前端构建 |

### 1️⃣ 初始化数据库

```bash
# 创建数据库和表
mysql -u root -p < compus-lost-find/src/sql/init_mysql.sql

# （可选）执行图片字段迁移
mysql -u root -p < compus-lost-find/src/sql/migration_v2_images.sql
```

### 2️⃣ 配置后端

编辑 `compus-lost-find/compus-lost-find-main/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: root
    password: 你的MySQL密码

aliyun:
  oss:
    access-key-id:    你的阿里云AccessKey ID     # ← 必填
    access-key-secret: 你的阿里云AccessKey Secret  # ← 必填
```

> 💡 AccessKey 从 [阿里云 RAM 控制台](https://ram.console.aliyun.com) 获取

### 3️⃣ 启动后端

```bash
cd compus-lost-find
mvn spring-boot:run
# 启动后服务在 http://localhost:8081
```

### 4️⃣ 启动前端

```bash
cd compus-lost-find-vue
npm install
npm run dev
# 启动后访问 http://localhost:5173
```

---

## 🧱 项目结构

```
compus-lost-find/                          ← 后端工程（Maven多模块）
├── pom.xml                                # 父POM（统一依赖管理）
├── HELP.md                                # ← 本文件
├── src/sql/
│   ├── init_mysql.sql                     # 数据库建表 + 初始化数据
│   └── migration_v2_images.sql            # 图片字段迁移脚本
│
├── compus-lost-find-util/                 # 工具模块
│   └── src/main/java/.../util/
│       ├── AliyunOSSUtil.java             # 阿里云 OSS 文件上传工具
│       ├── JwtUtil.java                   # JWT 令牌生成/解析
│       ├── PasswordUtil.java              # SHA-256 加盐密码加密
│       └── ThreadLocalUtil.java           # 请求上下文（当前用户）
│
├── compus-lost-find-pojo/                 # 实体模块
│   ├── entity/                            # 数据库实体
│   │   ├── User.java
│   │   ├── Item.java                      # 物品（含 images/imageList 图片字段）
│   │   ├── Comment.java
│   │   └── Announcement.java
│   ├── dto/                               # 请求参数
│   └── common/Result.java                 # 统一响应封装
│
└── compus-lost-find-main/                 # 主模块（启动类 + 业务代码）
    ├── CompusLostFindApplication.java     # 启动类
    ├── config/
    │   ├── WebMvcConfig.java              # CORS + JWT拦截器 + 文件上传
    │   └── GlobalExceptionHandler.java    # 全局异常处理
    ├── controller/                        # REST API 控制器
    ├── interceptor/
    │   └── JwtInterceptor.java            # JWT 请求校验
    ├── mapper/                            # MyBatis 接口
    ├── service/ + impl/                   # 业务逻辑
    └── resources/
        ├── application.yml                # 应用配置
        ├── org/zhangjiamin/mapper/        # MyBatis Mapper XML
        └── mapper/                        # ↓ 遗留目录


compus-lost-find-vue/                      ← 前端工程（Vite + Vue 3）
├── package.json
├── vite.config.js                         # Vite配置（含 /api 代理到 8081）
├── index.html
└── src/
    ├── main.js                            # 应用入口
    ├── App.vue
    ├── style.css                          # 全局样式
    ├── api/                               # Axios 请求封装
    │   ├── request.js                     # 请求/响应拦截器（Token注入 + 错误提示）
    │   ├── user.js                        # 用户 + 头像上传
    │   ├── item.js                        # 物品 + 图片上传
    │   ├── comment.js
    │   └── announcement.js
    ├── router/index.js                    # 路由配置（含登录守卫）
    ├── stores/user.js                     # Pinia 用户状态
    ├── utils/
    │   ├── auth.js                        # Token/用户本地存储
    │   └── constants.js                   # 常量定义
    └── views/
        ├── layout/MainLayout.vue          # 主布局（导航栏 + 用户头像）
        ├── home/HomePage.vue              # 首页（轮播公告 + 快捷操作 + 最新发布）
        ├── item/
        │   ├── ItemList.vue               # 物品列表（筛选+分页+缩略图）
        │   ├── ItemDetail.vue             # 详情（图片画廊+评论+操作）
        │   └── ItemPublish.vue            # 发布/编辑（图片上传预览）
        ├── user/
        │   ├── Login.vue / Register.vue   # 登录/注册
        │   └── Profile.vue                # 个人中心（头像上传+资料+密码+我的发布）
        └── announcement/
            └── AnnouncementList.vue       # 公告列表
```

---

## 🔧 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.0 | 框架基础 |
| MyBatis | 3.0.3 | 数据持久化 |
| PageHelper | 2.1.0 | 分页插件 |
| JJWT | 0.12.6 | JWT 令牌 |
| Aliyun OSS SDK | 3.18.4 | 文件存储（头像/物品图片） |
| MySQL Connector | 8.x | 数据库驱动 |
| Lombok | - | 简化代码 |
| Validation | - | 参数校验 |

### 前端

| 技术 | 用途 |
|------|------|
| Vue 3 + Vite | 框架 + 构建 |
| Pinia | 状态管理 |
| Vue Router 4 | 路由 |
| Axios | HTTP 请求 |
| Element Plus | UI 组件库 |

---

## 📡 API 接口文档

### 基础信息

- **Base URL**: `http://localhost:8081/api`
- **鉴权方式**: `Authorization: Bearer <token>`
- **响应格式**:

```json
{ "code": 200, "message": "success", "data": { ... } }
```

### 用户模块 `/api/user`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| POST | `/api/user/login` | 登录 | ❌ |
| POST | `/api/user/register` | 注册 | ❌ |
| GET | `/api/user/info` | 获取个人信息 | ✅ |
| PUT | `/api/user/update` | 更新资料 | ✅ |
| PUT | `/api/user/password` | 修改密码 | ✅ |
| **POST** | **`/api/user/avatar`** | **上传头像到 OSS** | ✅ |

### 物品模块 `/api/items`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | `/api/items/list` | 分页查询物品列表 | ❌ |
| GET | `/api/items/detail/{id}` | 物品详情 | ❌ |
| POST | `/api/items/create` | 发布物品 | ✅ |
| PUT | `/api/items/update` | 编辑物品 | ✅ |
| DELETE | `/api/items/delete/{id}` | 删除物品 | ✅ |
| PUT | `/api/items/status` | 更新状态（找回/归还） | ✅ |
| GET | `/api/items/types` | 获取物品分类 | ❌ |
| **POST** | **`/api/items/upload/image`** | **上传单张物品图片** | ✅ |
| **POST** | **`/api/items/upload/images`** | **批量上传物品图片** | ✅ |
| **PUT** | **`/api/items/images/{itemId}`** | **更新物品图片列表** | ✅ |
| **DELETE** | **`/api/items/upload/image`** | **删除 OSS 图片** | ✅ |

**分页查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| pageNum | int | 页码，默认1 |
| pageSize | int | 每页条数，默认12 |
| publishType | int | 0-丢失, 1-捡到 |
| itemType | int | 1-证件 2-钥匙 3-电子设备 4-衣物 5-钱包 6-其他 |
| status | int | 0-未找回, 1-已找回 |
| keyword | String | 关键词搜索（标题/描述/特征/地点） |
| publisherId | Long | 按发布者筛选 |

### 评论模块 `/api/comments`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | `/api/comments/item/{itemId}` | 获取评论列表 | ❌ |
| POST | `/api/comments/create` | 发表评论 | ✅ |
| DELETE | `/api/comments/delete/{id}` | 删除评论 | ✅ |

### 公告模块 `/api/announcements`

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|:------:|
| GET | `/api/announcements/list` | 公告列表 | ❌ |
| GET | `/api/announcements/detail/{id}` | 公告详情 | ❌ |
| POST | `/api/announcements/create` | 发布公告（管理员） | ✅ |
| PUT | `/api/announcements/update` | 编辑公告（管理员） | ✅ |
| DELETE | `/api/announcements/delete/{id}` | 删除公告（管理员） | ✅ |

---

## 🖼️ 图片上传说明

### 存储架构

```
阿里云 OSS Bucket: compus-lost-find
 ├── avatar/               ← 用户头像
 │   └── {uuid}.jpg
 └── images/               ← 物品图片
     └── {uuid}.jpg
```

### 头像上传流程

```
Profile.vue → 点击头像 → 选择图片
    → POST /api/user/avatar (multipart)
    → AliyunOSSUtil.uploadAvatar()
    → OSS URL → 写入 users.avatar
    → 前端即时刷新（页面 + 导航栏）
```

### 物品图片上传流程

```
ItemPublish.vue → 选择多张图片
    → 逐张 POST /api/items/upload/image (multipart)
    → 显示缩略图预览（可删除）
    → 提交表单时:
        imageUrls → ['url1','url2']
        → items.images = 'url1,url2'           (逗号分隔)
        → items.image_list = '["url1","url2"]'  (JSON数组)
```

### OSS 凭证配置

支持两种方式（按优先级）：

```
方式A（推荐开发用）— application.yml:
  aliyun.oss.access-key-id: LTAI5t...
  aliyun.oss.access-key-secret: xcR...

方式B（推荐生产用）— 环境变量:
  OSS_ACCESS_KEY_ID
  OSS_ACCESS_KEY_SECRET
```

---

## 🔐 权限说明

| 角色 | 值 | 权限 |
|------|:---:|------|
| 普通用户 | `role=0` | 发布/编辑/删除自己的物品、发表/删除自己的评论、上传头像 |
| 管理员 | `role=1` | 继承普通用户权限 + 管理公告（CRUD）、管理所有物品 |

注册时默认 `role=0`。设置管理员需直接在数据库修改：`UPDATE users SET role = 1 WHERE username = 'xxx'`

---

## ⚙️ 配置文件详解

### `application.yml` 关键配置

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_lost_found
    username: root
    password: 你的密码
  servlet:
    multipart:
      max-file-size: 5MB           # 单文件最大 5MB
      max-request-size: 10MB       # 请求最大 10MB

jwt:
  secret: campus-lost-found-secret-key-...   # JWT 签名密钥
  expiration: 86400000                       # Token 有效期 24h

aliyun:
  oss:
    endpoint: https://oss-cn-beijing.aliyuncs.com
    bucket-name: compus-lost-find
    region: cn-beijing
    access-key-id: YOUR_AK           # ← 填入你的
    access-key-secret: YOUR_SK       # ← 填入你的
```

### 前端 `vite.config.js` 代理

```js
server: {
  port: 5173,
  proxy: {
    '/api': { target: 'http://localhost:8081', changeOrigin: true }
  }
}
```

开发时前端 `5173` → 自动代理到后端 `8081`，无需处理跨域。

---

## 📦 Maven 命令速查

```bash
cd compus-lost-find

mvn compile                  # 编译全部模块
mvn clean                    # 清理 target
mvn spring-boot:run          # 启动后端（热部署）
mvn package -DskipTests      # 打包 JAR

# 打包后的 JAR
java -jar compus-lost-find-main/target/compus-lost-find-main-0.0.1-SNAPSHOT.jar
```

## 📦 npm 命令速查

```bash
cd compus-lost-find-vue

npm install                  # 安装依赖
npm run dev                  # 启动开发服务器（热更新）
npm run build                # 生产构建（输出到 dist/）
npm run preview              # 预览生产构建
```

---

## 🧪 Apifox 测试指南

### 环境变量

| 变量 | 初始值 |
|------|--------|
| `base_url` | `http://localhost:8081` |
| `token` | （登录后自动填充） |

### 登录后自动提取 Token

在登录接口「后操作」脚本中：

```javascript
pm.environment.set("token", pm.response.json().data.token);
```

其他接口 Header：`Authorization: Bearer {{token}}`

---

## ⚠️ 常见问题

### Q: `Access key id should not be null or empty`
> `application.yml` 中没有配置 `aliyun.oss.access-key-id` / `access-key-secret`，也没有设置 `OSS_ACCESS_KEY_ID` 环境变量。二选一填写即可。

### Q: 登录提示 `用户名或密码错误`
> - 先注册再登录（`POST /api/user/register`）
> - 初始化脚本的 `admin` 密码是明文，不兼容 SHA-256 加密

### Q: 接口返回 `401 未登录`
> - 请求头需携带 `Authorization: Bearer <token>`
> - Token 过期（默认24h），重新登录

### Q: 图片上传失败
> - 检查 OSS 配置是否正确（endpoint / bucket / accessKey）
> - 图片不超过 5MB
> - 查看后端日志中的 OSS 异常详情

### Q: 前端跨域
> Vite 已配置 `/api` 代理到后端，开发时无跨域问题。生产环境需用 nginx 反向代理或后端已配置的 CORS。

### Q: PageHelper 分页不生效
> 确保参数名是 `pageNum` 和 `pageSize`，PageHelper 从第1页开始。

---

## 📄 数据库字段说明

### items — 物品图片字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `images` | TEXT | 逗号分隔的图片URL历史字段 |
| `image_list` | JSON | 图片URL JSON数组（推荐使用） |

> 前端优先解析 `image_list`，不存在时降级到 `images`。
