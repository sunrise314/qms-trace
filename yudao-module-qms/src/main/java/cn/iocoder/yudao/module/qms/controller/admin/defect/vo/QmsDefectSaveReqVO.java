package cn.iocoder.yudao.module.qms.controller.admin.defect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - QMS 缺陷记录新增/修改 Request VO")
public record QmsDefectSaveReqVO(

        @Schema(description = "缺陷 ID", example = "1")
        Long id,

        @Schema(description = "所属检验单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "检验单 ID 不能为空")
        Long inspectionOrderId,

        @Schema(description = "缺陷编码", example = "DEF-001")
        String defectCode,

        @Schema(description = "缺陷名称", example = "毛刺")
        String defectName,

        @Schema(description = "缺陷类别", example = "外观")
        String defectCategory,

        @Schema(description = "严重度: 1=致命 2=严重 3=轻微", example = "2")
        Integer severity,

        @Schema(description = "缺陷数量", example = "5")
        Integer quantity,

        @Schema(description = "处置方式: 1=返工 2=返修 3=让步接收 4=报废 5=退货", example = "1")
        Integer disposition,

        @Schema(description = "责任工序", example = "OP-20")
        String responsibleProcess,

        @Schema(description = "缺陷图片地址")
        String imageUrl,

        @Schema(description = "备注")
        String remark
) {
}
