package cn.iocoder.yudao.module.qms.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * QMS 检验单。一次检验活动的主记录，承载抽样方案（n/Ac/Re）与最终判定结果。
 *
 * 与检验项（qms_inspection_item）是一对多：一张检验单下挂多个检验项，
 * submitInspection 汇总所有检验项的不合格数，与 rejectQty（Re）比较得出判定结果。
 */
@Data
@TableName("qms_inspection_order")
public class QmsInspectionOrderDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** 检验单编码，租户内唯一，如 QMS-20261013-001 */
    private String code;

    /** 1=IQC来料 2=IPQC过程 3=FQC成品 4=OQC出货 */
    private Integer inspectionType;

    /** 关联批次 ID（追溯主体） */
    private Long batchId;

    /** 关联工单编码 */
    private String workOrderCode;

    private String productCode;

    /** 检验批量（提交检验的送检总数） */
    private Integer lotSize;

    /** 抽样数 n（由 AQL 抽样方案引擎给出） */
    private Integer sampleQty;

    /** AQL 水平，如 "2.5" */
    private String aqlLevel;

    /** 接收数 Ac */
    private Integer acceptQty;

    /** 拒收数 Re（不合格数 >= Re 判不合格） */
    private Integer rejectQty;

    /** 0=待检 1=合格 2=不合格 3=让步接收 */
    private Integer result;

    private String inspector;

    private LocalDateTime inspectTime;

    private String remark;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
