package cn.iocoder.yudao.module.qms.controller.admin.spc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - QMS SPC 采样点新增 Request VO")
public record QmsSpcSampleSaveReqVO(

        @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "P-1001")
        @NotBlank(message = "产品编码不能为空")
        String productCode,

        @Schema(description = "特性编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "外径")
        @NotBlank(message = "特性编码不能为空")
        String characteristicCode,

        @Schema(description = "控制图类型: 1=X-Bar/R 2=X-MR 3=P 4=NP 5=C 6=U", example = "1")
        Integer chartType,

        @Schema(description = "子组内测量值数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "[10.01, 9.99, 10.02]")
        @NotEmpty(message = "测量值不能为空")
        List<Double> values,

        @Schema(description = "采样时间")
        LocalDateTime sampleTime
) {
}
