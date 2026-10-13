package cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "管理后台 - QMS 检验项新增/修改 Request VO")
public record QmsInspectionItemSaveReqVO(

        @Schema(description = "检验项 ID", example = "1")
        Long id,

        @Schema(description = "所属检验单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "检验单 ID 不能为空")
        Long inspectionOrderId,

        @Schema(description = "检验项编码", example = "DIM-01")
        String itemCode,

        @Schema(description = "检验项名称", example = "外径")
        String itemName,

        @Schema(description = "标准值", example = "10±0.05")
        String standardValue,

        @Schema(description = "规格上限", example = "10.0500")
        BigDecimal upperLimit,

        @Schema(description = "规格下限", example = "9.9500")
        BigDecimal lowerLimit,

        @Schema(description = "实测值", example = "10.0100")
        BigDecimal actualValue,

        @Schema(description = "单位", example = "mm")
        String unit,

        @Schema(description = "单项判定: 1=合格 2=不合格", example = "1")
        Integer itemResult,

        @Schema(description = "备注")
        String remark
) {
}
