package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 缺陷严重度。严重度直接决定不合格品处置规则的建议方向（致命→报废/退货，严重→返工/返修，轻微→让步接收）。
 */
@Getter
@AllArgsConstructor
public enum QmsDefectSeverity {

    CRITICAL(1, "致命"),
    MAJOR(2, "严重"),
    MINOR(3, "轻微");

    private final Integer code;
    private final String name;
}
