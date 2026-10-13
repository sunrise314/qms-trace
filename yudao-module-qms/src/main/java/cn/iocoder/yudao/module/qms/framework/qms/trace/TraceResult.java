package cn.iocoder.yudao.module.qms.framework.qms.trace;

import cn.iocoder.yudao.module.qms.enums.QmsTraceDirection;

import java.math.BigDecimal;
import java.util.List;

/**
 * 追溯结果。正向（召回）与反向（溯源）共用同一个结构，仅 direction 不同。
 *
 * @param direction 追溯方向（1=正向追溯 2=反向追溯）
 * @param rootBatch 根批次节点（追溯起点，不重复放进 nodes）
 * @param nodes     命中的关联批次节点（不含根批次），按 BFS 层序排列
 * @param links     命中的关联链路（谱系图的边）
 * @param depth     追溯深度（根批次为 0，最远一层为 depth）
 * @param totalQty  关联批次的合计数量（正向=受影响的下游产出量，反向=投入的上游料量）
 */
public record TraceResult(
        QmsTraceDirection direction,
        BatchNode rootBatch,
        List<BatchNode> nodes,
        List<TraceLinkVO> links,
        int depth,
        BigDecimal totalQty
) {
}
