package cn.iocoder.yudao.module.qms.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsSpcSampleDO;

import java.util.List;

public interface QmsSpcSampleMapper extends BaseMapperX<QmsSpcSampleDO> {

    /** 查询某产品某特性的全部采样点，按子组序号升序（chart 取最近 25 个子组） */
    default List<QmsSpcSampleDO> selectListByCharacteristic(String productCode, String characteristicCode) {
        return selectList(new LambdaQueryWrapperX<QmsSpcSampleDO>()
                .eq(QmsSpcSampleDO::getProductCode, productCode)
                .eq(QmsSpcSampleDO::getCharacteristicCode, characteristicCode)
                .eq(QmsSpcSampleDO::getDeleted, false)
                .orderByAsc(QmsSpcSampleDO::getSubgroupIndex));
    }

    /** 查询某产品某特性已录入的子组数（用于生成下一个子组序号） */
    default Long selectCountByCharacteristic(String productCode, String characteristicCode) {
        return selectCount(new LambdaQueryWrapperX<QmsSpcSampleDO>()
                .eq(QmsSpcSampleDO::getProductCode, productCode)
                .eq(QmsSpcSampleDO::getCharacteristicCode, characteristicCode)
                .eq(QmsSpcSampleDO::getDeleted, false));
    }
}
