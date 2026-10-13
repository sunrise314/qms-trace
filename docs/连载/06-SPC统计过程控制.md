# SPC 统计过程控制——X-bar/R 控制图与 Cpk

## 一、场景：尺寸在慢慢漂，肉眼看不见

上一章的检验单是"事后把关"——抽完样、数完不合格、判合格或不合格。但王主任给我看过一个更隐蔽的问题：有个关键尺寸（注塑外壳外径）的标准是 10±0.05mm，几个月里 **IPQC 每次抽检都判合格**，因为每一次的实测值都落在 9.95~10.05 里，一张单子也不超差。可某个月突然整批超差、成批报废，一查记录才发现：**外径的均值这几个月一直在往上漂，从 10.00 漂到 10.04，就差 0.01 就顶到上限了**。单次判定永远是"合格"，趋势却在往悬崖边走——事后把关看不见这种漂移，因为它只看"这一点在不在范围内"，不看"这些点排成什么形状"。

这就是 SPC（Statistical Process Control，统计过程控制）要解决的问题。它不满足于"这一批合格"，它要回答三个更深的问题：**过程稳不稳（有没有异常波动）？过程的均值偏不偏？过程的能力够不够（能不能稳定地卡在规格里）？** 对应的三件工具是：**控制图（看稳不稳）、控制限（看偏不偏）、过程能力指数 Cpk（看够不够）**。

SPC 的哲学转变很关键：检验单是"**从产品里挑出坏的**"（符合性检验），SPC 是"**从过程里找出会造出坏产品的趋势**"（过程控制）。前者治标，后者治本。王主任那句"刀具在慢慢磨，你看不见"，说的就是这件事。

## 二、为什么 SPC 用纯 Java 手写，不引 commons-math3

做 SPC 第一反应是"上 Apache Commons Math 啊，均值、标准差、分布函数都有现成的"。我在取舍表里选择**纯 Java 手写**，理由有三条：

**其一，SPC 用的统计量极简单。** 控制图要的就三个量：**均值、极差、标准差**。均值是求和除以个数，极差是最大值减最小值，标准差是"每个值减均值、平方、求和、除以 n-1、开根号"。这三个公式都是几行代码的事，`commons-math3` 那套 `DescriptiveStatistics` 封装的是"几千个统计量"，我们只用三个，**引入整个库为用零头，不划算**。

**其二，依赖越少越省心。** `commons-math3` 是个老牌库，但任何第三方依赖都意味着多一个版本升级、多一次安全公告、多一层冲突排查。SCADA、APS、QMS 三个模块共用一个芋道工程，我刻意让它们**都不引算法库**——APS 用手写规则不引 Timefold，QMS 用手写统计不引 commons-math3，整个工程的外部依赖就压到最小。

**其三，可控可测。** 手写的 `mean/range/stdDev` 逻辑透明、可单元测试、边界处理自己说了算（比如空数组返回 0、样本数小于 2 时标准差置 0）。用库的黑盒方法，出了数值偏差还得去翻它文档。

所以 `SpcCalculator` 是一个 `@UtilityClass`（Lombok 的静态工具类），里面全是 `static` 方法，没有任何依赖注入，可以脱离 Spring 单独跑单元测试。

## 三、X-Bar/R 控制图：三个系数表定了控制限

控制图的核心是**控制限**。这里要先分清两个概念，它们最容易被搞混：**规格限（USL/LSL）** 是"客户要求的边界"——外径 10±0.05，也就是 9.95~10.05，这是硬约束；**控制限（UCL/LCL）** 是"过程自己波动的边界"——由过程数据算出来的、反映过程真实能力的统计边界。**控制限永远比规格限更窄**，因为它是过程自身波动的刻画。判异看控制限，判合规格看规格限，两套线。

X-Bar/R 控制图是最经典的一对图：**X-Bar 图看"均值漂没漂"，R 图看"波动大不大"**。它们的控制限不是简单取 3 倍标准差，而是通过系数表换算：

```java
// framework/qms/spc/SpcCalculator.java —— 三个系数表
/** A2 系数（X-Bar 图控制限用），下标 = 子组容量 n */
private static final double[] A2 = {
        0, 0, 1.880, 1.023, 0.729, 0.577, 0.483, 0.419, 0.373, 0.337, 0.308
};

/** D3 系数（R 图下控制限用），下标 = 子组容量 n */
private static final double[] D3 = {
        0, 0, 0, 0, 0, 0, 0, 0.076, 0.136, 0.184, 0.223
};

/** D4 系数（R 图上控制限用），下标 = 子组容量 n */
private static final double[] D4 = {
        0, 0, 3.267, 2.574, 2.282, 2.114, 2.004, 1.924, 1.864, 1.816, 1.777
};
```

这三个数组的下标是**子组容量 n**，数组长度 11，也就是覆盖 n = 0..10（工业常用的子组大小是 4~5，最多到 10）。注意 `A2[0]`、`A2[1]` 是 0，`D3` 在 n≤6 时全是 0——**不是笔误，是数学事实**：A2/A3 系数在 n=1 时无意义；D3 在 n≤6 时理论值为 0（极差分布的偏度导致近正态的下限不存在），所以 R 图在小子组时**没有下控制限**（`LCL = D3 × R̄ = 0`）。这些都是查标准表得来的固定常数，抄错一位整张图就废了。

控制限的算法：

```java
// framework/qms/spc/SpcCalculator.java
/**
 * X-Bar/R 控制图控制限。
 * X-Bar 图：CL = 各子组均值之均值，UCL/LCL = CL ± A2 × 平均极差；
 * R 图：CL = 平均极差，UCL = D4 × 平均极差，LCL = D3 × 平均极差。
 *
 * @param subgroups 子组集合，每个子组是一组测量值（假定容量一致）
 */
public static XbarRChart xbarRChart(List<double[]> subgroups) {
    if (subgroups == null || subgroups.isEmpty()) {
        return new XbarRChart(0, 0, 0, 0, 0, 0);
    }
    int n = largestSize(subgroups);
    double sumMean = 0d;
    double sumRange = 0d;
    for (double[] subgroup : subgroups) {
        sumMean += mean(subgroup);
        sumRange += range(subgroup);
    }
    double xbarCl = sumMean / subgroups.size();
    double rCl = sumRange / subgroups.size();
    double a2 = coefficient(A2, n);
    double d3 = coefficient(D3, n);
    double d4 = coefficient(D4, n);
    return new XbarRChart(
            xbarCl, xbarCl + a2 * rCl, xbarCl - a2 * rCl,
            rCl, d4 * rCl, d3 * rCl);
}
```

逻辑分成四步：①遍历所有子组，累加"每个子组的均值"和"每个子组的极差"；②除以子组个数得到**总均值 X̄**（X-Bar 图的 CL）和**平均极差 R̄**（R 图的 CL）；③按子组容量 n 查 A2/D3/D4；④ X-Bar 图控制限是 `X̄ ± A2·R̄`，R 图控制限是 `R̄` 上下各乘 D4/D3。

注意返回值是一个 `record XbarRChart`，一次把**两张图的六条线**（X-Bar 的 CL/UCL/LCL + R 图的 CL/UCL/LCL）全带回来。为什么合成一个 record 而不是返回两个对象？因为这两张图**永远成对出现**——看 X-Bar 均值漂移的同时必须看 R 极差，否则可能是"一个子组内某个点特别离谱"拉动了极差，未必是过程真的漂了。**成对返回，逼调用方同时看两张图**，这是个刻意的 API 设计。

查系数用 `coefficient(table, n)` 做了一次防御性夹取：n 超过表范围就取最近端点。为什么？因为如果现场录入了容量 12 的子组（超出 A2 表的 n=10），直接下标越界会崩。夹取到 n=10 的系数是**近似但不崩**——同时我要求"超范围需人工确认"（写在类注释里），因为系数不对，控制限就是错的。

## 四、Cpk：过程能力指数，回答"够不够"

控制图看"稳不稳"，Cpk 看"够不够"——**过程本身的宽度，相比规格的宽度，还有多少余量**。

```java
// framework/qms/spc/SpcCalculator.java
/**
 * 过程能力指数。
 * @param mean   过程均值
 * @param stdDev 过程标准差（<=0 时返回全 0）
 * @param usl    规格上限
 * @param lsl    规格下限
 */
public static CpkResult cpk(double mean, double stdDev, double usl, double lsl) {
    if (stdDev <= 0) {
        return new CpkResult(0, 0, 0, 0, mean, stdDev);
    }
    double cp = (usl - lsl) / (6 * stdDev);
    double cpu = (usl - mean) / (3 * stdDev);
    double cpl = (mean - lsl) / (3 * stdDev);
    double cpk = Math.min(cpu, cpl);
    // 仅有组内标准差，Pp/Ppk 暂与 Cp/Cpk 同值；接入长期数据后可分开计算
    return new CpkResult(cp, cpk, cp, cpk, mean, stdDev);
}
```

这里有两个指数：

- **Cp**（潜在能力）= `(USL - LSL) / (6σ)`——只看过程和规格的**宽度比**，不管均值偏不偏。Cp 高说明过程波动小，但如果均值偏到一边，照样超差。
- **Cpk**（实际能力）= `min(CPU, CPL)`——把均值偏移也考虑进去。`CPU = (USL - 均值) / (3σ)` 是均值到上限还能容纳几个 σ，`CPL` 是到下限的几个 σ，取**两者中小的那个**（离哪边近，能力就被哪边卡着）。

举例：外径规格 9.95~10.05（宽 0.10），过程 σ=0.01。那么 Cp = 0.10/(6×0.01) = 1.67，很好。但如果均值漂到了 10.04，CPU = (10.05-10.04)/(3×0.01) = 0.33，CPL = (10.04-9.95)/(3×0.01) = 3.0，取小的 → **Cpk = 0.33，惨不忍睹**。这就是"宽是够的，但偏了"。行业惯例：Cpk ≥ 1.33 算合格，≥ 1.67 算优良，< 1.0 说明过程能力严重不足。王主任那个从 10.00 漂到 10.04 的案例，Cpk 就是从"优良"慢慢掉到"崩盘"的过程——**单次合格掩盖了 Cpk 的持续恶化**。

代码里 `Cp` 直接等于 `Cpk` 的那个注释要点一下：真正的 Cp/Cpk 用**组内标准差 σ̂ = R̄/d2**（短期能力），Pp/Ppk 用**长期标准差**（含组间波动）。当前实现只有一个"子组均值序列的标准差"，所以 Cp 和 Cpk 用了同一个 σ，Pp/Ppk 暂时与 Cp/Cpk 同值。**商用接入长期数据后，Pp/Ppk 必须用长期 σ 单独算**——这是我在检查清单里标注的点，也是这类"简化实现"必须诚实标注边界的地方。

## 五、判异规则：Western Electric 4 条

有了控制限，最后一步是**自动判"哪些点不对劲"**。SPC 的判异常不是"超线才算异常"，而是一整套规则——最经典的是 Western Electric 规则。它抓的是点序列的**形状**（连续同侧、连续单调、交替上升），而不是单点是否超线。我实现了其中 4 条：

```java
// framework/qms/spc/SpcCalculator.java
/**
 * Western Electric 判异规则（简化 4 条）。
 * @param points 控制点序列（X-Bar 或单值）
 * @param cl     中心线
 * @param sigma  过程标准差（用于 3σ 判定）
 * @return 命中的规则描述；无异常返回空列表
 */
public static List<String> westernElectricRules(List<Double> points, double cl, double sigma) {
    List<String> violations = new ArrayList<>();
    if (points == null || points.isEmpty()) {
        return violations;
    }
    int size = points.size();

    // 规则 1：单点超出 3σ 控制限
    for (int i = 0; i < size; i++) {
        Double p = points.get(i);
        if (p != null && sigma > 0 && Math.abs(p - cl) > 3 * sigma) {
            violations.add("规则1：第 " + (i + 1) + " 点超出 3σ 控制限");
        }
    }

    // 规则 2：连续 9 点位于中心线同侧
    int sameSideRun = 1;
    for (int i = 1; i < size; i++) {
        Double prev = points.get(i - 1);
        Double cur = points.get(i);
        if (prev == null || cur == null) {
            sameSideRun = 1;
            continue;
        }
        boolean sameSide = (prev > cl && cur > cl) || (prev < cl && cur < cl);
        sameSideRun = sameSide ? sameSideRun + 1 : 1;
        if (sameSideRun >= 9) {
            violations.add("规则2：连续 9 点位于中心线同侧（第 " + (i - 7) + "-" + (i + 1) + " 点）");
        }
    }
    // ... 规则 3（连续 6 点递增/递减）、规则 4（连续 14 点交替）同类扫描 ...
    return violations;
}
```

四条规则各有分工：

- **规则 1：单点超出 3σ。** 最直白的异常——某点直接飞到控制限外。这是"突变型"异常（设备故障、来料突变）。
- **规则 2：连续 9 点位于中心线同侧。** 单看每个点都在线内，但**9 点连续偏在一侧**说明均值系统性地漂了。王主任那个"均值慢慢漂"的案例，命中的就是这条——它比规则 1 更早预警，因为点在超线之前，早就在同一侧排了一长串。
- **规则 3：连续 6 点持续递增或递减。** 抓"持续的单调趋势"——刀具磨损、温度缓升这类**趋势型**异常。均值从 10.00 一路爬到 10.04，就是这条先亮灯。
- **规则 4：连续 14 点交替上下。** 抓"规律的锯齿波动"——通常反映**两个交替的系统**（双工位、双班次、双模具）在轮流制造波动。

规则 2 的实现里有个细节值得说：`sameSideRun` 用"两点是否在中心线同一侧"来判断，`prev` 和 `cur` 都不为 null 才算，且严格用 `>` 和 `<`（**等于 cl 的点不算任何一侧**，会重置计数）。这处理了"均值恰好落在中心线上"的边界。命中后加进 `violations` 列表，描述里带上**命中的点区间**（如"第 3-11 点"），让现场能直接翻回去看是哪一段。

规则 3、规则 4 也是同样的滑动窗口扫描，区别只是判断条件（单调 vs 交替）。这四条规则是"简化 4 条"，实际 Western Electric 有 8 条、Nelson 规则有 8 条——**商用按客户 SOP 增补**，架构上只是一组 `if` 分支，加规则不动主流程。

## 六、Service 层：控制限滚动计算

引擎算的是"给定一堆子组，控制限是多少、有没有判异"。谁来喂子组？`QmsSpcServiceImpl.addSample` 负责：**每次采样，把已有子组 + 新子组一起重算控制限，回填到新采样点，并对均值序列跑判异**。

```java
// service/spc/QmsSpcServiceImpl.java —— addSample 节选
List<QmsSpcSampleDO> existing = spcSampleMapper.selectListByCharacteristic(productCode, characteristicCode);
List<double[]> subgroups = new ArrayList<>();
for (QmsSpcSampleDO sample : existing) {
    subgroups.add(toArray(sample.getValuesJson()));
}
double[] newSubgroup = toArray(values);
subgroups.add(newSubgroup);

SpcCalculator.XbarRChart chart = SpcCalculator.xbarRChart(subgroups);
List<Double> means = new ArrayList<>();
for (double[] subgroup : subgroups) {
    means.add(SpcCalculator.mean(subgroup));
}
// 判异用的 sigma：以子组均值序列的样本标准差估计
double sigma = SpcCalculator.stdDev(toArray(means));
List<String> violations = SpcCalculator.westernElectricRules(means, chart.xbarCl(), sigma);

QmsSpcSampleDO sample = new QmsSpcSampleDO();
sample.setSubgroupIndex(existing.size() + 1);
sample.setValuesJson(values);
sample.setMeanValue(round(SpcCalculator.mean(newSubgroup)));
sample.setRangeValue(round(SpcCalculator.range(newSubgroup)));
sample.setClValue(round(chart.xbarCl()));
sample.setUclValue(round(chart.xbarUcl()));
sample.setLclValue(round(chart.xbarLcl()));
sample.setRuleViolations(violations);
spcSampleMapper.insert(sample);
```

三个工程点：

**其一，控制限是"滚动"的。** 每来一个新子组，就把历史全部子组重算一遍控制限，写到**新采样点**上。这意味着**同一特性的不同采样点，CL/UCL/LCL 可能是不同的**——早期样本少时控制限宽，后期样本多了收窄。工业上更规范的做法是"先用一段基线数据固定控制限，之后冻结不再变"，但 MVP 用滚动更简单（不用判断"基线够了没有"），代价是控制限会动。我在类注释和清单里都标了"商用按需切换"。

**其二，`subgroup_index` 自增。** `existing.size() + 1` 让同一特性下的子组序号 1、2、3……递增，`chart` 查询时"取最近 25 个子组"（`MAX_SUBGROUPS = 25`）就靠它排序取尾。

**其三，判异用的 sigma 是"子组均值序列的标准差"。** 注意这里不是用组内 σ̂（R̄/d2），而是直接对**均值序列**求样本标准差。数学上更严格的 X-Bar 图判异应该用 `σ/√n` 作为均值的标准误，当前实现用"均值的标准差"是一种**近似**——我在代码注释里也写了"商用可改用 R̄/d2 估计组内 sigma"。这类近似在 MVP 阶段能跑、能预警，但严谨性上要如实标出来。

`chart` 方法的另一半逻辑是"取最近 25 个子组 + 可选算 Cpk"：如果请求带了 `usl` 和 `lsl`，就调 `SpcCalculator.cpk` 算出 Cp/Cpk 塞进返回的 `SpcChartVO`；没带就只出控制图。这样前端"看控制图"和"看能力"用同一个接口，按需带规格限即可。

## 七、边界与踩坑

**坑一：子组容量必须一致。** X-Bar/R 用 R̄ 查系数要求"所有子组等容量"。如果现场有的子组 5 个点、有的 3 个点，R̄ 的语义就乱了。当前实现用 `largestSize` 取最大容量查系数，是**防御性妥协**——它不崩，但控制限会有偏差。根治要在录入侧校验容量一致。

**坑二：σ 的估计口径。** 前面说的"均值序列标准差"和"组内 σ̂ = R̄/d2"是两种口径，算出的控制限和 Cpk 不同。这是 SPC 里最容易吵架的地方——**两套算法都"对"，但结果不一样，关键是全系统口径统一**。当前统一用"样本标准差"，能自洽，但和教科书标准算法有差异。

**坑三：判异规则的"每次累积重算"成本。** `addSample` 每次都全量重算 + 全序列判异，子组多了会变慢（O(n) 每次，n 次累积 O(n²)）。子组量级不大时无所谓，几万子组要改增量算法或用数据库窗口函数。

**坑四：Cp=Cpk 的简化。** 前面点过，Pp/Ppk 和 Cp/Cpk 应该分开（短期 vs 长期 σ），当前同值。**给客户看 Cpk 时要说清这是短期能力**，别让客户拿它当长期承诺。

## 八、商用化检查清单（SPC 版）

- 子组容量是否被强制一致？不一致时 `largestSize` 会静默产生偏差系数。
- σ 估计口径（样本标准差 vs R̄/d2）是否全系统统一？别两处算出两个 Cpk。
- 控制限是滚动还是冻结？当前滚动，确认客户接受"控制限会随时间移动"。
- A2/D3/D4 表覆盖 n=2..10，超出后的夹取行为是否有人工复核？
- Western Electric 4 条是否够用？按客户 SOP 增补到 8 条的成本评估。
- Pp/Ppk 是否已与 Cp/Cpk 分开计算？短期/长期能力不能混为一谈。
- `MAX_SUBGROUPS = 25` 的展示窗口是否需要可配置？

## 九、下一章预告：不合格品处置

SPC 回答了"过程稳不稳、能力够不够"，但还有一个最现实的环节没讲：**判了不合格之后，这批货到底怎么办？** 报废？返工？返修？退货？让步接收？五种处置怎么选、谁来选、系统的建议和人的决策怎么配合？

下一章讲**不合格品处置**——缺陷代码与严重度模型、`DispositionRuleEngine` 的规则（致命→报废/退货、严重→返工/返修、轻微→让步接收）、以及最关键的那个设计哲学：**为什么规则引擎只给"建议"，最终必须走 MRB 物料评审人工终审**。这是质量体系里"自动化"和"人负责"的边界所在。
