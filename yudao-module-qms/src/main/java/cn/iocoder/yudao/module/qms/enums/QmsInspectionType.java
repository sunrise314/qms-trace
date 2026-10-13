package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检验类型：按检验发生在生产流程中的位置划分。
 */
@Getter
@AllArgsConstructor
public enum QmsInspectionType {

    IQC(1, "来料检验"),
    IPQC(2, "过程检验"),
    FQC(3, "成品检验"),
    OQC(4, "出货检验");

    private final Integer code;
    private final String name;
}
