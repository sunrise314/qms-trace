package cn.iocoder.yudao.module.qms.controller.admin.sampling;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.qms.framework.qms.sampling.SamplingPlan;
import cn.iocoder.yudao.module.qms.service.sampling.QmsSamplingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * QMS AQL 抽样方案 Controller。
 *
 * <p>依据 GB/T 2828.1 一般检验水平 II，按批量与 AQL 水平查样本量字码与接收/拒收数。</p>
 */
@Tag(name = "管理后台 - QMS 抽样方案")
@RestController
@RequestMapping("/api/admin/qms/sampling")
@RequiredArgsConstructor
@Validated
public class QmsSamplingController {

    private final QmsSamplingService samplingService;

    @GetMapping("/plan")
    @Operation(summary = "查询 AQL 抽样方案")
    @Parameter(name = "lotSize", description = "检验批量", required = true, example = "1000")
    @Parameter(name = "aqlLevel", description = "AQL 水平（0.65/1.0/1.5/2.5/4.0）", required = true, example = "2.5")
    public CommonResult<SamplingPlan> plan(@RequestParam("lotSize") Integer lotSize,
                                           @RequestParam("aqlLevel") String aqlLevel) {
        return success(samplingService.plan(lotSize, aqlLevel));
    }
}
