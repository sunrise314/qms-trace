package cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - QMS 检验项 Response VO")
public record QmsInspectionItemRespVO(

        @Schema(description = "检验项 ID", example = "1")
        Long id,

        @Schema(description = "所属检验单 ID", example = "1")
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
        String remark,

        @Schema(description = "创建时间")
        LocalDateTime createTime,

        @Schema(description = "更新时间")
        LocalDateTime updateTime
) {
}
