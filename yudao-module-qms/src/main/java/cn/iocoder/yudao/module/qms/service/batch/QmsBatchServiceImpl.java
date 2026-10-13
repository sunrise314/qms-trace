package cn.iocoder.yudao.module.qms.service.batch;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsBatchDO;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsBatchMapper;
import cn.iocoder.yudao.module.qms.enums.QmsBatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * QMS 批次 Service 实现。
 */
@Service
@RequiredArgsConstructor
public class QmsBatchServiceImpl implements QmsBatchService {

    private static final ErrorCode ERROR_CODE_NOT_EXISTS = new ErrorCode(1_002_301, "批次不存在: id={}");

    private final QmsBatchMapper batchMapper;

    @Override
    public Long createBatch(QmsBatchSaveReqVO createReqVO) {
        QmsBatchDO batch = new QmsBatchDO();
        batch.setBatchNo(createReqVO.batchNo());
        batch.setProductCode(createReqVO.productCode());
        batch.setProductName(createReqVO.productName());
        batch.setQuantity(createReqVO.quantity());
        batch.setProductionOrderCode(createReqVO.productionOrderCode());
        batch.setMachineCode(createReqVO.machineCode());
        batch.setShift(createReqVO.shift());
        batch.setParentBatchId(createReqVO.parentBatchId());
        batch.setAttributes(createReqVO.attributes());
        batch.setStatus(createReqVO.status() != null ? createReqVO.status() : QmsBatchStatus.IN_PROCESS.getCode());
        batch.setProduceTime(createReqVO.produceTime());
        batch.setExpiryTime(createReqVO.expiryTime());
        batch.setRemark(createReqVO.remark());
        batchMapper.insert(batch);
        return batch.getId();
    }

    @Override
    public void updateBatch(QmsBatchSaveReqVO updateReqVO) {
        validateBatchExists(updateReqVO.id());
        QmsBatchDO updateObj = new QmsBatchDO();
        updateObj.setId(updateReqVO.id());
        updateObj.setBatchNo(updateReqVO.batchNo());
        updateObj.setProductCode(updateReqVO.productCode());
        updateObj.setProductName(updateReqVO.productName());
        updateObj.setQuantity(updateReqVO.quantity());
        updateObj.setProductionOrderCode(updateReqVO.productionOrderCode());
        updateObj.setMachineCode(updateReqVO.machineCode());
        updateObj.setShift(updateReqVO.shift());
        updateObj.setParentBatchId(updateReqVO.parentBatchId());
        updateObj.setAttributes(updateReqVO.attributes());
        updateObj.setStatus(updateReqVO.status());
        updateObj.setProduceTime(updateReqVO.produceTime());
        updateObj.setExpiryTime(updateReqVO.expiryTime());
        updateObj.setRemark(updateReqVO.remark());
        batchMapper.updateById(updateObj);
    }

    @Override
    public void deleteBatch(Long id) {
        validateBatchExists(id);
        batchMapper.deleteById(id);
    }

    @Override
    public QmsBatchDO getBatch(Long id) {
        QmsBatchDO batch = batchMapper.selectById(id);
        if (batch == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
        return batch;
    }

    @Override
    public PageResult<QmsBatchDO> getBatchPage(QmsBatchPageReqVO pageReqVO) {
        return batchMapper.selectPage(pageReqVO);
    }

    @Override
    public QmsBatchDO getByBatchNo(String batchNo) {
        QmsBatchDO batch = batchMapper.selectByBatchNo(batchNo);
        if (batch == null) {
            throw ServiceExceptionUtil.exception(new ErrorCode(1_002_302, "批次号不存在: {}", batchNo), batchNo);
        }
        return batch;
    }

    @Override
    public void release(Long batchId) {
        validateBatchExists(batchId);
        updateStatus(batchId, QmsBatchStatus.QUALIFIED.getCode());
    }

    @Override
    public void freeze(Long batchId) {
        validateBatchExists(batchId);
        updateStatus(batchId, QmsBatchStatus.FROZEN.getCode());
    }

    private void updateStatus(Long batchId, Integer status) {
        QmsBatchDO updateObj = new QmsBatchDO();
        updateObj.setId(batchId);
        updateObj.setStatus(status);
        batchMapper.updateById(updateObj);
    }

    private void validateBatchExists(Long id) {
        if (batchMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
    }
}
