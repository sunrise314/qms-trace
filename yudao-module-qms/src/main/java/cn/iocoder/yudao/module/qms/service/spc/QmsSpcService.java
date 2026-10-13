package cn.iocoder.yudao.module.qms.service.spc;

import cn.iocoder.yudao.module.qms.controller.admin.spc.vo.QmsSpcSampleSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsSpcSampleDO;
import cn.iocoder.yudao.module.qms.framework.qms.spc.SpcChartVO;

import java.math.BigDecimal;

/**
 * QMS SPC 统计过程控制 Service 接口。
 */
public interface QmsSpcService {

    /**
     * 新增采样点：写入子组原始测量值，自动计算均值/极差与当前控制限，并执行判异规则。
     */
    QmsSpcSampleDO addSample(QmsSpcSampleSaveReqVO saveReqVO);

    /**
     * 查询控制图（最近 25 个子组）。未提供规格上下限时，Cp/Cpk 返回 null。
     */
    SpcChartVO chart(String productCode, String characteristicCode);

    /**
     * 查询控制图（最近 25 个子组），并依据规格上下限计算 Cp/Cpk。
     */
    SpcChartVO chart(String productCode, String characteristicCode, BigDecimal usl, BigDecimal lsl);
}
