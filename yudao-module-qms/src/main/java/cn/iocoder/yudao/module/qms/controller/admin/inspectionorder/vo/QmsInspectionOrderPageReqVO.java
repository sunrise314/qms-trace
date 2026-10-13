package cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验单分页 Request VO。继承 yudao 的 PageParam，直接对接 BaseMapperX.selectPage。
 */
@Schema(description = "管理后台 - QMS 检验单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QmsInspectionOrderPageReqVO extends PageParam {

    @Schema(description = "检验单编码", example = "QMS-20261013-001")
    private String code;

    @Schema(description = "检验类型: 1=IQC来料 2=IPQC过程 3=FQC成品 4=OQC出货", example = "1")
    private Integer inspectionType;

    @Schema(description = "判定结果: 0=待检 1=合格 2=不合格 3=让步接收", example = "1")
    private Integer result;

    @Schema(description = "关联批次 ID", example = "1")
    private Long batchId;

    @Schema(description = "产品编码", example = "P-1001")
    private String productCode;
}
