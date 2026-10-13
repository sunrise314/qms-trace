package cn.iocoder.yudao.module.qms.service.inspectionorder;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsBatchDO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsDefectDO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionItemDO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionOrderDO;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsBatchMapper;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsDefectMapper;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsInspectionItemMapper;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsInspectionOrderMapper;
import cn.iocoder.yudao.module.qms.enums.QmsBatchStatus;
import cn.iocoder.yudao.module.qms.enums.QmsDefectDisposition;
import cn.iocoder.yudao.module.qms.enums.QmsDefectSeverity;
import cn.iocoder.yudao.module.qms.enums.QmsInspectionResult;
import cn.iocoder.yudao.module.qms.framework.qms.rule.DispositionRuleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QMS 检验单 Service 实现。
 */
@Service
@RequiredArgsConstructor
public class QmsInspectionOrderServiceImpl implements QmsInspectionOrderService {

    private static final ErrorCode ERROR_CODE_NOT_EXISTS = new ErrorCode(1_002_001, "检验单不存在: id={}");

    /** 检验项判定为不合格的取值 */
    private static final int ITEM_RESULT_FAIL = 2;

    private final QmsInspectionOrderMapper inspectionOrderMapper;
    private final QmsInspectionItemMapper inspectionItemMapper;
    private final QmsDefectMapper defectMapper;
    private final QmsBatchMapper batchMapper;

    @Override
    public Long createInspectionOrder(QmsInspectionOrderSaveReqVO createReqVO) {
        QmsInspectionOrderDO order = new QmsInspectionOrderDO();
        order.setCode(createReqVO.code());
        order.setInspectionType(createReqVO.inspectionType());
        order.setBatchId(createReqVO.batchId());
        order.setWorkOrderCode(createReqVO.workOrderCode());
        order.setProductCode(createReqVO.productCode());
        order.setLotSize(createReqVO.lotSize());
        order.setSampleQty(createReqVO.sampleQty());
        order.setAqlLevel(createReqVO.aqlLevel());
        order.setAcceptQty(createReqVO.acceptQty());
        order.setRejectQty(createReqVO.rejectQty());
        order.setResult(createReqVO.result() != null ? createReqVO.result() : QmsInspectionResult.PENDING.getCode());
        order.setInspector(createReqVO.inspector());
        order.setInspectTime(createReqVO.inspectTime());
        order.setRemark(createReqVO.remark());
        inspectionOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    public void updateInspectionOrder(QmsInspectionOrderSaveReqVO updateReqVO) {
        validateOrderExists(updateReqVO.id());
        QmsInspectionOrderDO updateObj = new QmsInspectionOrderDO();
        updateObj.setId(updateReqVO.id());
        updateObj.setCode(updateReqVO.code());
        updateObj.setInspectionType(updateReqVO.inspectionType());
        updateObj.setBatchId(updateReqVO.batchId());
        updateObj.setWorkOrderCode(updateReqVO.workOrderCode());
        updateObj.setProductCode(updateReqVO.productCode());
        updateObj.setLotSize(updateReqVO.lotSize());
        updateObj.setSampleQty(updateReqVO.sampleQty());
        updateObj.setAqlLevel(updateReqVO.aqlLevel());
        updateObj.setAcceptQty(updateReqVO.acceptQty());
        updateObj.setRejectQty(updateReqVO.rejectQty());
        updateObj.setResult(updateReqVO.result());
        updateObj.setInspector(updateReqVO.inspector());
        updateObj.setInspectTime(updateReqVO.inspectTime());
        updateObj.setRemark(updateReqVO.remark());
        inspectionOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteInspectionOrder(Long id) {
        validateOrderExists(id);
        inspectionOrderMapper.deleteById(id);
    }

    @Override
    public QmsInspectionOrderDO getInspectionOrder(Long id) {
        QmsInspectionOrderDO order = inspectionOrderMapper.selectById(id);
        if (order == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
        return order;
    }

    @Override
    public PageResult<QmsInspectionOrderDO> getInspectionOrderPage(QmsInspectionOrderPageReqVO pageReqVO) {
        return inspectionOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public QmsInspectionOrderDO submitInspection(Long id) {
        QmsInspectionOrderDO order = getInspectionOrder(id);

        // 1. 汇总检验项不合格数，对照拒收数 Re 判定
        long failCount = inspectionItemMapper.selectListByOrderId(id).stream()
                .filter(item -> item.getItemResult() != null && item.getItemResult() == ITEM_RESULT_FAIL)
                .count();
        int rejectQty = order.getRejectQty() == null ? 0 : order.getRejectQty();
        boolean pass = failCount < rejectQty;
        int result = pass ? QmsInspectionResult.PASS.getCode() : QmsInspectionResult.FAIL.getCode();

        QmsInspectionOrderDO orderUpdate = new QmsInspectionOrderDO();
        orderUpdate.setId(id);
        orderUpdate.setResult(result);
        orderUpdate.setInspectTime(LocalDateTime.now());
        inspectionOrderMapper.updateById(orderUpdate);

        // 2. 联动批次状态：合格→已合格，不合格→不合格
        if (order.getBatchId() != null) {
            QmsBatchDO batch = batchMapper.selectById(order.getBatchId());
            if (batch != null) {
                QmsBatchDO batchUpdate = new QmsBatchDO();
                batchUpdate.setId(batch.getId());
                batchUpdate.setStatus(pass ? QmsBatchStatus.QUALIFIED.getCode() : QmsBatchStatus.UNQUALIFIED.getCode());
                batchMapper.updateById(batchUpdate);
            }
        }

        // 3. 不合格时，对未处置缺陷按处置规则引擎给出建议（MRB 评审前的系统输入）
        if (!pass) {
            for (QmsDefectDO defect : defectMapper.selectListByOrderId(id)) {
                if (defect.getDisposition() != null) {
                    continue;
                }
                QmsDefectDisposition suggest = DispositionRuleEngine.suggest(
                        severityOf(defect.getSeverity()), defect.getDefectCategory());
                QmsDefectDO defectUpdate = new QmsDefectDO();
                defectUpdate.setId(defect.getId());
                defectUpdate.setDisposition(suggest.getCode());
                defectMapper.updateById(defectUpdate);
            }
        }
        return getInspectionOrder(id);
    }

    private QmsDefectSeverity severityOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (QmsDefectSeverity severity : QmsDefectSeverity.values()) {
            if (severity.getCode().equals(code)) {
                return severity;
            }
        }
        return null;
    }

    private void validateOrderExists(Long id) {
        if (inspectionOrderMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
    }
}
