# FBA 全链路物流 AI 系统 (FBA-LogiAI) - 终极实施方案

## 1. 项目愿景与架构对齐
本项目将构建一个**FBA 头程物流 AI 系统**，核心目标是学习 **Agent, Sub-Agent, Skill, RAG** 技术。
架构上，我们将深度参考您本地的 `big-market` 项目，采用 **DDD (领域驱动设计)**，将 Python 项目结构与 Java 标准工程结构对齐，实现 "Java 思想，Python 实现，AI 驱动"。

## 2. 核心领域划分 (Bounded Contexts)

### A. 营销领域 (Marketing Context) - [参考 big-market]
*   **核心模型**:
    *   `RaffleActivity` (抽奖活动): 定义活动周期、库存。
    *   `RaffleStrategy` (抽奖策略): 定义概率、权重、黑白名单。
    *   `Award` (奖品): 优惠券、免单名额。
*   **🏢 内部专家 Agent (Internal Strategist)**
    *   **职责**: 通过对话配置策略。
    *   **场景**: "创建一个新的双11活动，设置『满100KG送IPHONE』的策略，中奖率 0.1%。" -> 自动生成 `Strategy` 对象。
*   **🌏 外部专家 Agent (External Sales Rep)**
    *   **职责**: 引导客户参与活动。
    *   **场景**: "客户询问是否有优惠" -> Agent 调用 `RaffleStrategyService.draw()` 抽奖 -> 返回结果。

### B. 订单领域 (Order Context)
*   **核心模型**: `ShipmentOrder`, `FbaInfo`, `Consignee`.
*   **🏢 内部 Agent (Auditor)**: 审核托书，风险控制。
*   **🌏 外部 Agent (Booking Clerk)**: 接收订单，解析 PDF。

### C. 仓储领域 (Warehouse Context)
*   **核心模型**: `Pallet`, `Carton`, `InboundReceipt`.
*   **🏢 内部 Agent (Ops Lead)**: 材积录入，FBA 标签校验。

## 3. 技术栈 (Technology Stack)

### 🧠 AI 与 Agent 核心
*   **Orchestration**: `LangChain` + `LangGraph` (构建多智能体状态机).
*   **LLM**: `Google Gemini` / `Anthropic Claude`.
*   **RAG**: `Milvus` (Docker) + `pymilvus`.
    *   *数据源*: 海关税则 (PDF), 历史报价 (Excel).

### ⚙️ 后端与架构 (Python 3.13)
*   **Web 框架**: `FastAPI` (对应 Java 的 Spring Web).
*   **领域建模**: `Pydantic v2` (对应 Java 的 POJO/Entity).
*   **依赖注入**: `Dependency Injector` (对应 Java 的 Spring DI).

### 💻 前端规划 (Frontend Plan)
*   **Stack**: `React 18` + `TypeScript` + `Vite` + `Ant Design 5.0`.
*   **AI UI**: `ProChat` (Ant Design).

## 4. 目录结构映射 (Mapping big-market)

```text
fba_logi_ai/
├── domain/                     # [Core] 领域层 (参考 big-market-domain)
│   ├── marketing/              # 营销域
│   │   ├── model/              # Entity: Activity, Strategy, Award
│   │   ├── service/            # Service: RaffleService (抽奖逻辑)
│   │   └── repository/         # Repo Interface
│   ├── order/                  # 订单域
│   └── warehouse/              # 仓储域
├── infrastructure/             # [Infra] 基础层 (参考 big-market-infrastructure)
│   ├── persistence/            # MilvusDAO, MySQLDAO (Mock)
│   ├── llm/                    # GeminiClient, ClaudeClient
│   └── adapter/                # PDFParser, ExcelReader
├── application/                # [App] 应用层
│   ├── agents/                 # **Agent 定义 (LangGraph)**
│   │   ├── marketing_graph.py  # 营销子图
│   │   ├── order_graph.py      # 订单子图
│   │   └── main_graph.py       # 主路由图
│   └── services/               # 应用服务 (协调 Domain)
├── interfaces/                 # [Interface] 接口层 (参考 big-market-trigger)
│   ├── api/                    # FastAPI Routes
│   └── cli/                    # 命令行入口
├── knowledge/                  # RAG 原始文档
└── requirements.txt
```

## 5. 实施路线图 (Step-by-Step)

1.  **基础设施搭建**:
    *   初始化项目结构。
    *   启动 `Milvus` Docker 容器。
    *   配置 LLM API Key。
2.  **复刻 Marketing 领域**:
    *   用 Python Pydantic 重写 `big-market` 的 `Strategy` 和 `Award` 模型。
    *   编写 `Internal Marketing Agent`，让它能通过自然语言配置这些模型。
3.  **构建 RAG 知识库**:
    *   将 "FBA 仓库代码表" 和 "禁运品列表" 存入 Milvus。
4.  **开发业务 Agent**:
    *   实现 Order 和 Warehouse 的 Agent。
5.  **LangGraph 编排**:
    *   将所有 Agent 串联成一个完整的 State Machine。

**准备就绪，我们将从第一步：项目初始化与 Marketing 领域建模开始。**