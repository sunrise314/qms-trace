package cn.iocoder.yudao.module.qms.service.spc;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.qms.controller.admin.spc.vo.QmsSpcSampleSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsSpcSampleDO;
import cn.iocoder.yudao.module.qms.dal.mysql.QmsSpcSampleMapper;
import cn.iocoder.yudao.module.qms.enums.QmsSpcChartType;
import cn.iocoder.yudao.module.qms.framework.qms.spc.CpkResult;
import cn.iocoder.yudao.module.qms.framework.qms.spc.SpcCalculator;
import cn.iocoder.yudao.module.qms.framework.qms.spc.SpcChartVO;
import cn.iocoder.yudao.module.qms.framework.qms.spc.SpcPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * QMS SPC 统计过程控制 Service 实现。
 *
 * addSample 的流程：加载该特性已有子组 → 连同新子组一起重算 X-Bar/R 控制限 → 回填控制限到新采样点，
 * 并对子组均值序列执行 Western Electric 判异规则。控制限随样本累积动态更新（工业上通常先固定
 * 一段基线再冻结控制限，此处采用滚动计算，商用可按需切换）。
 */
@Service
@RequiredArgsConstructor
public class QmsSpcServiceImpl implements QmsSpcService {

    private static final ErrorCode ERROR_CODE_VALUES_EMPTY = new ErrorCode(1_002_501, "测量值不能为空");
    private static final ErrorCode ERROR_CODE_VALUES_TOO_FEW = new ErrorCode(1_002_502, "子组测量值至少 2 个");
    private static final ErrorCode ERROR_CODE_CHART_EMPTY = new ErrorCode(1_002_503, "该特性暂无采样数据: {}-{}");

    /** 控制图最多展示的子组数 */
    private static final int MAX_SUBGROUPS = 25;

    private final QmsSpcSampleMapper spcSampleMapper;

    @Override
    public QmsSpcSampleDO addSample(QmsSpcSampleSaveReqVO saveReqVO) {
        List<Double> values = saveReqVO.values();
        if (values == null || values.isEmpty()) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_VALUES_EMPTY);
        }
        if (values.size() < 2) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_VALUES_TOO_FEW);
        }
        String productCode = saveReqVO.productCode();
        String characteristicCode = saveReqVO.characteristicCode();

        // 已有子组 + 新子组 → 重算控制限
        List<QmsSpcSampleDO> existing = spcSampleMapper.selectListByCharacteristic(productCode, characteristicCode);
        List<double[]> subgroups = new ArrayList<>();
        for (QmsSpcSampleDO sample : existing) {
            subgroups.add(toArray(sample.getValuesJson()));
        }
        double[] newSubgroup = toArray(values);
        subgroups.add(newSubgroup);

        SpcCalculator.XbarRChart chart = SpcCalculator.xbarRChart(subgroups);
        List<Double> means = new ArrayList<>();
        for (double[] subgroup : subgroups) {
            means.add(SpcCalculator.mean(subgroup));
        }
        // 判异用的 sigma：以子组均值序列的样本标准差估计（商用可改用 R̄/d2 估计组内 sigma）
        double sigma = SpcCalculator.stdDev(toArray(means));
        List<String> violations = SpcCalculator.westernElectricRules(means, chart.xbarCl(), sigma);

        QmsSpcSampleDO sample = new QmsSpcSampleDO();
        sample.setProductCode(productCode);
        sample.setCharacteristicCode(characteristicCode);
        sample.setChartType(saveReqVO.chartType() != null
                ? saveReqVO.chartType() : QmsSpcChartType.XBAR_R.getCode());
        sample.setSubgroupIndex(existing.size() + 1);
        sample.setSubgroupSize(values.size());
        sample.setValuesJson(values);
        sample.setMeanValue(round(SpcCalculator.mean(newSubgroup)));
        sample.setRangeValue(round(SpcCalculator.range(newSubgroup)));
        sample.setClValue(round(chart.xbarCl()));
        sample.setUclValue(round(chart.xbarUcl()));
        sample.setLclValue(round(chart.xbarLcl()));
        sample.setRuleViolations(violations);
        sample.setSampleTime(saveReqVO.sampleTime() != null ? saveReqVO.sampleTime() : LocalDateTime.now());
        spcSampleMapper.insert(sample);
        return sample;
    }

    @Override
    public SpcChartVO chart(String productCode, String characteristicCode) {
        return chart(productCode, characteristicCode, null, null);
    }

    @Override
    public SpcChartVO chart(String productCode, String characteristicCode, BigDecimal usl, BigDecimal lsl) {
        List<QmsSpcSampleDO> samples = spcSampleMapper.selectListByCharacteristic(productCode, characteristicCode);
        if (samples.isEmpty()) {
            throw ServiceExceptionUtil.exception(ERROR_CODE_CHART_EMPTY, productCode, characteristicCode);
        }
        // 取最近 MAX_SUBGROUPS 个子组
        List<QmsSpcSampleDO> recent = samples.size() <= MAX_SUBGROUPS
                ? samples
                : samples.subList(samples.size() - MAX_SUBGROUPS, samples.size());

        List<double[]> subgroups = new ArrayList<>();
        List<SpcPointVO> points = new ArrayList<>();
        List<Double> means = new ArrayList<>();
        Integer chartType = QmsSpcChartType.XBAR_R.getCode();
        for (QmsSpcSampleDO sample : recent) {
            double[] values = toArray(sample.getValuesJson());
            subgroups.add(values);
            means.add(sample.getMeanValue() != null
                    ? sample.getMeanValue().doubleValue() : SpcCalculator.mean(values));
            points.add(new SpcPointVO(sample.getSubgroupIndex(), sample.getMeanValue(),
                    sample.getRangeValue(), sample.getSampleTime()));
            if (sample.getChartType() != null) {
                chartType = sample.getChartType();
            }
        }

        SpcCalculator.XbarRChart chart = SpcCalculator.xbarRChart(subgroups);
        double sigma = SpcCalculator.stdDev(toArray(means));
        List<String> violations = SpcCalculator.westernElectricRules(means, chart.xbarCl(), sigma);

        BigDecimal cp = null;
        BigDecimal cpk = null;
        if (usl != null && lsl != null) {
            CpkResult cpkResult = SpcCalculator.cpk(chart.xbarCl(), sigma, usl.doubleValue(), lsl.doubleValue());
            cp = round(cpkResult.cp());
            cpk = round(cpkResult.cpk());
        }

        return new SpcChartVO(productCode, characteristicCode, chartTypeOf(chartType),
                points, round(chart.xbarCl()), round(chart.xbarUcl()), round(chart.xbarLcl()), cp, cpk, violations);
    }

    private QmsSpcChartType chartTypeOf(Integer code) {
        if (code != null) {
            for (QmsSpcChartType type : QmsSpcChartType.values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
        }
        return QmsSpcChartType.XBAR_R;
    }

    private double[] toArray(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new double[0];
        }
        double[] array = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i) == null ? 0d : values.get(i);
        }
        return array;
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
