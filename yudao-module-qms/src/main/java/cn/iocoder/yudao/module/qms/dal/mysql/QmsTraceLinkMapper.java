package cn.iocoder.yudao.module.qms.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsTraceLinkDO;

import java.util.List;

public interface QmsTraceLinkMapper extends BaseMapperX<QmsTraceLinkDO> {

    /**
     * 查询租户下所有追溯链路，交给 GenealogyGraph 做正反向 BFS。
     *
     * 商用扩展点：批次量大时此处全量加载会偏重，可改为 PostgreSQL 递归 CTE
     * （WITH RECURSIVE）按根批次增量展开，避免一次性把整张边表读进内存。
     */
    default List<QmsTraceLinkDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<QmsTraceLinkDO>()
                .eq(QmsTraceLinkDO::getDeleted, false)
                .orderByAsc(QmsTraceLinkDO::getLinkTime));
    }

    /** 查询某批次的所有下游边（from=batchId） */
    default List<QmsTraceLinkDO> selectListByFromBatchId(Long fromBatchId) {
        return selectList(new LambdaQueryWrapperX<QmsTraceLinkDO>()
                .eq(QmsTraceLinkDO::getFromBatchId, fromBatchId)
                .eq(QmsTraceLinkDO::getDeleted, false));
    }

    /** 查询某批次的所有上游边（to=batchId） */
    default List<QmsTraceLinkDO> selectListByToBatchId(Long toBatchId) {
        return selectList(new LambdaQueryWrapperX<QmsTraceLinkDO>()
                .eq(QmsTraceLinkDO::getToBatchId, toBatchId)
                .eq(QmsTraceLinkDO::getDeleted, false));
    }
}
