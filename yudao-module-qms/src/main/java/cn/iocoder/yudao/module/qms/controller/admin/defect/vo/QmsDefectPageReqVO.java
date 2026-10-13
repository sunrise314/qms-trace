package cn.iocoder.yudao.module.qms.controller.admin.defect.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 缺陷记录分页 Request VO。
 */
@Schema(description = "管理后台 - QMS 缺陷记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QmsDefectPageReqVO extends PageParam {

    @Schema(description = "所属检验单 ID", example = "1")
    private Long inspectionOrderId;

    @Schema(description = "缺陷类别", example = "外观")
    private String defectCategory;

    @Schema(description = "严重度: 1=致命 2=严重 3=轻微", example = "2")
    private Integer severity;

    @Schema(description = "处置方式: 1=返工 2=返修 3=让步接收 4=报废 5=退货", example = "1")
    private Integer disposition;
}
