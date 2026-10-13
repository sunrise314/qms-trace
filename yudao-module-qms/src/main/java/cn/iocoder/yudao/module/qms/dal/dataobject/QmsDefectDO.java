package cn.iocoder.yudao.module.qms.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * QMS 缺陷记录。一次检验中发现的具体缺陷，含严重度、数量、责任工序与处置方式。
 *
 * 处置方式 disposition 为 NULL 表示尚未处置；submitInspection 判定不合格时，
 * 会调用 DispositionRuleEngine 按严重度给出建议处置并回填到未处置的缺陷上。
 */
@Data
@TableName("qms_defect")
public class QmsDefectDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long inspectionOrderId;

    /** 缺陷编码，如 DEF-001 */
    private String defectCode;

    /** 缺陷名称，如 "毛刺" */
    private String defectName;

    /** 缺陷类别，如 "外观" / "尺寸" / "性能" */
    private String defectCategory;

    /** 1=致命 2=严重 3=轻微 */
    private Integer severity;

    /** 缺陷数量 */
    private Integer quantity;

    /** 1=返工 2=返修 3=让步接收 4=报废 5=退货；NULL=未处置 */
    private Integer disposition;

    /** 责任工序 */
    private String responsibleProcess;

    /** 缺陷图片地址 */
    private String imageUrl;

    private String remark;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
