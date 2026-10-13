package cn.iocoder.yudao.module.qms.service.trace;

import cn.iocoder.yudao.module.qms.framework.qms.trace.TraceResult;

/**
 * QMS 批次谱系追溯 Service 接口。
 */
public interface QmsTraceService {

    /**
     * 正向追溯（召回）：从上游批次出发，查它的料流向了哪些下游批次。
     */
    TraceResult forwardTrace(Long batchId);

    /**
     * 反向追溯（溯源）：从下游批次出发，查它用了哪些上游批次。
     */
    TraceResult backwardTrace(Long batchId);

    /**
     * 完整追溯报告：同时做正反向遍历，合并出该批次所处的整张谱系子图。
     */
    TraceResult traceReport(Long batchId);
}
