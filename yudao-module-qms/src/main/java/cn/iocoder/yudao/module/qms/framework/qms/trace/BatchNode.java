package cn.iocoder.yudao.module.qms.framework.qms.trace;

import java.time.LocalDateTime;

/**
 * 追溯图中的批次节点。由 QmsBatchDO 映射而来，只保留谱系渲染需要的字段。
 *
 * @param id           批次 ID
 * @param batchNo      批次号
 * @param productCode  产品编码
 * @param productName  产品名称
 * @param quantity     批次数量
 * @param status       批次状态（1=在制 ... 6=冻结）
 * @param machineCode  生产机台
 * @param shift        班次
 * @param produceTime  生产时间
 */
public record BatchNode(
        Long id,
        String batchNo,
        String productCode,
        String productName,
        Integer quantity,
        Integer status,
        String machineCode,
        String shift,
        LocalDateTime produceTime
) {
}
