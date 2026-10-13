package cn.iocoder.yudao.module.qms.service.sampling;

import cn.iocoder.yudao.module.qms.framework.qms.sampling.SamplingPlan;
import cn.iocoder.yudao.module.qms.framework.qms.sampling.SamplingPlanEngine;
import org.springframework.stereotype.Service;

/**
 * QMS 抽样方案 Service 实现。
 */
@Service
public class QmsSamplingServiceImpl implements QmsSamplingService {

    @Override
    public SamplingPlan plan(Integer lotSize, String aqlLevel) {
        int size = (lotSize == null || lotSize <= 0) ? 0 : lotSize;
        return SamplingPlanEngine.lookup(size, aqlLevel);
    }
}
