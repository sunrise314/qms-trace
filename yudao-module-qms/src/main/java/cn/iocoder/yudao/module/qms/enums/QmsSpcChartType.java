package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SPC 控制图类型。
 * 计量型：X-Bar/R（均值极差图）、X-MR（单值移动极差图）；
 * 计数型：P（不合格品率）、NP（不合格品数）、C（缺陷数）、U（单位缺陷数）。
 */
@Getter
@AllArgsConstructor
public enum QmsSpcChartType {

    XBAR_R(1, "X-Bar/R 均值极差图"),
    XMR(2, "X-MR 单值移动极差图"),
    P(3, "P 不合格品率图"),
    NP(4, "NP 不合格品数图"),
    C(5, "C 缺陷数图"),
    U(6, "U 单位缺陷数图");

    private final Integer code;
    private final String name;
}
