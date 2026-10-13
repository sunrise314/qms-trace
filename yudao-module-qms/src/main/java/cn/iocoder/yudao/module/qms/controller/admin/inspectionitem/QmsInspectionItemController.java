package cn.iocoder.yudao.module.qms.controller.admin.inspectionitem;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemRespVO;
import cn.iocoder.yudao.module.qms.controller.admin.inspectionitem.vo.QmsInspectionItemSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsInspectionItemDO;
import cn.iocoder.yudao.module.qms.service.inspectionitem.QmsInspectionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * QMS 检验项 Controller。
 */
@Tag(name = "管理后台 - QMS 检验项")
@RestController
@RequestMapping("/api/admin/qms/inspection-item")
@RequiredArgsConstructor
@Validated
public class QmsInspectionItemController {

    private final QmsInspectionItemService inspectionItemService;

    @PostMapping("/create")
    @Operation(summary = "创建检验项")
    public CommonResult<Long> createInspectionItem(@Valid @RequestBody QmsInspectionItemSaveReqVO createReqVO) {
        return success(inspectionItemService.createInspectionItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新检验项")
    public CommonResult<Boolean> updateInspectionItem(@Valid @RequestBody QmsInspectionItemSaveReqVO updateReqVO) {
        inspectionItemService.updateInspectionItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除检验项")
    @Parameter(name = "id", description = "检验项 ID", required = true)
    public CommonResult<Boolean> deleteInspectionItem(@RequestParam("id") Long id) {
        inspectionItemService.deleteInspectionItem(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取检验项详情")
    @Parameter(name = "id", description = "检验项 ID", required = true, example = "1")
    public CommonResult<QmsInspectionItemRespVO> getInspectionItem(@RequestParam("id") Long id) {
        return success(toRespVO(inspectionItemService.getInspectionItem(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询检验项")
    public CommonResult<PageResult<QmsInspectionItemRespVO>> getInspectionItemPage(@Valid QmsInspectionItemPageReqVO pageReqVO) {
        PageResult<QmsInspectionItemDO> page = inspectionItemService.getInspectionItemPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream()
                .map(QmsInspectionItemController::toRespVO).toList(), page.getTotal()));
    }

    @GetMapping("/list-by-order")
    @Operation(summary = "根据检验单 ID 查询所有检验项")
    @Parameter(name = "inspectionOrderId", description = "检验单 ID", required = true, example = "1")
    public CommonResult<List<QmsInspectionItemRespVO>> listByOrderId(@RequestParam("inspectionOrderId") Long inspectionOrderId) {
        return success(inspectionItemService.listByOrderId(inspectionOrderId).stream()
                .map(QmsInspectionItemController::toRespVO).toList());
    }

    // ---------- VO 转换 ----------

    private static QmsInspectionItemRespVO toRespVO(QmsInspectionItemDO i) {
        return new QmsInspectionItemRespVO(i.getId(), i.getInspectionOrderId(), i.getItemCode(), i.getItemName(),
                i.getStandardValue(), i.getUpperLimit(), i.getLowerLimit(), i.getActualValue(), i.getUnit(),
                i.getItemResult(), i.getRemark(), i.getCreateTime(), i.getUpdateTime());
    }
}
