package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 检验单判定结果。
 */
@Getter
@AllArgsConstructor
public enum QmsInspectionResult {

    PENDING(0, "待检"),
    PASS(1, "合格"),
    FAIL(2, "不合格"),
    CONCESSION(3, "让步接收");

    private final Integer code;
    private final String name;
}
