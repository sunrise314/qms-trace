package cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验项分页 Request VO。
 */
@Schema(description = "管理后台 - QMS 检验项分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QmsInspectionItemPageReqVO extends PageParam {

    @Schema(description = "所属检验单 ID", example = "1")
    private Long inspectionOrderId;

    @Schema(description = "检验项编码", example = "DIM-01")
    private String itemCode;

    @Schema(description = "单项判定: 1=合格 2=不合格", example = "2")
    private Integer itemResult;
}
