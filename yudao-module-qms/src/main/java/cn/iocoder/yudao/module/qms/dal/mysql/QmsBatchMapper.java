package cn.iocoder.yudao.module.qms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchPageReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsBatchDO;

import java.util.Collection;
import java.util.List;

public interface QmsBatchMapper extends BaseMapperX<QmsBatchDO> {

    default PageResult<QmsBatchDO> selectPage(QmsBatchPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QmsBatchDO>()
                .eqIfPresent(QmsBatchDO::getBatchNo, reqVO.getBatchNo())
                .eqIfPresent(QmsBatchDO::getProductCode, reqVO.getProductCode())
                .eqIfPresent(QmsBatchDO::getStatus, reqVO.getStatus())
                .eqIfPresent(QmsBatchDO::getParentBatchId, reqVO.getParentBatchId())
                .eqIfPresent(QmsBatchDO::getMachineCode, reqVO.getMachineCode())
                .orderByDesc(QmsBatchDO::getId));
    }

    /** 按批次号查询（租户内唯一） */
    default QmsBatchDO selectByBatchNo(String batchNo) {
        return selectOne(new LambdaQueryWrapperX<QmsBatchDO>()
                .eq(QmsBatchDO::getBatchNo, batchNo)
                .eq(QmsBatchDO::getDeleted, false));
    }

    /** 按 ID 集合批量查询（追溯时把 BFS 得到的批次 ID 一次捞回来） */
    default List<QmsBatchDO> selectListByIds(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapperX<QmsBatchDO>()
                .in(QmsBatchDO::getId, ids)
                .eq(QmsBatchDO::getDeleted, false));
    }
}
