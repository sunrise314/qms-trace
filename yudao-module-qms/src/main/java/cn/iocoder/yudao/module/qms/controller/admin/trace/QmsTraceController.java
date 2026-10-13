package cn.iocoder.yudao.module.qms.controller.admin.trace;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.qms.framework.qms.trace.TraceResult;
import cn.iocoder.yudao.module.qms.service.trace.QmsTraceService;
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
 * QMS 批次谱系追溯 Controller。
 *
 * <p>追溯是只读查询，不需要 CRUD：正向追溯用于召回（料流向哪些下游），
 * 反向追溯用于溯源（这批货用了哪些上游），报告则合并出整张谱系子图。</p>
 */
@Tag(name = "管理后台 - QMS 批次追溯")
@RestController
@RequestMapping("/api/admin/qms/trace")
@RequiredArgsConstructor
@Validated
public class QmsTraceController {

    private final QmsTraceService traceService;

    @GetMapping("/forward")
    @Operation(summary = "正向追溯（召回）：上游批次的料流向了哪些下游批次")
    @Parameter(name = "batchId", description = "起点批次 ID", required = true, example = "1")
    public CommonResult<TraceResult> forward(@RequestParam("batchId") Long batchId) {
        return success(traceService.forwardTrace(batchId));
    }

    @GetMapping("/backward")
    @Operation(summary = "反向追溯（溯源）：下游批次用了哪些上游批次")
    @Parameter(name = "batchId", description = "起点批次 ID", required = true, example = "1")
    public CommonResult<TraceResult> backward(@RequestParam("batchId") Long batchId) {
        return success(traceService.backwardTrace(batchId));
    }

    @GetMapping("/report")
    @Operation(summary = "完整追溯报告：合并正反向谱系子图")
    @Parameter(name = "batchId", description = "起点批次 ID", required = true, example = "1")
    public CommonResult<TraceResult> report(@RequestParam("batchId") Long batchId) {
        return success(traceService.traceReport(batchId));
    }
}
