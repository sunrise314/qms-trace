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
 * QMS 追溯链路。批次谱系图的"边"：记录某个上游批次以何种方式流向下游批次。
 *
 * link_type 区分投料/拼批/分拆/装配/返工——不同工艺关系的追溯语义不同，
 * 例如"分拆"意味着一批原料产出多个子批，"装配"意味着多个料批合成一个成品批。
 * GenealogyGraph 正向/反向 BFS 均以此为邻接边。
 */
@Data
@TableName("qms_trace_link")
public class QmsTraceLinkDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    /** 上游/输入批次 ID */
    private Long fromBatchId;

    /** 下游/产出批次 ID */
    private Long toBatchId;

    /** 1=投料 2=拼批 3=分拆 4=装配 5=返工 */
    private Integer linkType;

    /** 流转数量（可能小于批次总量，如部分投料） */
    private BigDecimal quantity;

    private LocalDateTime linkTime;

    private String remark;

    private String creator;
    private LocalDateTime createTime;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean deleted;
}
