package cn.iocoder.yudao.module.qms.service.trace;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsBatchDO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsTraceLinkDO;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsBatchMapper;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsTraceLinkMapper;
import cn.iocoder.yudao.module.qms.enums.QmsTraceDirection;
import cn.iocoder.yudao.module.qms.framework.qms.trace.BatchNode;
import cn.iocoder.yudao.module.qms.framework.qms.trace.GenealogyGraph;
import cn.iocoder.yudao.module.qms.framework.qms.trace.TraceLinkVO;
import cn.iocoder.yudao.module.qms.framework.qms.trace.TraceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * QMS 批次谱系追溯 Service 实现。
 *
 * 流程：读 qms_trace_link → 映射为 TraceLinkVO（补齐批次号标签）→ GenealogyGraph 正/反向 BFS
 * → 捞出命中批次的 DO → 组装 TraceResult。
 */
@Service
@RequiredArgsConstructor
public class QmsTraceServiceImpl implements QmsTraceService {

    private static final ErrorCode ERROR_CODE_BATCH_NOT_EXISTS = new ErrorCode(1_002_401, "批次不存在: id={}");

    private final QmsBatchMapper batchMapper;
    private final QmsTraceLinkMapper traceLinkMapper;

    @Override
    public TraceResult forwardTrace(Long batchId) {
        return trace(batchId, QmsTraceDirection.FORWARD, false);
    }

    @Override
    public TraceResult backwardTrace(Long batchId) {
        return trace(batchId, QmsTraceDirection.BACKWARD, false);
    }

    @Override
    public TraceResult traceReport(Long batchId) {
        return trace(batchId, QmsTraceDirection.FORWARD, true);
    }

    /**
     * 通用追溯。
     *
     * @param batchId  根批次
     * @param direction 追溯方向
     * @param merge     是否合并正反向（traceReport 用）
     */
    private TraceResult trace(Long batchId, QmsTraceDirection direction, boolean merge) {
        List<QmsTraceLinkDO> linkDOs = traceLinkMapper.selectListAll();

        // 一次性把链路端点 + 根批次对应的批次信息捞回来，既做链路标签，又做节点组装
        Set<Long> endpointIds = new HashSet<>();
        endpointIds.add(batchId);
        for (QmsTraceLinkDO link : linkDOs) {
            if (link.getFromBatchId() != null) endpointIds.add(link.getFromBatchId());
            if (link.getToBatchId() != null) endpointIds.add(link.getToBatchId());
        }
        Map<Long, QmsBatchDO> batchById = new HashMap<>();
        for (QmsBatchDO batch : batchMapper.selectListByIds(endpointIds)) {
            batchById.put(batch.getId(), batch);
        }
        if (!batchById.containsKey(batchId)) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_BATCH_NOT_EXISTS, batchId);
        }

        List<TraceLinkVO> links = new ArrayList<>();
        for (QmsTraceLinkDO link : linkDOs) {
            links.add(toLinkVO(link, batchById));
        }

        // 正/反向 BFS，去重合并节点
        Set<Long> nodeIds = new LinkedHashSet<>();
        int depth;
        if (merge) {
            GenealogyGraph.Traversal backward = GenealogyGraph.traverse(links, batchId, false);
            GenealogyGraph.Traversal forward = GenealogyGraph.traverse(links, batchId, true);
            nodeIds.addAll(backward.order());
            nodeIds.addAll(forward.order());
            depth = Math.max(backward.depth(), forward.depth());
        } else {
            boolean forward = direction == QmsTraceDirection.FORWARD;
            GenealogyGraph.Traversal traversal = GenealogyGraph.traverse(links, batchId, forward);
            nodeIds.addAll(traversal.order());
            depth = traversal.depth();
        }

        List<BatchNode> nodes = new ArrayList<>();
        for (Long id : nodeIds) {
            QmsBatchDO batch = batchById.get(id);
            if (batch != null) {
                nodes.add(toBatchNode(batch));
            }
        }

        // 只保留两端都落在谱系子图内的链路
        Set<Long> subgraphIds = new HashSet<>(nodeIds);
        subgraphIds.add(batchId);
        List<TraceLinkVO> usedLinks = links.stream()
                .filter(l -> subgraphIds.contains(l.fromBatchId()) && subgraphIds.contains(l.toBatchId()))
                .toList();

        BigDecimal totalQty = nodes.stream()
                .map(n -> BigDecimal.valueOf(n.quantity() == null ? 0 : n.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TraceResult(direction, toBatchNode(batchById.get(batchId)), nodes, usedLinks, depth, totalQty);
    }

    private TraceLinkVO toLinkVO(QmsTraceLinkDO link, Map<Long, QmsBatchDO> batchById) {
        return new TraceLinkVO(
                link.getFromBatchId(), batchNoOf(batchById, link.getFromBatchId()),
                link.getToBatchId(), batchNoOf(batchById, link.getToBatchId()),
                link.getLinkType(), link.getQuantity(), link.getLinkTime());
    }

    private String batchNoOf(Map<Long, QmsBatchDO> batchById, Long id) {
        QmsBatchDO batch = id == null ? null : batchById.get(id);
        return batch == null ? null : batch.getBatchNo();
    }

    private BatchNode toBatchNode(QmsBatchDO b) {
        if (b == null) {
            return null;
        }
        return new BatchNode(b.getId(), b.getBatchNo(), b.getProductCode(), b.getProductName(),
                b.getQuantity(), b.getStatus(), b.getMachineCode(), b.getShift(), b.getProduceTime());
    }
}
