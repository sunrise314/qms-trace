package cn.iocoder.yudao.module.qms.framework.qms.sampling;

import lombok.experimental.UtilityClass;

/**
 * AQL 抽样方案引擎（GB/T 2828.1-2012，一般检验水平 II）。
 *
 * 查表分两步：
 * 1. 由"检验批量"落在哪一段区间，得到"样本量字码"（A～Q）；
 * 2. 由"字码 × AQL"查主表，得到样本量 n、接收数 Ac、拒收数 Re（Re = Ac + 1）。
 *
 * 重要说明：本类内置的是 GB/T 2828.1 的**简化表**——原表中批量过小、用箭头指向
 * 更大样本量的单元格（如 AQL 0.65 的 A/B 码）此处已坍缩为 Ac=0，以保证任意批量
 * 都能给出确定方案。商用上线必须按标准全文逐格核对/补全，或把主表配置化到数据库。
 * 检验水平固定为 II（一般检验水平），如需 I / III 或特殊水平，需扩展 SAMPLE_SIZES 与字码边界。
 */
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
}
