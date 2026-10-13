package cn.iocoder.yudao.module.qms.framework.qms.sampling;

/**
 * AQL 抽样方案。一行完整的抽样决策：抽多少（n）+ 允许多少个不合格（Ac）+ 到多少判拒（Re）。
 *
 * @param sampleSize  样本量 n
 * @param acceptQty   接收数 Ac（不合格数 <= Ac 判合格）
 * @param rejectQty   拒收数 Re（不合格数 >= Re 判不合格，标准中 Re = Ac + 1）
 * @param codeLetter  样本量字码（A～Q，与批量范围一一对应）
 */
public record SamplingPlan(
        int sampleSize,
        int acceptQty,
        int rejectQty,
        String codeLetter
) {
}
