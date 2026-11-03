# 旅游服务平台

> 基于 Spring Boot 3 + Vue 3 + uni-app 的三端旅游服务平台（毕业设计）

## 项目简介

面向 C 端用户、B 端商家、平台运营三方，集酒店 / 景点 / 餐饮 **一站式搜索与预订** 于一体的旅游服务平台。覆盖用户浏览下单、商家经营管理、平台审核监管完整业务闭环，并接入大模型实现智能旅行助手与个性化推荐。

## 技术栈

| 层级 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 3.2 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 5.7+（17 张数据表） |
| 缓存 | Redis（读穿缓存 + 旁路失效） |
| 鉴权 | JWT + BCrypt 密码加密 |
| 大模型 | DeepSeek（OpenAI 兼容接口，SSE 流式输出） |
| 管理端 / 商家端 | Vue 3 + Element Plus + Pinia + Vue Router + Axios + ECharts |
| 用户端 | uni-app（一套代码兼容 H5 / 微信小程序 / App） |
| 构建工具 | Maven / Vite |

## 功能模块

### 用户端（uni-app，20 个页面）
- 首页：轮播图、公告、推荐酒店/景点/餐厅、个性化推荐「猜你喜欢」
- 浏览搜索：酒店 / 景点 / 餐厅列表、详情、筛选
- 预订下单：酒店房型预订、景点门票预订、订单管理、模拟支付
- 互动：评价发布与查看、收藏管理
- **AI 旅行助手**：自然语言对话、意图识别、行程规划、流式输出

### 商家端（Vue 3 Web）
- 店铺档案维护、酒店 / 餐厅信息维护与上下架申请
- 房型 / 菜品管理
- 订单处理、评价回复与申诉
- 经营数据统计概览

### 管理端（Vue 3 Web）
- 商家入驻审核、信息审核（酒店 / 景点 / 餐厅 / 评价 / 申诉）
- 用户与商家管理、分类管理
- 轮播图 / 公告内容管理
- 平台运营数据看板（ECharts 可视化）

## 核心技术亮点

### 1. AI 旅行助手（大模型接入）
设计「**意图解析（JSON 结构化输出）→ 本地关键词召回 → 回复生成**」三段式流水线：
- 大模型识别用户意图（推荐 / 行程规划 / 闲聊）并提取结构化关键词
- 关键词在本地数据库做召回打分，取 Top3 真实资源
- 结合对话历史与召回结果生成自然语言回复，附带可点击推荐卡片
- 支持 SSE 流式输出实现打字机效果，多轮对话携带最近 6 条历史
- 多级异常兜底：意图识别失败 → 按闲聊处理；大模型超时 → 固定文案，不阻塞主流程

### 2. 三级混合个性化推荐
| 级别 | 策略 | 适用场景 |
|------|------|---------|
| 第一级 | Item-CF 协同过滤（基于收藏/下单行为共现） | 用户有历史行为 |
| 第二级 | 同分类 / 同同城内容匹配 | 协同过滤召回不足 |
| 第三级 | 热门兜底（收藏数 × 评分排序） | 新用户冷启动 |

推荐结果按用户缓存于 Redis（5 分钟），收藏 / 下单行为变化时主动失效；每条推荐附带可解释理由。

### 3. Redis 缓存（Cache Aside 旁路模式）
- **读穿缓存**：查缓存 → 命中返回；未命中查库 → 写缓存（带 TTL）→ 返回
- **写失效**：更新数据库后由 `CacheEvictor` 统一按前缀批量删除相关缓存
- **防穿透**：空结果也缓存，避免不存在的数据反复打库
- **降级容错**：Redis 宕机自动回源数据库，缓存故障不影响主流程

### 4. 并发库存控制（乐观锁）
采用原子 SQL 乐观扣减，避免「先查再改」的竞态条件：
```sql
UPDATE biz_hotel_room
SET remaining_count = remaining_count - #{count}
WHERE id = #{id} AND remaining_count >= #{count}
```
影响行数为 0 即库存不足，事务内保证订单与库存一致。

### 5. 三端权限隔离
三张独立账号表（用户 / 商家 / 管理员），登录后签发 JWT（内含角色声明），后端拦截器按 URL 前缀做接口级权限控制：`/api/user/**`、`/api/merchant/**`、`/api/admin/**`。

## 项目结构

```
Travel-platform/
├── travel-platform-backend/     # 后端 Spring Boot
│   └── src/main/java/com/travel/
│       ├── common/               # 统一返回、全局异常、配置、工具类
│       ├── interceptor/          # JWT 权限拦截器
│       ├── controller/          # 按端分包：open / user / merchant / admin
│       ├── service/             # 业务逻辑（含 AI 服务、推荐服务）
│       ├── mapper/              # MyBatis-Plus Mapper
│       └── entity / dto / vo/   # 数据对象
├── admin-web/                   # 管理端 Vue 3
├── merchant-web/                # 商家端 Vue 3
├── user-app/                    # 用户端 uni-app
└── sql/                         # 数据库初始化脚本
```

## 本地启动

### 环境要求
- JDK 17+
- MySQL 5.7+
- Redis
- Node.js 16+
- HBuilderX（运行 uni-app 用户端）

### 后端启动
1. 创建数据库 `travel_platform`，执行 `sql/init.sql` 初始化表结构与测试数据
2. 启动 Redis
3. 在环境变量中配置：
   - `DB_PASSWORD`：MySQL 密码
   - `JWT_SECRET`：JWT 密钥（至少 32 字节）
   - `DEEPSEEK_API_KEY`：DeepSeek API Key
4. 运行 `TravelApplication.java`，后端默认端口 8080

### 前端启动
```bash
# 管理端 / 商家端
cd admin-web    # 或 merchant-web
npm install
npm run dev

# 用户端（uni-app）
# 用 HBuilderX 打开 user-app 目录，选择运行到 H5 / 微信开发者工具
```

## 数据库设计

共 17 张表，核心设计：
- **统一订单表 + 明细表**：通过 `order_type` 区分酒店 / 景点订单，便于后续扩展
- **多态评价 / 审核**：`target_type + target_id` 一张表覆盖三类业务
- **逻辑删除**：用户表等业务表带 `deleted` 字段，软删除保留数据

---

**作者**：吴世楠 · 大连外国语大学 软件学院
