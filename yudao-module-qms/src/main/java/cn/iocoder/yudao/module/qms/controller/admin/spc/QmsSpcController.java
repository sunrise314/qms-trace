package cn.iocoder.yudao.module.qms.controller.admin.spc;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.qms.controller.admin.spc.vo.QmsSpcSampleSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsSpcSampleDO;
import cn.iocoder.yudao.module.qms.framework.qms.spc.SpcChartVO;
import cn.iocoder.yudao.module.qms.service.spc.QmsSpcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * QMS SPC 统计过程控制 Controller。
 *
 * <p>采样点写入即完成一次控制图计算；查询控制图时可选传入规格上下限，
 * 传了才计算 Cp/Cpk，否则能力指数返回 null。</p>
 */
@Tag(name = "管理后台 - QMS 统计过程控制")
@RestController
@RequestMapping("/api/admin/qms/spc")
@RequiredArgsConstructor
@Validated
public class QmsSpcController {

    private final QmsSpcService spcService;

    @PostMapping("/sample")
    @Operation(summary = "新增采样点（自动计算控制限与判异）")
    public CommonResult<Long> addSample(@Valid @RequestBody QmsSpcSampleSaveReqVO saveReqVO) {
        QmsSpcSampleDO sample = spcService.addSample(saveReqVO);
        return success(sample.getId());
    }

    @GetMapping("/chart")
    @Operation(summary = "查询控制图（最近 25 个子组，可选规格上下限计算 Cp/Cpk）")
    @Parameter(name = "productCode", description = "产品编码", required = true, example = "P-1001")
    @Parameter(name = "characteristicCode", description = "特性编码", required = true, example = "外径")
    @Parameter(name = "usl", description = "规格上限（可选）", example = "10.05")
    @Parameter(name = "lsl", description = "规格下限（可选）", example = "9.95")
    public CommonResult<SpcChartVO> chart(@RequestParam("productCode") String productCode,
                                          @RequestParam("characteristicCode") String characteristicCode,
                                          @RequestParam(value = "usl", required = false) BigDecimal usl,
                                          @RequestParam(value = "lsl", required = false) BigDecimal lsl) {
        return success(spcService.chart(productCode, characteristicCode, usl, lsl));
    }
}
