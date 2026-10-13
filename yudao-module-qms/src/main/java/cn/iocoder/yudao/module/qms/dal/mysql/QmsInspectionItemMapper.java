package cn.iocoder.yudao.module.qms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemPageReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionItemDO;

import java.util.List;

public interface QmsInspectionItemMapper extends BaseMapperX<QmsInspectionItemDO> {

    default PageResult<QmsInspectionItemDO> selectPage(QmsInspectionItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QmsInspectionItemDO>()
                .eqIfPresent(QmsInspectionItemDO::getInspectionOrderId, reqVO.getInspectionOrderId())
                .eqIfPresent(QmsInspectionItemDO::getItemCode, reqVO.getItemCode())
                .eqIfPresent(QmsInspectionItemDO::getItemResult, reqVO.getItemResult())
                .orderByAsc(QmsInspectionItemDO::getId));
    }

    /** 查询检验单下所有检验项（submitInspection 汇总不合格数用） */
    default List<QmsInspectionItemDO> selectListByOrderId(Long inspectionOrderId) {
        return selectList(new LambdaQueryWrapperX<QmsInspectionItemDO>()
                .eq(QmsInspectionItemDO::getInspectionOrderId, inspectionOrderId)
                .eq(QmsInspectionItemDO::getDeleted, false)
                .orderByAsc(QmsInspectionItemDO::getId));
    }
}
