# qms-trace — QMS 质量追溯系统

基于芋道 ruoyi-vue-pro 二次开发，覆盖中小制造工厂 **质量管理 + 全流程批次追溯** 场景。

## 定位

QMS（Quality Management System，质量管理系统）是生产制造链条上的质量守门人——从原材料入厂到成品出货，每一批物料都要经过检验、记录缺陷、给出处置，并能在出问题时快速定位"问题料流向了哪里"和"不合格品用了哪些料"。

差异化核心是三个引擎：

- **批次谱系追溯**：正向（召回）/ 反向（溯源）BFS 图遍历，回答"这批料流向哪些成品"与"这个不合格品用了哪些料"
- **AQL 抽样方案引擎**：按 GB/T 2828.1 一般检验水平 II，由批量 + AQL 查样本量字码与 (n, Ac, Re)
- **SPC 统计过程控制**：X-Bar/R 控制图（A2/D4/D3 系数）、Cp/Cpk 过程能力指数、Western Electric 判异规则

## 核心取舍

| 取舍 | 决策 | 理由 |
|------|------|------|
| 谱系存储 | 邻接边表 `qms_trace_link` + 内存 BFS | 中小工厂批次规模下无需 Neo4j 等图数据库，关系库 + 应用层图遍历更轻、可运维 |
| 抽样方案 | 内置简化查表引擎（字码表 + AQL 主表） | GB/T 2828.1 主表有限，硬编码查表比引入规则引擎更快；商用需按标准全文补全 |
| SPC 计算 | 纯 Java 实现，不引入 commons-math3 | 均值/极差/标准差计算逻辑简单，少一个第三方依赖少一份升级风险 |
| 处置规则 | 规则引擎给出建议，人工/MRB 终审 | 致命缺陷不完全自动化，保留 MRB（物料评审）人工决策闭环 |
| 数据模型 | 6 张核心表 | 检验单/检验项/缺陷/批次/追溯链路/SPC 采样点，无多余关联表 |

## 代码结构

```
yudao-module-qms/
├── framework/qms/
│   ├── sampling/        AQL 抽样方案引擎（字码表 + GB/T 2828.1 简化主表）
│   ├── trace/           批次谱系追溯引擎（GenealogyGraph 正向/反向 BFS）
│   ├── spc/             SPC 统计过程控制（X-Bar/R 控制图 + 判异规则 + Cpk）
│   └── rule/            不合格品处置规则引擎（MRB 建议）
├── service/             7 组 Service（检验单/检验项/缺陷/批次/追溯/SPC/抽样）
├── dal/                 MyBatis-Plus Mapper + DO
├── controller/admin/    7 个 Controller（检验单/检验项/缺陷/批次/追溯/SPC/抽样）
└── enums/               7 个业务枚举
```

## 快速开始

```bash
# 1. 建表 + 种子数据
psql -d your_db -f yudao-module-qms/src/main/resources/sql/qms-init.sql

# 2. 查询 AQL 抽样方案（批量 500，AQL 2.5）
# GET /api/admin/qms/sampling/plan?lotSize=500&aqlLevel=2.5

# 3. 提交检验（汇总检验项不合格数 → 判定合格/不合格 → 联动批次状态）
# POST /api/admin/qms/inspection-order/submit?id=1

# 4. 正向追溯（召回）与反向追溯（溯源）
# GET /api/admin/qms/trace/forward?batchId=1
# GET /api/admin/qms/trace/backward?batchId=2

# 5. SPC 控制图（最近 25 个子组）
# GET /api/admin/qms/spc/chart?productCode=P-1001&characteristicCode=外径
```

## 商用化检查清单

- [ ] AQL 主表是否按 GB/T 2828.1-2012 全文补全？当前为简化表（箭头单元格已坍缩，商用必须逐格核对）
- [ ] 批次谱系是否有环？GenealogyGraph 用 visited Set 防环，但脏数据形成的环需在录入侧拦截
- [ ] 追溯查询全量加载 links 是否有性能风险？大批量场景改用 PostgreSQL 递归 CTE（`WITH RECURSIVE`）
- [ ] SPC 子组容量是否落在 A2/D3/D4 系数表范围内（n=2..10）？超范围需查表扩展
- [ ] 判异规则是否命中需求？当前实现 4 条 Western Electric 规则，可按客户 SOP 增补
- [ ] 致命缺陷处置是否强制走 MRB 评审？规则引擎只给建议，终审必须在 MRB 会议留痕

## 依赖

- Java 8+（yudao 父工程 jdk8-snapshot）
- cn.iocoder.boot:yudao-spring-boot-starter-biz-*（Web/Security/MyBatis/Tenant）
- 无第三方算法依赖（抽样/追溯/SPC 均纯 Java 实现）

## License

LGPL-3.0（与 ruoyi-vue-pro 保持一致，商用友好）
