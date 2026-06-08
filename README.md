# 灵境景区数字人

基于 Live2D 交互的智慧景区数字人系统，结合大语言模型为游客提供智能导览服务。

## 项目结构

```
├── lingjing-frontend/          # 前端项目（Vite + Vue 3）
│   ├── src/
│   │   ├── api/                # API 请求层
│   │   ├── components/         # 公共组件
│   │   ├── composables/        # 组合式函数
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # 状态管理
│   │   ├── views/              # 页面视图
│   │   └── assets/             # 静态资源
│   └── package.json
│
├── LingJingDigitalHuman/       # 后端项目（Spring Boot）
│   ├── src/main/java/
│   │   └── cn/edu/nuc/digitalhuman/
│   │       ├── admin/          # 管理员模块
│   │       ├── auth/           # 认证模块
│   │       ├── attraction/     # 景点模块
│   │       ├── chat/           # 聊天模块
│   │       ├── checkin/        # 签到模块
│   │       ├── digitalhuman/   # 数字人模块
│   │       ├── kb/             # 知识库模块
│   │       ├── llm/            # 大语言模型模块
│   │       └── ...             # 其他模块
│   └── pom.xml
│
└── fix_coordinates.sql         # 坐标修复脚本
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 数字人渲染 | PixiJS + Live2D |
| 图表 | ECharts |
| 后端框架 | Spring Boot + Maven |
| 数据库 | MySQL |
| AI | 大语言模型 / DashScope TTS |

## 快速开始

### 前端

```bash
cd lingjing-frontend
npm install
npm run dev
```

### 后端

```bash
cd LingJingDigitalHuman
mvn spring-boot:run
```

## 功能概览

- 🎭 **Live2D 数字人交互** — 支持表情切换、口型同步
- 💬 **智能对话** — 接入大语言模型，回答景区相关问题
- 🗺️ **景点导览** — 景区信息查询与推荐
- 📊 **数据看板** — 后台数据统计与分析
- 🔐 **用户认证** — 登录注册与权限管理

## 许可证

本项目仅用于学习交流。
