# FBA LogiAI 全栈实施与部署计划

## 1. 现状评估
*   **后端 (Backend)**: FastAPI API 服务已在 `fba_logi_ai/interfaces/api/server.py` 实现。支持所有 Agent 接口 (Strategist, Sales, Booking, Ops) 及数据查询。
*   **前端 (Frontend)**: 项目目录 `fba-logi-web` 存在但仅包含基础 `package.json`，缺少实际代码和 React/Vite 结构，初始化未完成。
*   **部署 (Deployment)**: 尚未创建 K8s/K3s 部署文件。

## 2. 实施步骤

### 第一阶段：前端重构 (React + Vite + AntD)
1.  **强制初始化**: 清空并重新初始化 `fba-logi-web` 为标准的 Vite + React + TS 项目。
2.  **安装依赖**: `antd`, `axios`, `react-router-dom`, `@ant-design/pro-components`, `tailwindcss`。
3.  **构建页面**:
    *   **客户端 (Client Portal)**: 清新风格。
        *   `Home`: 营销Banner + 功能入口。
        *   `Chat`: 统一对话界面 (支持切换 Sales/Booking 助手)。
        *   `Orders`: 订单列表查询。
    *   **管理端 (Admin Dashboard)**: 专业风格。
        *   `Strategy`: 策略配置对话窗口。
        *   `Warehouse`: 入库操作对话窗口。
        *   `Dashboard`: 数据概览 (Active Campaigns, Orders)。

### 第二阶段：后端增强
1.  **Docker化**: 创建 `Dockerfile` 用于构建后端镜像。
2.  **静态资源托管**: (可选) 配置 FastAPI 托管构建后的前端静态文件，或单独构建前端镜像。

### 第三阶段：K3s 部署
1.  **容器化**:
    *   构建后端镜像 `fba-logi-backend:v1`。
    *   构建前端镜像 `fba-logi-frontend:v1` (Nginx 托管)。
2.  **K8s 资源清单 (`k8s/`)**:
    *   `deployment-backend.yaml`: 部署 FastAPI 服务。
    *   `deployment-frontend.yaml`: 部署前端服务。
    *   `service.yaml`: 暴露服务 (NodePort 或 LoadBalancer)。
    *   `ingress.yaml`: (可选) 路由配置。
3.  **部署脚本**: 编写 `deploy.sh` 自动化应用配置。

## 3. 执行顺序
我们将优先完成 **前端代码编写**，确保应用逻辑闭环，最后编写 **K3s 部署文件**。