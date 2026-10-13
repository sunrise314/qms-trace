package cn.iocoder.yudao.module.qms.framework.qms.spc;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * SPC 统计过程控制计算器（纯 Java 实现，不依赖 commons-math3）。
 *
 * 覆盖：描述统计（均值/极差/标准差）、X-Bar/R 控制图控制限（查 A2/D4/D3 系数）、
 * 过程能力指数（Cp/Cpk）、Western Electric 判异规则（简化 4 条）。
 *
 * A2/D3/D4 系数仅覆盖子组容量 n=2..10（工业常用区间）；n 超范围时按最近端点取值并需人工确认。
 */
@UtilityClass
public class SpcCalculator {

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

    /** X-Bar/R 控制图的控制限结果。 */
    public record XbarRChart(
            double xbarCl, double xbarUcl, double xbarLcl,
            double rCl, double rUcl, double rLcl
    ) {
    }

    /** 算术均值。空数组返回 0。 */
    public static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return 0d;
        }
        double sum = 0d;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /** 极差 R = 最大值 - 最小值。空数组返回 0。 */
    public static double range(double[] values) {
        if (values == null || values.length == 0) {
            return 0d;
        }
        double max = values[0];
        double min = values[0];
        for (double v : values) {
            if (v > max) max = v;
            if (v < min) min = v;
        }
        return max - min;
    }

    /** 样本标准差（除以 n-1，Bessel 校正）。样本数 < 2 返回 0。 */
    public static double stdDev(double[] values) {
        if (values == null || values.length < 2) {
            return 0d;
        }
        double avg = mean(values);
        double sumSq = 0d;
        for (double v : values) {
            double diff = v - avg;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (values.length - 1));
    }

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

    /**
     * 过程能力指数。
     *
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

    /**
     * Western Electric 判异规则（简化 4 条）。
     *
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

        // 规则 3：连续 6 点持续递增或递减
        int trendRun = 1;
        for (int i = 1; i < size; i++) {
            Double prev = points.get(i - 1);
            Double cur = points.get(i);
            if (prev == null || cur == null) {
                trendRun = 1;
                continue;
            }
            boolean sameTrend = (cur > prev && trendRun > 0) || (cur < prev && trendRun < 0);
            if (sameTrend) {
                trendRun += (trendRun > 0 ? 1 : -1);
            } else {
                trendRun = cur > prev ? 1 : -1;
            }
            if (Math.abs(trendRun) >= 6) {
                violations.add("规则3：连续 6 点持续" + (trendRun > 0 ? "递增" : "递减")
                        + "（第 " + (i - 4) + "-" + (i + 1) + " 点）");
            }
        }

        // 规则 4：连续 14 点交替上下
        int alternatingRun = 1;
        for (int i = 2; i < size; i++) {
            Double a = points.get(i - 2);
            Double b = points.get(i - 1);
            Double c = points.get(i);
            if (a == null || b == null || c == null) {
                alternatingRun = 1;
                continue;
            }
            boolean zigzag = (b > a && c < b) || (b < a && c > b);
            alternatingRun = zigzag ? alternatingRun + 1 : 1;
            if (alternatingRun >= 14) {
                violations.add("规则4：连续 14 点交替上下（第 " + (i - 12) + "-" + (i + 1) + " 点）");
            }
        }

        return violations;
    }

    /** 取子组最大容量，用于查系数（各子组应等容量，此处防御性取最大）。 */
    private static int largestSize(List<double[]> subgroups) {
        int n = 0;
        for (double[] subgroup : subgroups) {
            if (subgroup != null && subgroup.length > n) {
                n = subgroup.length;
            }
        }
        return n;
    }

    /** 查系数表，超范围时按最近端点夹取。 */
    private static double coefficient(double[] table, int n) {
        if (n <= 0) {
            return 0d;
        }
        if (n >= table.length) {
            return table[table.length - 1];
        }
        return table[n];
    }
}
