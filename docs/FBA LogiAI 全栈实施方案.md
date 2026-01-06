# FBA LogiAI 全栈开发计划

## 1. 后端 API 开发 (FastAPI)
为了支持前端交互，我们需要将现有的 CLI 逻辑封装为 HTTP API。

*   **API 入口**: `fba_logi_ai/interfaces/api/server.py`
*   **核心接口**:
    *   `POST /api/chat/marketing/strategist`: 内部策略官对话
    *   `POST /api/chat/marketing/sales`: 外部销售对话
    *   `POST /api/chat/order/booking`: 订舱助手对话
    *   `POST /api/chat/warehouse/ops`: 仓库操作对话
    *   `GET /api/campaigns`: 获取活动列表 (用于前端展示)
    *   `GET /api/orders`: 获取订单列表
*   **技术**: FastAPI, Uvicorn, LangGraph (runnable invocation).

## 2. 前端项目初始化 (React + Vite)
创建一个新的前端项目 `fba-logi-web`。

*   **技术栈**: React 18, TypeScript, Vite, TailwindCSS, Ant Design 5.0 (使用 ProComponents 打造现代化后台).
*   **风格**: "Fresh & Modern" (使用蓝绿色调，圆角设计，卡片式布局).

## 3. 前端模块开发

### A. 客户端 (Customer Portal) - 清新风格
*   **布局**: 顶部导航，大图 Banner，卡片式内容。
*   **功能页**:
    *   **智能客服 (Chat)**: 集成 `SalesBot` 和 `BookingAgent`，支持 RAG 问答 (查运价、查时效)。
    *   **我的订单 (Orders)**: 订单列表与状态追踪。
    *   **营销活动 (Marketing)**: 抽奖转盘页面 (调用 Sales 工具)。

### B. 管理端 (Admin Portal) - 专业高效风格
*   **布局**: 侧边栏导航 (Ant Design Pro Layout)。
*   **功能页**:
    *   **策略工作台 (Strategy)**: 与 `Strategist` 对话创建活动。
    *   **订单审核 (Audit)**: 审核客户提交的订单。
    *   **仓库作业 (Warehouse)**: 录入材积，查看超重预警。

## 4. 联调与交付
*   配置 CORS 允许前端访问。
*   启动脚本 `start.sh` 同时拉起前后端。

我们将从 **后端 API 封装** 开始，然后是 **前端项目搭建**。