package cn.iocoder.yudao.module.qms.framework.qms.sampling;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
