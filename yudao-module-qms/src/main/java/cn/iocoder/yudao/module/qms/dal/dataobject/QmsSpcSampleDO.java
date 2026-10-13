package cn.iocoder.yudao.module.qms.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * QMS SPC 采样点。一个控制图子组（subgroup）的样本数据。
 *
 * values_json 存子组内的原始测量值数组（JSONB），mean_value/range_value 由 SpcCalculator 计算回填；
 * cl/ucl/lcl 是控制图的中心线与上下控制限，由 xbarRChart 依据子组容量查 A2/D4/D3 系数得到；
 * rule_violations 存 Western Electric 判异规则的命中描述。
 */
@Data
@TableName(value = "qms_spc_sample", autoResultMap = true)
public class QmsSpcSampleDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String productCode;

    /** 特性编码，如 "外径" / "厚度" */
    private String characteristicCode;

    /** 1=X-Bar/R 2=X-MR 3=P 4=NP 5=C 6=U */
    private Integer chartType;

    /** 子组序号（同一特性内递增） */
    private Integer subgroupIndex;

    /** 子组容量 */
    private Integer subgroupSize;

    /** 子组内测量值数组（JSONB） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Double> valuesJson;

    /** 子组均值 X-bar */
    private BigDecimal meanValue;

    /** 子组极差 R */
    private BigDecimal rangeValue;

    /** 中心线 CL */
    private BigDecimal clValue;

    /** 上控制限 UCL */
    private BigDecimal uclValue;

    /** 下控制限 LCL */
    private BigDecimal lclValue;

    /** 判异规则命中描述（JSONB） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> ruleViolations;

    private LocalDateTime sampleTime;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
