package cn.iocoder.yudao.module.qms.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * QMS 检验项。检验单下的单个检测特性，记录标准值/上下限/实测值/单件判定。
 *
 * submitInspection 时统计 item_result=2（不合格）的条数作为本次检验的不合格数。
 */
@Data
@TableName("qms_inspection_item")
public class QmsInspectionItemDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long inspectionOrderId;

    /** 检验项编码，如 DIM-01 */
    private String itemCode;

    /** 检验项名称，如 "外径" */
    private String itemName;

    /** 标准值（文本描述形式，如 "10±0.05"） */
    private String standardValue;

    /** 规格上限 */
    private BigDecimal upperLimit;

    /** 规格下限 */
    private BigDecimal lowerLimit;

    /** 实测值 */
    private BigDecimal actualValue;

    private String unit;

    /** 1=合格 2=不合格 */
    private Integer itemResult;

    private String remark;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
