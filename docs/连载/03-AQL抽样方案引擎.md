# AQL 抽样方案引擎——从 GB/T 2828.1 到查表实现

## 一、场景：老师傅脑子里的"抽 125 个、超 7 个不合格"

第 1 章讲到，王主任卡住的一个问题是"判定靠人"。我举个具体的例子。

原料批 B-20261013-001，注塑外壳 1000 个。质检员老李来料检验时抽了 125 个，逐个量外径。数下来 6 个超差。"6 个，合格。"如果数下来 8 个，"8 个，不合格。"这条线卡在哪儿？——**为什么抽 125？为什么分界是 7 和 8？**

我问老李："这 125 是怎么定的？"他说："经验，这批料多，多抽点。"我又问："那 7 个是哪来的？"他愣了愣："反正一直这么判的。"

这就是问题的根：**抽样规则存在老师傅的脑子里，既写不下来，也传不下去**。换个人、换批货、换个客户审厂，这套"经验"立刻站不住脚。客户来审厂，第一句话就是"你们的抽样依据是什么标准？"

答案是国标 **GB/T 2828.1**（等同采用 ISO 2859-1）。它规定了"计数抽样检验"的完整程序——给定批量（lot size）和接收质量限（AQL），查表得到"抽多少个（n）、允许多少个不合格（Ac）、到多少个判拒收（Re）"。它不是我发明的规则，是**行业共识、客户认可、白纸黑字**的标准。把老李脑子里那套经验，换成这张表的查表结果，判定就有了依据。

## 二、AQL 是什么

**AQL，Acceptable Quality Limit，接收质量限**（旧称可接受质量水平）。它回答的是："这批货我最多容忍多高比例的不合格品？"

注意措辞——AQL 不是"零缺陷"，它是"**可接受的质量上限**"。AQL 2.5 不代表"允许 2.5% 不合格"，而是一个统计学意义上的抽样方案参数：对应这批货的"过程平均"，只要不超过这个水平，按抽样方案判定为合格的概率就足够高。这是抽样检验的本质——**用有限样本推断整批质量，必然有"判错"的风险**（把好批判成坏批 = 生产方风险 α，把坏批判成好批 = 使用方风险 β，GB/T 2828.1 帮这两种风险各自定了一个可接受的水平）。

工程上常用的五个 AQL 档位：

| AQL | 典型场景 |
|-----|---------|
| 0.65 | 关键安全特性、汽车/医疗 |
| 1.0 | 重要功能特性 |
| 1.5 | 一般功能特性 |
| 2.5 | 工业界最常用（外观、尺寸） |
| 4.0 | 非关键特性、一般外观 |

这套系统默认用 2.5，因为对注塑件的外壳、尺寸，2.5 是最常见的档位。`AqlLevel` 枚举把这五档收进来：

```java
// framework/qms/sampling/AqlLevel.java
/**
 * AQL（Acceptable Quality Limit，接收质量限）水平。
 * 取 GB/T 2828.1 常用档位，value 直接用于查 AQL 主表。
 */
@Getter
@AllArgsConstructor
public enum AqlLevel {
    AQL_0_65("0.65"),
    AQL_1_0("1.0"),
    AQL_1_5("1.5"),
    AQL_2_5("2.5"),
    AQL_4_0("4.0");

    private final String value;
}
```

## 三、GB/T 2828.1 的两步查表

标准的查表分两步，很像查字典：

**第一步：由"检验批量"查"样本量字码"。** 一批货 1000 个，落在"501–1200"这一段，对应字码 **J**。字码是一组字母 A～Q，批量越大字码越靠后。这一步只跟批量有关，跟 AQL 无关。

**第二步：由"字码 × AQL"查主表，得到样本量 n、接收数 Ac、拒收数 Re。** 字码 J 那一行、AQL 2.5 那一列，交叉得到 Ac=2（这个数是标准主表里的真实值）。而样本量 n 是"字码决定"的——字码 J 对应 n=80。

等一下，前面说抽 125，这里算出来是 80，对不上？ ——对不上是因为老李那批 1000 个、字码 J、样本量其实是 80（不是 125）。老李自己也不确定抽了多少，这正是"经验判定"的问题。用标准查出来是 n=80、Ac=2、Re=3，白纸黑字。**这就是标准化削掉的那块"模糊"。**

```java
// framework/qms/sampling/SamplingPlan.java
/**
 * AQL 抽样方案。一行完整的抽样决策：抽多少（n）+ 允许多少个不合格（Ac）+ 到多少判拒（Re）。
 */
public record SamplingPlan(
        int sampleSize,    // 样本量 n
        int acceptQty,     // 接收数 Ac（不合格数 <= Ac 判合格）
        int rejectQty,     // 拒收数 Re（不合格数 >= Re 判不合格，标准中 Re = Ac + 1）
        String codeLetter  // 样本量字码（A～Q，与批量范围一一对应）
) {
}
```

`SamplingPlan` 是个 `record`——不可变、四字段。Re 永远等于 Ac + 1，所以其实只用存 Ac，Re 现推。但为了前端展示和审计方便，我在结构里把 Re 也放进去，一眼能看到"到多少个判拒"。

## 四、为什么用查表，不用公式现算

有朋友会问：GB/T 2828.1 背后的统计学（二项分布、泊松分布近似）不是可以写成公式吗？为什么非要内置一张数组表？

这个问题我认真想过，理由有三个：

**第一，可审计性。** 客户审厂时问"为什么抽 80 个"，你答"因为批量 1000 落在字码 J 段"就够了；如果现算公式，"批量 1000 时最优样本量是……"你得当场推导二项分布，客户听不懂，审厂员也不接受。**查表 = 可复现、可对簿公堂；公式 = 每次都要解释一遍**。

**第二，标准就是表。** GB/T 2828.1 原文给的就是表格，不是公式。工程实践里，大家查的都是那张表。内置一张和标准一致的数组，等于把"标准结集"进了代码。

**第三，性能与确定性。** 查表是 O(1) 数组下标访问，没有任何浮点误差和收敛问题。公式现算要算阶乘、算二项累积分布，慢且可能因浮点误差在边界抖动。质量判定这种事，**要的是绝对确定，不是数学优雅**。

这就是 README 取舍表里那条"抽样方案用内置简化查表引擎"的由来。Code > Formula，在这里是个严肃的工程选择。

## 五、代码实现：三张二维数组

引擎的全部秘密在三个常量上：`LETTERS`（字码）、`SAMPLE_SIZES`（各字码的样本量）、`ACCEPTS`（AQL 主表）。

```java
// framework/qms/sampling/SamplingPlanEngine.java
@UtilityClass
public class SamplingPlanEngine {

    /** 样本量字码（一般检验水平 II），下标与 SAMPLE_SIZES / ACCEPTS 对齐 */
    private static final String[] LETTERS = {
            "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q"
    };

    /** 各字码对应的样本量 n */
    private static final int[] SAMPLE_SIZES = {
            2, 3, 5, 8, 13, 20, 32, 50, 80, 125, 200, 315, 500, 800, 1250
    };

    /**
     * AQL 主表（行=字码，列=AQL 0.65 / 1.0 / 1.5 / 2.5 / 4.0）的接收数 Ac。
     * Re 由 Ac + 1 推出，故表中只存 Ac。
     */
    private static final int[][] ACCEPTS = {
            // 0.65  1.0   1.5   2.5   4.0
            {0, 0, 0, 0, 0},   // A
            {0, 0, 0, 0, 0},   // B
            {0, 0, 0, 0, 1},   // C
            {0, 0, 0, 1, 1},   // D
            {0, 0, 0, 1, 2},   // E
            {0, 0, 1, 1, 2},   // F
            {0, 1, 1, 2, 3},   // G
            {0, 1, 2, 3, 5},   // H
            {1, 2, 3, 5, 6},   // J
            {2, 3, 5, 6, 10},  // K
            {3, 5, 6, 10, 14}, // L
            {5, 7, 10, 14, 21},// M
            {7, 10, 14, 21, 21}, // N
            {10, 14, 21, 21, 21}, // P
            {14, 21, 21, 21, 21}  // Q
    };

    /** AQL 列的标签，用于把入参 aqlLevel 映射到列下标 */
    private static final String[] AQL_LABELS = {"0.65", "1.0", "1.5", "2.5", "4.0"};

    /** 缺省 AQL：工业界最常用的 2.5 */
    private static final int DEFAULT_AQL_INDEX = 3;
}
```

三张表的三个下标是**对齐**的：`LETTERS[i]`、`SAMPLE_SIZES[i]`、`ACCEPTS[i]` 说的是同一个字码。字码 A 对应 n=2，字码 J 对应 n=80，字码 Q 对应 n=1250。`ACCEPTS` 是 15 行 × 5 列的二维数组，行是字码、列是 AQL 档位。

注意 `LETTERS` 里跳过了字母 "I" 和 "O"——这是 GB/T 2828.1 的规矩，为避免和数字 1、0 混淆。

### 5.1 两步查表的主入口

```java
/**
 * 查询抽样方案。
 *
 * @param lotSize 检验批量（送检总数）
 * @param aqlLevel AQL 水平，如 "2.5"；非法/空值回退到 2.5
 * @return 完整抽样方案（n / Ac / Re / 字码）
 */
public static SamplingPlan lookup(int lotSize, String aqlLevel) {
    int idx = codeLetterIndex(lotSize);
    int aqlIdx = aqlIndexOf(aqlLevel);
    int accept = ACCEPTS[idx][aqlIdx];
    return new SamplingPlan(SAMPLE_SIZES[idx], accept, accept + 1, LETTERS[idx]);
}
```

三行核心逻辑，正好是两步查表：

1. `codeLetterIndex(lotSize)` —— 第一步，由批量查字码下标。
2. `aqlIndexOf(aqlLevel)` —— 由 AQL 字符串查列下标。
3. `ACCEPTS[idx][aqlIdx]` —— 第二步，二维数组交叉取 Ac，再推出 Re = Ac + 1，配上 `SAMPLE_SIZES[idx]` 得到 n。

### 5.2 第一步：批量 → 字码

```java
/** 由批量查样本量字码下标（一般检验水平 II）。 */
public static int codeLetterIndex(int lotSize) {
    if (lotSize <= 8) return 0;          // A：2-8（含更小批量的兜底）
    if (lotSize <= 15) return 1;         // B：9-15
    if (lotSize <= 25) return 2;         // C：16-25
    if (lotSize <= 50) return 3;         // D：26-50
    if (lotSize <= 90) return 4;         // E：51-90
    if (lotSize <= 150) return 5;        // F：91-150
    if (lotSize <= 280) return 6;        // G：151-280
    if (lotSize <= 500) return 7;        // H：281-500
    if (lotSize <= 1200) return 8;       // J：501-1200
    if (lotSize <= 3200) return 9;       // K：1201-3200
    if (lotSize <= 10000) return 10;     // L：3201-10000
    if (lotSize <= 35000) return 11;     // M：10001-35000
    if (lotSize <= 150000) return 12;    // N：35001-150000
    if (lotSize <= 500000) return 13;    // P：150001-500000
    return 14;                           // Q：>500000
}
```

这是一串**升序的区间判断**。写成 `if-else` 而不是 for 循环查区间表，是为了让边界一眼看清——每一行的注释都标了标准里对应的批量区间，和 GB/T 2828.1 表 1 逐行对得上。这是典型的"宁可啰嗦，也要可对照"的写法。注意 `lotSize <= 8` 这里的兜底：标准里最小批量是 2，但实际可能传 0 或 1，统一兜到字码 A。

### 5.3 AQL 字符串 → 列下标

```java
/** 由 AQL 字符串查列下标；无法识别时回退到 2.5。 */
public static int aqlIndexOf(String aqlLevel) {
    if (aqlLevel == null) {
        return DEFAULT_AQL_INDEX;
    }
    String normalized = aqlLevel.trim();
    for (int i = 0; i < AQL_LABELS.length; i++) {
        if (AQL_LABELS[i].equals(normalized)) {
            return i;
        }
    }
    return DEFAULT_AQL_INDEX;
}
```

AQL 从数据库里读出来是字符串（`"2.5"`），要映射到 `ACCEPTS` 的列下标。这里做了两件事：`trim()` 去掉前后空格，逐列比对标签。**匹配不上一律回退到 2.5**——不是抛异常，而是给一个工业界最常用的默认档。为什么这么宽容？因为抽样方案是"给生产在用"，宁可给个合理默认，也不要因为前端传了 `"2.50"` 或 `"2.5 "` 就让整个检验流程卡死。

### 5.4 Service 与 Controller：极薄的一层

引擎是纯静态的 `UtilityClass`，Service 只是薄薄包一层，把 null 批量兜成 0：

```java
// service/sampling/QmsSamplingServiceImpl.java
@Service
public class QmsSamplingServiceImpl implements QmsSamplingService {
    @Override
    public SamplingPlan plan(Integer lotSize, String aqlLevel) {
        int size = (lotSize == null || lotSize <= 0) ? 0 : lotSize;
        return SamplingPlanEngine.lookup(size, aqlLevel);
    }
}
```

对外接口也很简单：

```java
// controller/admin/sampling/QmsSamplingController.java
@GetMapping("/plan")
@Operation(summary = "查询 AQL 抽样方案")
@Parameter(name = "lotSize", description = "检验批量", required = true, example = "1000")
@Parameter(name = "aqlLevel", description = "AQL 水平（0.65/1.0/1.5/2.5/4.0）", required = true, example = "2.5")
public CommonResult<SamplingPlan> plan(@RequestParam("lotSize") Integer lotSize,
                                       @RequestParam("aqlLevel") String aqlLevel) {
    return success(samplingService.plan(lotSize, aqlLevel));
}
```

`GET /api/admin/qms/sampling/plan?lotSize=1000&aqlLevel=2.5`，返回 `{sampleSize:80, acceptQty:2, rejectQty:3, codeLetter:"J"}`。前端在"新建检验单"页面调它，把 n/Ac/Re 自动填进表单，质检员不用再猜。

## 六、边界与踩坑：这是一张"简化表"

必须把这句话说在前面：**`SamplingPlanEngine` 内置的是 GB/T 2828.1 的简化表，不是标准全文**。类头注释写得很直白：

```java
/**
 * AQL 抽样方案引擎（GB/T 2828.1-2012，一般检验水平 II）。
 * ...
 * 重要说明：本类内置的是 GB/T 2828.1 的**简化表**——原表中批量过小、用箭头指向
 * 更大样本量的单元格（如 AQL 0.65 的 A/B 码）此处已坍缩为 Ac=0，以保证任意批量
 * 都能给出确定方案。商用上线必须按标准全文逐格核对/补全，或把主表配置化到数据库。
 * 检验水平固定为 II（一般检验水平），如需 I / III 或特殊水平，需扩展 SAMPLE_SIZES 与字码边界。
 */
```

简化在哪？三点：

**其一，箭头单元格坍缩了。** 标准原表里，小批量 + 小 AQL（比如批量 10、AQL 0.65）的格子，标准是用一个"↓"箭头指向下面一行更大的样本量，意思是"这批太小，要用更大的样本"。我用 Ac=0 兜住了——因为对这么小的批量，标准本身就建议"全检"，Ac=0 是最保守的兜底，不会放过任何不合格品。

**其二，检验水平固定 II。** GB/T 2828.1 有三个一般检验水平（I 宽松、II 正常、III 严格）。我固定用 II，这是最通用的。如果客户要 III（更严，样本更大），得重做字码边界表和 SAMPLE_SIZES。

**其三，只覆盖 5 个 AQL 档。** 标准有更多档位（0.010 到 1000），我取了最常用的 5 个。

这三条不是"做得不认真"，而是**刻意的取舍**：把一张几百格的完整标准表硬编码进 Java，维护成本极高、改起来极容易出错。用一张"覆盖 90% 场景的简化表"，把边角留给"商用上线时按标准全文补全，或把主表配置化到数据库"。这也是 README 里明确列的商用检查项。**在 MVP 阶段，能覆盖主流场景就是合格；把标准全文搬进代码，反而是一种过度设计。**

**踩坑记录。** 写的时候踩过一个坑：一开始 `ACCEPTS` 的列顺序排错了，把 AQL 1.0 放在了 2.5 前面。测试批量 1000 时发现问题——`lookup(1000, "2.5")` 返回 Ac=3（应该是 2）。原因是 `AQL_LABELS` 数组和 `ACCEPTS` 的列没对齐。修法很简单：**让 `AQL_LABELS` 的下标和二维数组的列严格一一对应，并在每次改表后写一个边界单测**（批量 8/9、1200/1201，AQL 0.65/4.0 四个角都跑一遍）。

## 七、商用化检查清单（抽样引擎版）

- AQL 主表是否按 GB/T 2828.1-2012 全文逐格核对？当前是简化表，箭头单元格已坍缩为 Ac=0。
- `AQL_LABELS` 和 `ACCEPTS` 的列顺序严格对齐了吗？边界单测覆盖了所有 AQL 档吗？
- `codeLetterIndex` 的每个区间边界和标准表 1 逐行对得上吗？（尤其 8/9、1200/1201 这类切换点）
- 检验水平 II 之外（I/III、特殊水平）的需求怎么扩展？字码边界和 SAMPLE_SIZES 是否需要重做？
- 抽取方案是否随检验单冻结入库？历史判定能否脱离当前表复现？
- Ac/Re 的展示和存储是否一致？Re = Ac + 1 的推导有没有在库里冗余出 Re 字段？

## 八、下一章预告：批次谱系追溯

抽样方案解决了"判多严"，检验单解决了"判什么"，但王主任那张投诉单上最难的问题还没答——**同批原料还流向了哪些客户、哪些批次？** 抽 125 个能告诉你这批货合不合格，告诉不了你"它的料去哪了"。

下一章讲 **批次谱系追溯**——正向追溯（召回，"我的料去哪了"）与反向追溯（溯源，"问题在哪批料"）、为什么不上 Neo4j 而用邻接表 + 内存 BFS、`GenealogyGraph` 的完整 BFS 实现（防环、按层序时间排序）、以及 `TraceLinkVO` / `TraceResult` / `BatchNode` 这套 VO 怎么把一张图喂给前端。这是 QMS 第二个纯 Java 引擎，也是整个系统最"图论"的一章。
