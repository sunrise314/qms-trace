package cn.iocoder.yudao.module.qms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批次追溯方向。
 * 正向（FORWARD）：从原材料批次出发，查我的料流向了哪些成品——召回场景；
 * 反向（BACKWARD）：从成品/不合格批次出发，查它用了哪些上游料——溯源场景。
 */
@Getter
@AllArgsConstructor
public enum QmsTraceDirection {

    FORWARD(1, "正向追溯"),
    BACKWARD(2, "反向追溯");

    private final Integer code;
    private final String name;
}
