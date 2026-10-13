package cn.iocoder.yudao.module.qms.controller.admin.inspectionorder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderRespVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionorder.vo.QmsInspectionOrderSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionOrderDO;
import cn.iocoder.yudao.module.qms.service.inspectionorder.QmsInspectionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * QMS 检验单 Controller。
 */
@Tag(name = "管理后台 - QMS 检验单")
@RestController
@RequestMapping("/api/admin/qms/inspection-order")
@RequiredArgsConstructor
@Validated
public class QmsInspectionOrderController {

    private final QmsInspectionOrderService inspectionOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建检验单")
    public CommonResult<Long> createInspectionOrder(@Valid @RequestBody QmsInspectionOrderSaveReqVO createReqVO) {
        return success(inspectionOrderService.createInspectionOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检验单")
    public CommonResult<Boolean> updateInspectionOrder(@Valid @RequestBody QmsInspectionOrderSaveReqVO updateReqVO) {
        inspectionOrderService.updateInspectionOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检验单")
    @Parameter(name = "id", description = "检验单 ID", required = true)
    public CommonResult<Boolean> deleteInspectionOrder(@RequestParam("id") Long id) {
        inspectionOrderService.deleteInspectionOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取检验单详情")
    @Parameter(name = "id", description = "检验单 ID", required = true, example = "1")
    public CommonResult<QmsInspectionOrderRespVO> getInspectionOrder(@RequestParam("id") Long id) {
        return success(toRespVO(inspectionOrderService.getInspectionOrder(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询检验单")
    public CommonResult<PageResult<QmsInspectionOrderRespVO>> getInspectionOrderPage(@Valid QmsInspectionOrderPageReqVO pageReqVO) {
        PageResult<QmsInspectionOrderDO> page = inspectionOrderService.getInspectionOrderPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream()
                .map(QmsInspectionOrderController::toRespVO).toList(), page.getTotal()));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交检验（汇总不合格数 → 判定 → 联动批次与缺陷处置）")
    @Parameter(name = "id", description = "检验单 ID", required = true, example = "1")
    public CommonResult<QmsInspectionOrderRespVO> submitInspection(@RequestParam("id") Long id) {
        return success(toRespVO(inspectionOrderService.submitInspection(id)));
    }

    // ---------- VO 转换 ----------

    private static QmsInspectionOrderRespVO toRespVO(QmsInspectionOrderDO o) {
        return new QmsInspectionOrderRespVO(o.getId(), o.getCode(), o.getInspectionType(), o.getBatchId(),
                o.getWorkOrderCode(), o.getProductCode(), o.getLotSize(), o.getSampleQty(), o.getAqlLevel(),
                o.getAcceptQty(), o.getRejectQty(), o.getResult(), o.getInspector(), o.getInspectTime(),
                o.getRemark(), o.getCreateTime(), o.getUpdateTime());
    }
}
