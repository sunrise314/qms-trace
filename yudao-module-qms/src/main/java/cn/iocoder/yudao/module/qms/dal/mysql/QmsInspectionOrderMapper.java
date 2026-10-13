package cn.iocoder.yudao.module.qms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderPageReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionOrderDO;

public interface QmsInspectionOrderMapper extends BaseMapperX<QmsInspectionOrderDO> {

    default PageResult<QmsInspectionOrderDO> selectPage(QmsInspectionOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QmsInspectionOrderDO>()
                .eqIfPresent(QmsInspectionOrderDO::getCode, reqVO.getCode())
                .eqIfPresent(QmsInspectionOrderDO::getInspectionType, reqVO.getInspectionType())
                .eqIfPresent(QmsInspectionOrderDO::getResult, reqVO.getResult())
                .eqIfPresent(QmsInspectionOrderDO::getBatchId, reqVO.getBatchId())
                .eqIfPresent(QmsInspectionOrderDO::getProductCode, reqVO.getProductCode())
                .orderByDesc(QmsInspectionOrderDO::getId));
    }

    /** 检验单编码唯一性校验 */
    default QmsInspectionOrderDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<QmsInspectionOrderDO>()
                .eq(QmsInspectionOrderDO::getCode, code)
                .eq(QmsInspectionOrderDO::getDeleted, false));
    }
}
