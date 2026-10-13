# -*- coding: utf-8 -*-
"""QMS 质量追溯系统：6 张核心表。

与 APS（排产，6 张：work_order/task/resource/calendar/mold/schedule_plan）相比，
QMS 不产出计划，而是把"检验—缺陷—批次—谱系—控制图"串成一条闭环：
发现问题（检验项/缺陷）→ 定位范围（批次/链路）→ 判断能力（SPC）。

表关系（ER 极简图）:

  qms_batch ──(自引用 parent_batch_id)── qms_batch
     │  │
     │  ├──(N) qms_trace_link ──(from_batch_id / to_batch_id)── qms_batch
     │  │
     │  └──(N) qms_inspection_order ──(N) qms_inspection_item
     │                                  └──(N) qms_defect
     │
     └──(N) qms_spc_sample (按 product_code + characteristic_code 关联，弱关联无外键)

  业务流程：
  1. 产出入库 → 建 qms_batch（投料/拼批/分拆再写 qms_trace_link 建谱系）
  2. 送检 → 建 qms_inspection_order（带 AQL 抽样方案 n/Ac/Re）
  3. 录入检测 → 逐条写 qms_inspection_item；发现不合格再写 qms_defect
  4. 提交检验 → 汇总不合格数与 Re 比较判定；不合格联动批次状态 + 回填缺陷处置建议
  5. 控制图 → 采样子组写 qms_spc_sample，自动算控制限与判异
  6. 追溯 → 正向（召回）/ 反向（溯源）沿 qms_trace_link 做 BFS
"""

-- ---------------------------------------------------------------------------
-- 批次/序列号：追溯的主体单元。parent_batch_id 是快速单级父追溯（可空）；
-- 完整多级谱系由 qms_trace_link 邻接表得到。attributes 用 JSONB 存行业扩展属性
-- （供应商/炉号/设备参数等），避免为每个行业加字段。
-- ---------------------------------------------------------------------------
CREATE TABLE qms_batch (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id             BIGINT       NOT NULL,
    batch_no              VARCHAR(64)  NOT NULL,                  -- 批次号，租户内唯一，如 B-20261013-001
    product_code          VARCHAR(64)  NOT NULL,                  -- 产品编码
    product_name          VARCHAR(128) NOT NULL,
    quantity              INTEGER      NOT NULL DEFAULT 0,        -- 批次数量
    production_order_code VARCHAR(64),                            -- 关联生产工单编码
    machine_code          VARCHAR(64),                            -- 生产机台
    shift                 VARCHAR(32),                            -- 班次，如 "白班" / "夜班"
    parent_batch_id       BIGINT,                                 -- 谱系父批次（自引用，可空）
    attributes            JSONB        NOT NULL DEFAULT '{}',     -- 批次扩展属性，如 {"supplier":"X钢厂","heatNo":"H2026101"}
    status                SMALLINT     NOT NULL DEFAULT 1,        -- 1=在制 2=待检 3=合格 4=不合格 5=已出货 6=冻结
    produce_time          TIMESTAMPTZ,                            -- 生产时间
    expiry_time           TIMESTAMPTZ,                            -- 有效期（食品/药品/化工）
    remark                VARCHAR(512) NOT NULL DEFAULT '',
    creator               VARCHAR(64),
    create_time           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updater               VARCHAR(64),
    update_time           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, batch_no, deleted)
);
CREATE INDEX idx_qms_batch_parent  ON qms_batch (parent_batch_id);
CREATE INDEX idx_qms_batch_product ON qms_batch (product_code);

-- ---------------------------------------------------------------------------
-- 追溯链路：批次谱系图的"边"。link_type 区分投料/拼批/分拆/装配/返工，
-- 不同工艺关系的追溯语义不同（分拆=一入多出，装配=多入一出，返工=自环后新批）。
-- GenealogyGraph 正向/反向 BFS 均以此为邻接边。
-- ---------------------------------------------------------------------------
CREATE TABLE qms_trace_link (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    from_batch_id BIGINT       NOT NULL,                        -- 上游/输入批次 ID
    to_batch_id   BIGINT       NOT NULL,                        -- 下游/产出批次 ID
    link_type     SMALLINT     NOT NULL DEFAULT 1,              -- 1=投料 2=拼批 3=分拆 4=装配 5=返工
    quantity      DECIMAL(18,4),                                -- 流转数量（可小于批次总量，如部分投料）
    link_time     TIMESTAMPTZ,                                  -- 流转时间
    remark        VARCHAR(512) NOT NULL DEFAULT '',
    creator       VARCHAR(64),
    create_time   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updater       VARCHAR(64),
    update_time   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_qms_trace_link_from ON qms_trace_link (from_batch_id);
CREATE INDEX idx_qms_trace_link_to   ON qms_trace_link (to_batch_id);

-- ---------------------------------------------------------------------------
-- 检验单：一次检验活动的主记录。承载 AQL 抽样方案（lot_size/sample_qty/Ac/Re），
-- submitInspection 时按检验项的不合格数与该单 reject_qty（Re）比较得出 result。
-- ---------------------------------------------------------------------------
CREATE TABLE qms_inspection_order (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id        BIGINT       NOT NULL,
    code             VARCHAR(64)  NOT NULL,                    -- 检验单编码，租户内唯一，如 QMS-20261013-001
    inspection_type  SMALLINT     NOT NULL DEFAULT 2,          -- 1=IQC来料 2=IPQC过程 3=FQC成品 4=OQC出货
    batch_id         BIGINT,                                   -- 关联批次 ID（追溯主体）
    work_order_code  VARCHAR(64),                              -- 关联工单编码
    product_code     VARCHAR(64),
    lot_size         INTEGER      NOT NULL DEFAULT 0,          -- 送检总数（检验批量）
    sample_qty       INTEGER      NOT NULL DEFAULT 0,          -- 抽样数 n（由 AQL 抽样方案引擎给出）
    aql_level        VARCHAR(16),                              -- AQL 水平，如 "2.5"
    accept_qty       INTEGER      NOT NULL DEFAULT 0,          -- 接收数 Ac
    reject_qty       INTEGER      NOT NULL DEFAULT 0,          -- 拒收数 Re（不合格数 >= Re 判不合格）
    result           SMALLINT     NOT NULL DEFAULT 0,          -- 0=待检 1=合格 2=不合格 3=让步接收
    inspector        VARCHAR(64),                              -- 检验员
    inspect_time     TIMESTAMPTZ,                              -- 检验时间
    remark           VARCHAR(512) NOT NULL DEFAULT '',
    creator          VARCHAR(64),
    create_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updater          VARCHAR(64),
    update_time      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (tenant_id, code, deleted)
);
CREATE INDEX idx_qms_inspection_order_batch ON qms_inspection_order (batch_id);

-- ---------------------------------------------------------------------------
-- 检验项：检验单下的单个检测特性。记录标准值/上下限/实测值/单件判定。
-- submitInspection 统计 item_result=2（不合格）的条数作为本次检验的不合格数。
-- ---------------------------------------------------------------------------
CREATE TABLE qms_inspection_item (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL,
    inspection_order_id BIGINT       NOT NULL REFERENCES qms_inspection_order (id) ON DELETE CASCADE,
    item_code           VARCHAR(64)  NOT NULL,                 -- 检验项编码，如 DIM-01
    item_name           VARCHAR(128) NOT NULL,                 -- 检验项名称，如 "外径"
    standard_value      VARCHAR(128),                          -- 标准值（文本描述，如 "10±0.05"）
    upper_limit         DECIMAL(18,4),                         -- 规格上限
    lower_limit         DECIMAL(18,4),                         -- 规格下限
    actual_value        DECIMAL(18,4),                         -- 实测值
    unit                VARCHAR(16),                           -- 计量单位
    item_result         SMALLINT     NOT NULL DEFAULT 1,       -- 1=合格 2=不合格
    remark              VARCHAR(512) NOT NULL DEFAULT '',
    creator             VARCHAR(64),
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updater             VARCHAR(64),
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_qms_inspection_item_order ON qms_inspection_item (inspection_order_id);

-- ---------------------------------------------------------------------------
-- 缺陷记录：检验中发现的具体缺陷。disposition 为 NULL 表示尚未处置；
-- submitInspection 判定不合格时调用 DispositionRuleEngine 按严重度给出建议处置并回填。
-- ---------------------------------------------------------------------------
CREATE TABLE qms_defect (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL,
    inspection_order_id BIGINT       NOT NULL REFERENCES qms_inspection_order (id) ON DELETE CASCADE,
    defect_code         VARCHAR(64)  NOT NULL,                 -- 缺陷编码，如 DEF-001
    defect_name         VARCHAR(128) NOT NULL,                 -- 缺陷名称，如 "毛刺"
    defect_category     VARCHAR(64),                           -- 缺陷类别，如 "外观" / "尺寸" / "性能"
    severity            SMALLINT     NOT NULL DEFAULT 3,       -- 1=致命 2=严重 3=轻微
    quantity            INTEGER      NOT NULL DEFAULT 1,       -- 缺陷数量
    disposition         SMALLINT,                              -- 1=返工 2=返修 3=让步接收 4=报废 5=退货；NULL=未处置
    responsible_process VARCHAR(128),                          -- 责任工序
    image_url           VARCHAR(512),                          -- 缺陷图片地址
    remark              VARCHAR(512) NOT NULL DEFAULT '',
    creator             VARCHAR(64),
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updater             VARCHAR(64),
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_qms_defect_order ON qms_defect (inspection_order_id);

-- ---------------------------------------------------------------------------
-- SPC 采样点：一个控制图子组（subgroup）的样本数据。values_json 存子组原始测量值，
-- mean_value/range_value 由 SpcCalculator 计算回填；cl/ucl/lcl 依据子组容量查
-- A2/D4/D3 系数得到；rule_violations 存 Western Electric 判异规则命中描述。
-- 与批次是弱关联（按 product_code + characteristic_code），不设外键。
-- ---------------------------------------------------------------------------
CREATE TABLE qms_spc_sample (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tenant_id           BIGINT       NOT NULL,
    product_code        VARCHAR(64)  NOT NULL,                 -- 产品编码
    characteristic_code VARCHAR(64)  NOT NULL,                 -- 特性编码，如 "外径" / "厚度"
    chart_type          SMALLINT     NOT NULL DEFAULT 1,       -- 1=X-Bar/R 2=X-MR 3=P 4=NP 5=C 6=U
    subgroup_index      INTEGER      NOT NULL DEFAULT 1,       -- 子组序号（同一特性内递增）
    subgroup_size       INTEGER      NOT NULL DEFAULT 0,       -- 子组容量
    values_json         JSONB        NOT NULL DEFAULT '[]',    -- 子组内测量值数组
    mean_value          DECIMAL(18,6),                         -- 子组均值 X-bar
    range_value         DECIMAL(18,6),                         -- 子组极差 R
    cl_value            DECIMAL(18,6),                         -- 中心线 CL
    ucl_value           DECIMAL(18,6),                         -- 上控制限 UCL
    lcl_value           DECIMAL(18,6),                         -- 下控制限 LCL
    rule_violations     JSONB        NOT NULL DEFAULT '[]',    -- 判异规则命中描述
    sample_time         TIMESTAMPTZ,                           -- 采样时间
    creator             VARCHAR(64),
    create_time         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updater             VARCHAR(64),
    update_time         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted             BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_qms_spc_sample_char ON qms_spc_sample (product_code, characteristic_code, subgroup_index);

-- ===========================================================================
-- 种子数据：一条"原料 → 成品"的最小谱系，附一张可追溯的检验单与一个控制图子组，
-- 方便本地跑 Demo（正/反向追溯、控制图、抽样方案均可直接验证）。
-- ===========================================================================

-- 两个批次（原料 + 成品）与它们之间的一条投料链路
INSERT INTO qms_batch (tenant_id, batch_no, product_code, product_name, quantity,
                       production_order_code, machine_code, shift, attributes, status, produce_time) VALUES
(0, 'B-20261013-001', 'P-1001', '注塑外壳', 1000,
 'WO-20261013-001', 'M-01', '白班', '{"supplier":"X塑胶厂","heatNo":"H2026101"}', 3, '2026-10-13 08:30:00+08'),
(0, 'B-20261013-002', 'P-1002', '成品组件', 950,
 'WO-20261013-001', 'LINE-01', '白班', '{"line":"LINE-01","operator":"张三"}', 2, '2026-10-13 14:00:00+08');

INSERT INTO qms_trace_link (tenant_id, from_batch_id, to_batch_id, link_type, quantity, link_time) VALUES
(0, 1, 2, 1, 1000, '2026-10-13 09:00:00+08');

-- 一张检验单（IPQC，AQL 2.5，n=125 Ac=7 Re=8）
INSERT INTO qms_inspection_order (tenant_id, code, inspection_type, batch_id, work_order_code,
                                  product_code, lot_size, sample_qty, aql_level, accept_qty, reject_qty,
                                  result, inspector, inspect_time) VALUES
(0, 'QMS-20261013-001', 2, 2, 'WO-20261013-001',
 'P-1002', 950, 125, '2.5', 7, 8,
 2, '李四', '2026-10-13 15:00:00+08');

-- 两个检验项（一个合格、一个不合格）
INSERT INTO qms_inspection_item (tenant_id, inspection_order_id, item_code, item_name, standard_value,
                                 upper_limit, lower_limit, actual_value, unit, item_result) VALUES
(0, 1, 'DIM-01', '外径', '10±0.05', 10.05, 9.95, 10.01, 'mm', 1),
(0, 1, 'APP-01', '外观', '无毛刺', NULL, NULL, NULL, NULL, 2);

-- 一个控制图子组（X-Bar/R，容量 5）
INSERT INTO qms_spc_sample (tenant_id, product_code, characteristic_code, chart_type,
                            subgroup_index, subgroup_size, values_json,
                            mean_value, range_value, cl_value, ucl_value, lcl_value,
                            rule_violations, sample_time) VALUES
(0, 'P-1002', '外径', 1,
 1, 5, '[10.01, 9.99, 10.02, 10.00, 9.98]',
 10.000000, 0.040000, 10.000000, 10.023035, 9.976965,
 '[]', '2026-10-13 15:10:00+08');
