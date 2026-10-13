package cn.iocoder.yudao.module.qms.framework.qms.spc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPC 控制图上的一个点（一个子组）。
 *
 * @param subgroupIndex 子组序号
 * @param meanValue     子组均值（X-Bar 图纵坐标）
 * @param rangeValue    子组极差（R 图纵坐标）
 * @param sampleTime    采样时间
 */
public record SpcPointVO(
        Integer subgroupIndex,
        BigDecimal meanValue,
        BigDecimal rangeValue,
        LocalDateTime sampleTime
) {
}
