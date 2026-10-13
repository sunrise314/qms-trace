package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批次/序列号状态。批次是追溯的主体，状态随检验结果与放行/冻结操作流转。
 */
@Getter
@AllArgsConstructor
public enum QmsBatchStatus {

    IN_PROCESS(1, "在制"),
    PENDING(2, "待检"),
    QUALIFIED(3, "合格"),
    UNQUALIFIED(4, "不合格"),
    SHIPPED(5, "已出货"),
    FROZEN(6, "冻结");

    private final Integer code;
    private final String name;
}
