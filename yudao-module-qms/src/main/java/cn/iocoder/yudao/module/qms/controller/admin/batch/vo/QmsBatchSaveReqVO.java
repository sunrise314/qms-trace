package cn.iocoder.yudao.module.qms.controller.admin.batch.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - QMS 批次新增/修改 Request VO")
public record QmsBatchSaveReqVO(

        @Schema(description = "批次 ID", example = "1")
        Long id,

        @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "B-20261013-001")
        @NotBlank(message = "批次号不能为空")
        String batchNo,

        @Schema(description = "产品编码", example = "P-1001")
        String productCode,

        @Schema(description = "产品名称", example = "法兰盘")
        String productName,

        @Schema(description = "批次数量", example = "500")
        Integer quantity,

        @Schema(description = "关联生产工单编码", example = "WO-20261013-001")
        String productionOrderCode,

        @Schema(description = "生产机台", example = "CNC-01")
        String machineCode,

        @Schema(description = "班次", example = "白班")
        String shift,

        @Schema(description = "谱系父批次 ID", example = "1")
        Long parentBatchId,

        @Schema(description = "批次扩展属性")
        Map<String, Object> attributes,

        @Schema(description = "状态: 1=在制 2=待检 3=合格 4=不合格 5=已出货 6=冻结", example = "1")
        Integer status,

        @Schema(description = "生产时间")
        LocalDateTime produceTime,

        @Schema(description = "有效期")
        LocalDateTime expiryTime,

        @Schema(description = "备注")
        String remark
) {
}
