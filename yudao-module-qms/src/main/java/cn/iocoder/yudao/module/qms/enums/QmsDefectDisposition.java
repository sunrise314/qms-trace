package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 不合格品处置方式。NULL 表示尚未处置。
 */
@Getter
@AllArgsConstructor
public enum QmsDefectDisposition {

    REWORK(1, "返工"),
    REPAIR(2, "返修"),
    CONCESSION(3, "让步接收"),
    SCRAP(4, "报废"),
    RETURN(5, "退货");

    private final Integer code;
    private final String name;
}
