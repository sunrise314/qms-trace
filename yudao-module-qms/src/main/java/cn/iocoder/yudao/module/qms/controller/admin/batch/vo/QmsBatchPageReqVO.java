package cn.iocoder.yudao.module.qms.controller.admin.batch.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 批次分页 Request VO。
 */
@Schema(description = "管理后台 - QMS 批次分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QmsBatchPageReqVO extends PageParam {

    @Schema(description = "批次号", example = "B-20261013-001")
    private String batchNo;

    @Schema(description = "产品编码", example = "P-1001")
    private String productCode;

    @Schema(description = "状态: 1=在制 2=待检 3=合格 4=不合格 5=已出货 6=冻结", example = "3")
    private Integer status;

    @Schema(description = "谱系父批次 ID", example = "1")
    private Long parentBatchId;

    @Schema(description = "生产机台", example = "CNC-01")
    private String machineCode;
}
