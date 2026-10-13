package cn.iocoder.yudao.module.qms.framework.qms.trace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 追溯链路 VO——谱系图中的一条边。同时带上首尾批次号，便于前端直接渲染带标签的箭头。
 *
 * @param fromBatchId  上游批次 ID
 * @param fromBatchNo  上游批次号
 * @param toBatchId    下游批次 ID
 * @param toBatchNo    下游批次号
 * @param linkType     关系类型（1=投料 2=拼批 3=分拆 4=装配 5=返工）
 * @param quantity     流转数量
 * @param linkTime     流转时间
 */
public record TraceLinkVO(
        Long fromBatchId,
        String fromBatchNo,
        Long toBatchId,
        String toBatchNo,
        Integer linkType,
        BigDecimal quantity,
        LocalDateTime linkTime
) {
}
