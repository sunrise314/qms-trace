package cn.iocoder.yudao.module.qms.service.inspectionitem;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionItemDO;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsInspectionItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * QMS 检验项 Service 实现。
 */
@Service
@RequiredArgsConstructor
public class QmsInspectionItemServiceImpl implements QmsInspectionItemService {

    private static final ErrorCode ERROR_CODE_NOT_EXISTS = new ErrorCode(1_002_101, "检验项不存在: id={}");

    private final QmsInspectionItemMapper inspectionItemMapper;

    @Override
    public Long createInspectionItem(QmsInspectionItemSaveReqVO createReqVO) {
        QmsInspectionItemDO item = new QmsInspectionItemDO();
        item.setInspectionOrderId(createReqVO.inspectionOrderId());
        item.setItemCode(createReqVO.itemCode());
        item.setItemName(createReqVO.itemName());
        item.setStandardValue(createReqVO.standardValue());
        item.setUpperLimit(createReqVO.upperLimit());
        item.setLowerLimit(createReqVO.lowerLimit());
        item.setActualValue(createReqVO.actualValue());
        item.setUnit(createReqVO.unit());
        item.setItemResult(createReqVO.itemResult());
        item.setRemark(createReqVO.remark());
        inspectionItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateInspectionItem(QmsInspectionItemSaveReqVO updateReqVO) {
        validateItemExists(updateReqVO.id());
        QmsInspectionItemDO updateObj = new QmsInspectionItemDO();
        updateObj.setId(updateReqVO.id());
        updateObj.setInspectionOrderId(updateReqVO.inspectionOrderId());
        updateObj.setItemCode(updateReqVO.itemCode());
        updateObj.setItemName(updateReqVO.itemName());
        updateObj.setStandardValue(updateReqVO.standardValue());
        updateObj.setUpperLimit(updateReqVO.upperLimit());
        updateObj.setLowerLimit(updateReqVO.lowerLimit());
        updateObj.setActualValue(updateReqVO.actualValue());
        updateObj.setUnit(updateReqVO.unit());
        updateObj.setItemResult(updateReqVO.itemResult());
        updateObj.setRemark(updateReqVO.remark());
        inspectionItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteInspectionItem(Long id) {
        validateItemExists(id);
        inspectionItemMapper.deleteById(id);
    }

    @Override
    public QmsInspectionItemDO getInspectionItem(Long id) {
        QmsInspectionItemDO item = inspectionItemMapper.selectById(id);
        if (item == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
        return item;
    }

    @Override
    public PageResult<QmsInspectionItemDO> getInspectionItemPage(QmsInspectionItemPageReqVO pageReqVO) {
        return inspectionItemMapper.selectPage(pageReqVO);
    }

    @Override
    public List<QmsInspectionItemDO> listByOrderId(Long inspectionOrderId) {
        return inspectionItemMapper.selectListByOrderId(inspectionOrderId);
    }

    private void validateItemExists(Long id) {
        if (inspectionItemMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_NOT_EXISTS, id);
        }
    }
}
