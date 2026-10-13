package cn.iocoder.yudao.module.qms.service.defect;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsDefectDO;

/**
 * QMS 缺陷记录 Service 接口。
 */
public interface QmsDefectService {

    /** 创建缺陷记录 */
    Long createDefect(QmsDefectSaveReqVO createReqVO);

    /** 更新缺陷记录 */
    void updateDefect(QmsDefectSaveReqVO updateReqVO);

    /** 删除缺陷记录 */
    void deleteDefect(Long id);

    /** 根据 ID 查询 */
    QmsDefectDO getDefect(Long id);

    /** 分页查询 */
    PageResult<QmsDefectDO> getDefectPage(QmsDefectPageReqVO pageReqVO);

    /** 处置缺陷（回填 disposition，取值同 QmsDefectDisposition） */
    void dispose(Long defectId, Integer disposition);
}
