<p align="center">
  <h1 align="center">✨ GlowSkin - AI 智能肤质检测与美肤顾问</h1>
  <p align="center">
    <b>基于 Kotlin + Jetpack Compose + Google Gemini AI 构建的原生 Android 美肤全栈应用</b>
  </p>
  <p align="center">
    <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
    <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose" /></a>
    <a href="https://ai.google.dev/"><img src="https://img.shields.io/badge/AI-Gemini%202.5%20Flash-8E44AD?style=for-the-badge&logo=google&logoColor=white" alt="Gemini AI" /></a>
    <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Database-Room-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB" /></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache--2.0-blue?style=for-the-badge" alt="License" /></a>
  </p>
</p>

---

## 📖 目录 (Table of Contents)

- [💡 简介与核心理念](#-简介与核心理念)
- [📌 GitHub 仓库配置参考](#-github-仓库配置参考)
- [✨ 核心功能模块](#-核心功能模块)
- [🏗️ 技术架构与设计模式](#️-技术架构与设计模式)
- [🎨 视觉与 UI 设计规范](#-视觉与-ui-设计规范)
- [📂 项目目录结构](#-项目目录结构)
- [🛠️ 快速编译与运行指南](#️-快速编译与运行指南)
- [🗺️ 项目演进路线图 (Roadmap)](#️-项目演进路线图-roadmap)
- [🤝 贡献指南 (Contributing)](#-贡献指南-contributing)
- [📄 开源协议 (License)](#-开源协议-license)

---

## 💡 简介与核心理念

**GlowSkin** 是一款专为 Android 平台打造的智能美肤健康管理应用。应用结合了 **端侧/云侧图像分析** 与 **Google Gemini 2.5 Flash 大语言模型**，为用户提供科学、精准、个性化的护肤方案。

针对用户在日常护肤中的核心痛点（如：盲目种草烂脸、不了解自身肤质变化、成分看不懂、护肤步骤混乱），GlowSkin 提供了**全链路闭环解决方案**：
从 **智能测肤诊断** ➔ **AI 护肤顾问定制 Routine** ➔ **成分库避雷匹配** ➔ **Before & After 改善对比** ➔ **云端多端同步**。

---

## 📌 GitHub 仓库配置参考

在 GitHub 仓库首页右侧点击 **Edit repository details** 进行如下规范化配置：

- **Description (项目描述)**:
  `✨ 原生 Android 智能肤质检测与美肤顾问 | Jetpack Compose + Gemini 2.5 AI + Room 数据库 + MAD 现代架构`
- **Website (在线 Demo / 落地页)**:
  `https://ais-pre-airowa3nmhomspysfkdvio-397008035337.us-east5.run.app`
- **Topics (标签分类)**:
  `android`, `kotlin`, `jetpack-compose`, `gemini-api`, `skincare-app`, `ai-skin-analysis`, `room-database`, `material-design-3`, `open-source`, `mvvm`

---

## ✨ 核心功能模块

### 1. 📸 智能肤质检测与多维诊断 (AI Skin Detection)
- **多维度科学打分**: 精确测量水油平衡值 (Moisture/Oil %)、敏感泛红指数及综合美肤得分 (Overall Score)。
- **细粒度皮肤问题分类**: 黑头粉刺、青春痘/毛孔粗大、细纹干纹、暗沉黑眼圈、皮肤屏障受损等。
- **双模检测引擎**:
  - **相机/图片 AI 诊断**: 结合光学辅助线与图像特征进行快速打分。
  - **10 题皮肤科问诊问卷**: 涵盖出油区域、洗脸紧绷感、过敏历史与护理习惯，自适应切题流畅体验。

### 2. 💬 Gemini AI 护肤顾问 & 早晚 Routine 方案 (AI Consultant)
- **实时对话式解答**: 接入 Gemini 大模型，解答成分禁忌（如 A 醇与酸类搭配）、敏感期急救与防晒知识。
- **动态 Routine 生成**: 根据最新检测得分，一键定制晨间 (AM) 清洁-水乳-防晒与夜间 (PM) 卸妆-精华-修护步骤。

### 3. 🧪 护肤品推荐与成分库避雷匹配 (Ingredient Matcher)
- **成分安全性评估**: 深度解析护肤品成分，标注致痘率、致敏风险及刺激性指数。
- **肤质匹配度智能判定**: 自动校验产品是否适合“敏感肌/油痘肌/干皮屏障受损”，防止盲目使用导致的肌肤负担。

### 4. 📈 肤质演变趋势与 Before & After 对比 (Analytics & Compare)
- **水油与得分折线图**: 本地平滑绘制历史检测数据曲线，直观呈现皮肤改善轨迹。
- **双排 Before & After 深度对比**: 允许挑选任意两次历史测肤，对比各项指标的增减幅度（如：水分 +15% ↗，敏感度 -8% 屏障变稳）。

### 5. 🔐 多渠道账号体系与打卡打卡 (Auth & Security)
- **多渠道快捷登录**: 包含微信一键授权、支付宝快捷登录、本机号码一键登录及手机验证码登录模式。
- **连续打卡与提醒**: 支持每日 20:00 夜间护肤打卡提醒与连续打卡天数激励。

---

## 🏗️ 技术架构与设计模式

GlowSkin 严格按照 **Modern Android Development (MAD)** 架构规范搭建：

```text
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Jetpack Compose)               │
│  DetectionScreen │ SkincareRoutineScreen │ ProfileScreen... │
└──────────────────────────────┬──────────────────────────────┘
                               │ Collects StateFlow
┌──────────────────────────────▼──────────────────────────────┐
│                    ViewModel Layer                          │
│                      SkinViewModel                          │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    Repository / Data Layer                  │
│       SkinRepository │ GeminiApiService │ Room DAO           │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
┌──────────────▼──────────────┐┌──────────────▼───────────────┐
│     Room Local Database     ││   Remote Gemini AI Service   │
│ (SQLite / SkinEntities)     ││    (Google AI REST API)     │
└─────────────────────────────┘└──────────────────────────────┘
```

### 核心依赖与技术选型

| 领域 | 依赖项 / 技术 | 作用说明 |
| :--- | :--- | :--- |
| **语言环境** | `Kotlin 1.9.20+` | 100% 纯 Kotlin 开发，利用 Coroutines 与 Flow 实现高效异步处理 |
| **UI 视图** | `Jetpack Compose 1.5+` | 声明式 UI，完整使用 Material Design 3 组件规范 |
| **架构与状态** | `ViewModel`, `StateFlow`, `collectAsStateWithLifecycle` | 生命周期感知的响应式状态管理 |
| **本地持久化** | `Room Database` + `KSP` | 高性能 SQLite 映射，存储检测历史与收藏数据 |
| **网络与 AI** | `Google Gemini REST API` / `Retrofit2` | 服务端 API Key 安全调用与智能文本生成 |
| **异步图片加载** | `Coil for Compose` | 异步加载网络图片与本地 Canvas 渲染 |
| **系统适配** | `Edge-to-Edge` | 配合 WindowInsets 实现全面屏沉浸式体验 |

---

## 🎨 视觉与 UI 设计规范

GlowSkin 采用了专为护肤与健康领域打造的 **Rose Gold & Pure Canvas** 视觉风格：

- **主色调 (Primary Color)**: `RosePrimary` (`#E53935` / `#D81B60`) — 优雅温柔的玫瑰红
- **点缀色 (Accent Color)**: `ChampagneGold` (`#FFD54F`) — 典雅高贵的香槟金
- **辅助色 (Secondary/Status)**: `SageGreen` (`#4CAF50`) — 代表健康与修护的草本绿
- **背景与卡片 (Canvas)**: 高对比度纯净浅色底纹与 `16.dp` 圆角修饰，带有柔和的 `RoseBorder` 边框。
- **无障碍体验 (Accessibility)**: 所有点击区域均符合 `48.dp` 最小触控标准，图标均设置合规 `contentDescription`。

---

## 📂 项目目录结构

```text
app/src/main/java/com/example/
├── MainActivity.kt                      # 主 Activity & 全局 Navigation 导航控制器
├── data/
│   ├── database/                        # Room 数据库架构
│   │   ├── SkinEntities.kt              # 实体类 (SkinScanRecord, Product, Routine)
│   │   ├── SkinDao.kt                   # 数据库访问 DAO 接口
│   │   └── SkinDatabase.kt              # RoomDatabase 配置
│   ├── remote/                          # Remote API (Gemini Service 客户端)
│   └── repository/                      # 统一数据源管理仓库
├── ui/
│   ├── screens/                         # Compose 核心功能页面
│   │   ├── DetectionScreen.kt           # 智能测肤、AI 相机与问答问诊
│   │   ├── SkincareRoutineScreen.kt     # AI 美肤顾问对话与早晚护肤 Routine
│   │   ├── ProductMatchScreen.kt        # 护肤品推荐、成分分析与防敏避雷
│   │   └── HistoryAndProfileScreen.kt   # 美肤档案、登录注册Modal、Before/After 对比
│   └── theme/                           # M3 动态主题、Color, Type, Shape
└── viewmodel/
    └── SkinViewModel.kt                 # 统一管理 UI State, Flow 及业务逻辑
```

---

## 🛠️ 快速编译与运行指南

### 1. 环境准备
- **Android Studio**: Android Studio Jellyfish (2023.3.1+) 或更新版本
- **JDK 版本**: JDK 17 (或 JDK 21)
- **SDK 版本**: `compileSdk = 34`, `minSdk = 24`, `targetSdk = 34`

### 2. 克隆项目与配置秘钥
1. 克隆代码至本地：
   ```bash
   git clone https://github.com/your-username/GlowSkin.git
   cd GlowSkin
   ```

2. **配置 Gemini API Key** *(可选)*:
   在项目根目录下配置 `.env` 文件或在 AI Studio 设置 Panel 中注入：
   ```env
   GEMINI_API_KEY="your_google_gemini_api_key"
   ```

### 3. 编译与安装运行
在 Android Studio 中打开项目，等待 Gradle 依赖自动下载完成后：
- 点击 **Run 'app'** (`Shift + F10`) 安装至真实设备或模拟器。
- 或使用 Terminal 构建 Debug APK：
  ```bash
  ./gradlew assembleDebug
  ```

---

## 🗺️ 项目演进路线图 (Roadmap)

- [x] **v1.0.0 (当前版本)**:
  - [x] AI 智能相机测肤与 10 题深度问卷系统
  - [x] Gemini 2.5 AI 顾问实时答疑与早晚 Routine 生成
  - [x] 成分库安全评价与避雷匹配
  - [x] 肤质水分/油脂趋势曲线与 Before & After 对比
  - [x] 多渠道账号登录 (微信/支付宝/一键登录/手机验证码)
- [ ] **v1.1.0 (计划中)**:
  - [ ] 支持国际化 (i18n) 英文与日韩文切换

---

## 🤝 贡献指南 (Contributing)

我们非常欢迎来自社区的贡献！无论是修复 Bug、改善 UI 还是增加新功能：

1. **Fork** 本仓库并创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
2. 提交您的修改 (`git commit -m 'Add some AmazingFeature'`)
3. 推送到远程分支 (`git push origin feature/AmazingFeature`)
4. 提交 **Pull Request**

---

## 📄 开源协议 (License)

本项目采用 [Apache License 2.0](LICENSE) 协议开源。

```text
Copyright 2026 GlowSkin Open Source Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

---

<p align="center">
  如果 GlowSkin 对您的学习或开发有所帮助，欢迎点亮右上角的 🌟 <b>Star</b> 支持本项目！
</p>
