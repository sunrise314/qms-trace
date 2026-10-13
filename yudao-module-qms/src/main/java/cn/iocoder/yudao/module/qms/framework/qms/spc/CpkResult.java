package cn.iocoder.yudao.module.qms.framework.qms.spc;

/**
 * 过程能力指数结果。
 *
 * Cp：潜在能力（只看规格宽度与过程波动，不考虑偏移）；
 * Cpk：实际能力（考虑过程均值与规格中心的偏移，通常用 Cpk 更保守）；
 * Pp/Ppk：长期过程能力（用总体标准差），与 Cp/Cpk 的区别在标准差口径（组内 vs 整体）。
 * 本引擎仅有组内标准差，故 pp/ppk 与 cp/cpk 同值，后续接入长期数据后可分开计算。
 *
 * @param cp     潜在能力指数
 * @param cpk    实际能力指数
 * @param pp     长期潜在能力指数
 * @param ppk    长期实际能力指数
 * @param mean   过程均值
 * @param stdDev 过程标准差
 */
public record CpkResult(
        double cp,
        double cpk,
        double pp,
        double ppk,
        double mean,
        double stdDev
) {
}
