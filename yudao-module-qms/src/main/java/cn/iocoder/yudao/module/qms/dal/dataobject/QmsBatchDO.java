package cn.iocoder.yudao.module.qms.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * QMS 批次/序列号。追溯的主体单元，一次投料/生产产出的物料集合。
 *
 * parentBatchId 是谱系父批次（自引用，可空）——主要用于快速的单级父追溯；
 * 完整的多级正反向谱系由 qms_trace_link 邻接表 + GenealogyGraph BFS 得到。
 * attributes 用 JSONB 存批次扩展属性（供应商/炉号/设备参数等），避免为每个行业建字段。
 */
@Data
@TableName(value = "qms_batch", autoResultMap = true)
public class QmsBatchDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** 批次号，租户内唯一，如 B-20261013-001 */
    private String batchNo;

    private String productCode;

    private String productName;

    /** 批次数量 */
    private Integer quantity;

    /** 关联生产工单编码 */
    private String productionOrderCode;

    /** 生产机台 */
    private String machineCode;

    /** 班次，如 "白班" / "夜班" */
    private String shift;

    /** 谱系父批次（自引用，可空） */
    private Long parentBatchId;

    /** 批次扩展属性（JSONB），如 {"supplier":"X钢厂","heatNo":"H2026101"} */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> attributes;

    /** 1=在制 2=待检 3=合格 4=不合格 5=已出货 6=冻结 */
    private Integer status;

    /** 生产时间 */
    private LocalDateTime produceTime;

    /** 有效期（食品/药品/化工等场景） */
    private LocalDateTime expiryTime;

    private String remark;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
