package cn.iocoder.yudao.module.qms.service.inspectionitem;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionItemDO;

import java.util.List;

/**
 * QMS 检验项 Service 接口。
 */
public interface QmsInspectionItemService {

    /** 创建检验项 */
    Long createInspectionItem(QmsInspectionItemSaveReqVO createReqVO);

    /** 更新检验项 */
    void updateInspectionItem(QmsInspectionItemSaveReqVO updateReqVO);

    /** 删除检验项 */
    void deleteInspectionItem(Long id);

    /** 根据 ID 查询 */
    QmsInspectionItemDO getInspectionItem(Long id);

    /** 分页查询 */
    PageResult<QmsInspectionItemDO> getInspectionItemPage(QmsInspectionItemPageReqVO pageReqVO);

    /** 查询检验单下所有检验项 */
    List<QmsInspectionItemDO> listByOrderId(Long inspectionOrderId);
}
