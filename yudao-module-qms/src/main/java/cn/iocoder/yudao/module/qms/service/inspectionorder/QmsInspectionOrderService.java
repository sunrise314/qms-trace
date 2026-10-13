package cn.iocoder.yudao.module.qms.service.inspectionorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionOrderDO;

/**
 * QMS 检验单 Service 接口。
 */
public interface QmsInspectionOrderService {

    /** 创建检验单 */
    Long createInspectionOrder(QmsInspectionOrderSaveReqVO createReqVO);

    /** 更新检验单 */
    void updateInspectionOrder(QmsInspectionOrderSaveReqVO updateReqVO);

    /** 删除检验单 */
    void deleteInspectionOrder(Long id);

    /** 根据 ID 查询 */
    QmsInspectionOrderDO getInspectionOrder(Long id);

    /** 分页查询 */
    PageResult<QmsInspectionOrderDO> getInspectionOrderPage(QmsInspectionOrderPageReqVO pageReqVO);

    /**
     * 提交检验：汇总该检验单下所有检验项的不合格数，与拒收数 Re 比较得出判定结果，
     * 联动更新批次状态；判定不合格时对未处置缺陷按处置规则给出建议处置。
     *
     * @param id 检验单 ID
     * @return 判定后的检验单
     */
    QmsInspectionOrderDO submitInspection(Long id);
}
