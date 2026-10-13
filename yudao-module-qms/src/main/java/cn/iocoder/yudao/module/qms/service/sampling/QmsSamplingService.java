package cn.iocoder.yudao.module.qms.service.sampling;

import cn.iocoder.yudao.module.qms.framework.qms.sampling.SamplingPlan;

/**
 * QMS 抽样方案 Service 接口。对 AQL 抽样方案引擎的一层业务包装。
 */
public interface QmsSamplingService {

    /**
     * 查询抽样方案。
     *
     * @param lotSize  检验批量
     * @param aqlLevel AQL 水平，如 "2.5"
     * @return 抽样方案（n / Ac / Re / 样本量字码）
     */
    SamplingPlan plan(Integer lotSize, String aqlLevel);
}
