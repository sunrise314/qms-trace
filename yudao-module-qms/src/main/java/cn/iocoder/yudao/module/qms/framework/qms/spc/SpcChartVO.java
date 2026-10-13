package cn.iocoder.yudao.module.qms.framework.qms.spc;

import cn.iocoder.yudao.module.qms.enums.QmsSpcChartType;

import java.math.BigDecimal;
import java.util.List;

/**
 * SPC 控制图响应 VO。前端 Canvas / ECharts 直接渲染：points 是折线，cl/ucl/lcl 是三条参考线。
 *
 * @param productCode       产品编码
 * @param characteristicCode 特性编码
 * @param chartType         控制图类型
 * @param points            子组序列
 * @param cl                中心线
 * @param ucl               上控制限
 * @param lcl               下控制限
 * @param cp                过程潜在能力指数
 * @param cpk               过程实际能力指数
 * @param violations        判异规则命中描述
 */
public record SpcChartVO(
        String productCode,
        String characteristicCode,
        QmsSpcChartType chartType,
        List<SpcPointVO> points,
        BigDecimal cl,
        BigDecimal ucl,
        BigDecimal lcl,
        BigDecimal cp,
        BigDecimal cpk,
        List<String> violations
) {
}
