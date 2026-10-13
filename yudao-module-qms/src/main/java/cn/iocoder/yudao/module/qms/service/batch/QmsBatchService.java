package cn.iocoder.yudao.module.qms.service.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsBatchDO;

/**
 * QMS 批次 Service 接口。
 */
public interface QmsBatchService {

    /** 创建批次 */
    Long createBatch(QmsBatchSaveReqVO createReqVO);

    /** 更新批次 */
    void updateBatch(QmsBatchSaveReqVO updateReqVO);

    /** 删除批次 */
    void deleteBatch(Long id);

    /** 根据 ID 查询 */
    QmsBatchDO getBatch(Long id);

    /** 分页查询 */
    PageResult<QmsBatchDO> getBatchPage(QmsBatchPageReqVO pageReqVO);

    /** 根据批次号查询 */
    QmsBatchDO getByBatchNo(String batchNo);

    /** 批次放行（status=QUALIFIED） */
    void release(Long batchId);

    /** 冻结批次（status=FROZEN） */
    void freeze(Long batchId);
}
