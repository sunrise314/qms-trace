package cn.iocoder.yudao.module.qms.service.defect;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsDefectDO;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsDefectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * QMS 缺陷记录 Service 实现。
 */
@Service
@RequiredArgsConstructor
public class QmsDefectServiceImpl implements QmsDefectService {

    private static final ErrorCode ERROR_CODE_NOT_EXISTS = new ErrorCode(1_002_201, "缺陷记录不存在: id={}");
    private static final ErrorCode ERROR_CODE_DISPOSITION_INVALID = new ErrorCode(1_002_202, "处置方式非法: {}");

    private final QmsDefectMapper defectMapper;

    @Override
    public Long createDefect(QmsDefectSaveReqVO createReqVO) {
        QmsDefectDO defect = new QmsDefectDO();
        defect.setInspectionOrderId(createReqVO.inspectionOrderId());
        defect.setDefectCode(createReqVO.defectCode());
        defect.setDefectName(createReqVO.defectName());
        defect.setDefectCategory(createReqVO.defectCategory());
        defect.setSeverity(createReqVO.severity());
        defect.setQuantity(createReqVO.quantity());
        defect.setDisposition(createReqVO.disposition());
        defect.setResponsibleProcess(createReqVO.responsibleProcess());
        defect.setImageUrl(createReqVO.imageUrl());
        defect.setRemark(createReqVO.remark());
        defectMapper.insert(defect);
        return defect.getId();
    }

    @Override
    public void updateDefect(QmsDefectSaveReqVO updateReqVO) {
        validateDefectExists(updateReqVO.id());
        QmsDefectDO updateObj = new QmsDefectDO();
        updateObj.setId(updateReqVO.id());
        updateObj.setInspectionOrderId(updateReqVO.inspectionOrderId());
        updateObj.setDefectCode(updateReqVO.defectCode());
        updateObj.setDefectName(updateReqVO.defectName());
        updateObj.setDefectCategory(updateReqVO.defectCategory());
        updateObj.setSeverity(updateReqVO.severity());
        updateObj.setQuantity(updateReqVO.quantity());
        updateObj.setDisposition(updateReqVO.disposition());
        updateObj.setResponsibleProcess(updateReqVO.responsibleProcess());
        updateObj.setImageUrl(updateReqVO.imageUrl());
        updateObj.setRemark(updateReqVO.remark());
        defectMapper.updateById(updateObj);
    }

    @Override
    public void deleteDefect(Long id) {
        validateDefectExists(id);
        defectMapper.deleteById(id);
    }

    @Override
    public QmsDefectDO getDefect(Long id) {
        QmsDefectDO defect = defectMapper.selectById(id);
        if (defect == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
        return defect;
    }

    @Override
    public PageResult<QmsDefectDO> getDefectPage(QmsDefectPageReqVO pageReqVO) {
        return defectMapper.selectPage(pageReqVO);
    }

    @Override
    public void dispose(Long defectId, Integer disposition) {
        validateDefectExists(defectId);
        if (!isValidDisposition(disposition)) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_DISPOSITION_INVALID, disposition);
        }
        QmsDefectDO updateObj = new QmsDefectDO();
        updateObj.setId(defectId);
        updateObj.setDisposition(disposition);
        defectMapper.updateById(updateObj);
    }

    private boolean isValidDisposition(Integer disposition) {
        return disposition != null && disposition >= 1 && disposition <= 5;
    }

    private void validateDefectExists(Long id) {
        if (defectMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
    }
}
