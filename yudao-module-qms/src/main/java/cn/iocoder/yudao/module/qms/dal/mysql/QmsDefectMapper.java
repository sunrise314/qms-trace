package cn.iocoder.yudao.module.qms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectPageReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsDefectDO;

import java.util.List;

public interface QmsDefectMapper extends BaseMapperX<QmsDefectDO> {

    default PageResult<QmsDefectDO> selectPage(QmsDefectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QmsDefectDO>()
                .eqIfPresent(QmsDefectDO::getInspectionOrderId, reqVO.getInspectionOrderId())
                .eqIfPresent(QmsDefectDO::getDefectCategory, reqVO.getDefectCategory())
                .eqIfPresent(QmsDefectDO::getSeverity, reqVO.getSeverity())
                .eqIfPresent(QmsDefectDO::getDisposition, reqVO.getDisposition())
                .orderByDesc(QmsDefectDO::getId));
    }

    /** 查询检验单下所有缺陷记录（submitInspection 回填处置建议用） */
    default List<QmsDefectDO> selectListByOrderId(Long inspectionOrderId) {
        return selectList(new LambdaQueryWrapperX<QmsDefectDO>()
                .eq(QmsDefectDO::getInspectionOrderId, inspectionOrderId)
                .eq(QmsDefectDO::getDeleted, false)
                .orderByAsc(QmsDefectDO::getId));
    }
}
