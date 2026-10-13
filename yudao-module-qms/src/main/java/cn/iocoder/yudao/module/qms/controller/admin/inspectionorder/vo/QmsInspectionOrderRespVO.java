package cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - QMS 检验单 Response VO")
public record QmsInspectionOrderRespVO(

        @Schema(description = "检验单 ID", example = "1")
        Long id,

        @Schema(description = "检验单编码", example = "QMS-20261013-001")
        String code,

        @Schema(description = "检验类型: 1=IQC来料 2=IPQC过程 3=FQC成品 4=OQC出货", example = "1")
        Integer inspectionType,

        @Schema(description = "关联批次 ID", example = "1")
        Long batchId,

        @Schema(description = "关联工单编码", example = "WO-20261013-001")
        String workOrderCode,

        @Schema(description = "产品编码", example = "P-1001")
        String productCode,

        @Schema(description = "检验批量", example = "500")
        Integer lotSize,

        @Schema(description = "抽样数 n", example = "50")
        Integer sampleQty,

        @Schema(description = "AQL 水平", example = "2.5")
        String aqlLevel,

        @Schema(description = "接收数 Ac", example = "3")
        Integer acceptQty,

        @Schema(description = "拒收数 Re", example = "4")
        Integer rejectQty,

        @Schema(description = "判定结果: 0=待检 1=合格 2=不合格 3=让步接收", example = "1")
        Integer result,

        @Schema(description = "检验员")
        String inspector,

        @Schema(description = "检验时间")
        LocalDateTime inspectTime,

        @Schema(description = "备注")
        String remark,

        @Schema(description = "创建时间")
        LocalDateTime createTime,

        @Schema(description = "更新时间")
        LocalDateTime updateTime
) {
}
