package cn.iocoder.yudao.module.qms.controller.admin.batch;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchRespVO;
import cn.iocoder.yudao.module.qms.controller.admin.batch.vo.QmsBatchSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsBatchDO;
import cn.iocoder.yudao.module.qms.service.batch.QmsBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * QMS 批次 Controller。
 */
@Tag(name = "管理后台 - QMS 批次")
@RestController
@RequestMapping("/api/admin/qms/batch")
@RequiredArgsConstructor
@Validated
public class QmsBatchController {

    private final QmsBatchService batchService;

    @PostMapping("/create")
    @Operation(summary = "创建批次")
    public CommonResult<Long> createBatch(@Valid @RequestBody QmsBatchSaveReqVO createReqVO) {
        return success(batchService.createBatch(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新批次")
    public CommonResult<Boolean> updateBatch(@Valid @RequestBody QmsBatchSaveReqVO updateReqVO) {
        batchService.updateBatch(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除批次")
    @Parameter(name = "id", description = "批次 ID", required = true)
    public CommonResult<Boolean> deleteBatch(@RequestParam("id") Long id) {
        batchService.deleteBatch(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取批次详情")
    @Parameter(name = "id", description = "批次 ID", required = true, example = "1")
    public CommonResult<QmsBatchRespVO> getBatch(@RequestParam("id") Long id) {
        return success(toRespVO(batchService.getBatch(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询批次")
    public CommonResult<PageResult<QmsBatchRespVO>> getBatchPage(@Valid QmsBatchPageReqVO pageReqVO) {
        PageResult<QmsBatchDO> page = batchService.getBatchPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream()
                .map(QmsBatchController::toRespVO).toList(), page.getTotal()));
    }

    @PutMapping("/release")
    @Operation(summary = "批次放行（status=QUALIFIED）")
    @Parameter(name = "id", description = "批次 ID", required = true, example = "1")
    public CommonResult<Boolean> release(@RequestParam("id") Long id) {
        batchService.release(id);
        return success(true);
    }

    @PutMapping("/freeze")
    @Operation(summary = "冻结批次（status=FROZEN）")
    @Parameter(name = "id", description = "批次 ID", required = true, example = "1")
    public CommonResult<Boolean> freeze(@RequestParam("id") Long id) {
        batchService.freeze(id);
        return success(true);
    }

    // ---------- VO 转换 ----------

    private static QmsBatchRespVO toRespVO(QmsBatchDO b) {
        return new QmsBatchRespVO(b.getId(), b.getBatchNo(), b.getProductCode(), b.getProductName(),
                b.getQuantity(), b.getProductionOrderCode(), b.getMachineCode(), b.getShift(),
                b.getParentBatchId(), b.getAttributes(), b.getStatus(), b.getProduceTime(),
                b.getExpiryTime(), b.getRemark(), b.getCreateTime(), b.getUpdateTime());
    }
}
