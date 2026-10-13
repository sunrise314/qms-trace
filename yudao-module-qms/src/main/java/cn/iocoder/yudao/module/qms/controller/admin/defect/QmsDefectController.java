package cn.iocoder.yudao.module.qms.controller.admin.defect;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectPageReqVO;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectRespVO;
import cn.iocoder.yudao.module.qms.controller.admin.defect.vo.QmsDefectSaveReqVO;
import cn.iocoder.yudao.module.qms.dal.dataobject.QmsDefectDO;
import cn.iocoder.yudao.module.qms.service.defect.QmsDefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * QMS 缺陷记录 Controller。
 */
@Tag(name = "管理后台 - QMS 缺陷记录")
@RestController
@RequestMapping("/api/admin/qms/defect")
@RequiredArgsConstructor
@Validated
public class QmsDefectController {

    private final QmsDefectService defectService;

    @PostMapping("/create")
    @Operation(summary = "创建缺陷记录")
    public CommonResult<Long> createDefect(@Valid @RequestBody QmsDefectSaveReqVO createReqVO) {
        return success(defectService.createDefect(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新缺陷记录")
    public CommonResult<Boolean> updateDefect(@Valid @RequestBody QmsDefectSaveReqVO updateReqVO) {
        defectService.updateDefect(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除缺陷记录")
    @Parameter(name = "id", description = "缺陷记录 ID", required = true)
    public CommonResult<Boolean> deleteDefect(@RequestParam("id") Long id) {
        defectService.deleteDefect(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取缺陷记录详情")
    @Parameter(name = "id", description = "缺陷记录 ID", required = true, example = "1")
    public CommonResult<QmsDefectRespVO> getDefect(@RequestParam("id") Long id) {
        return success(toRespVO(defectService.getDefect(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询缺陷记录")
    public CommonResult<PageResult<QmsDefectRespVO>> getDefectPage(@Valid QmsDefectPageReqVO pageReqVO) {
        PageResult<QmsDefectDO> page = defectService.getDefectPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream()
                .map(QmsDefectController::toRespVO).toList(), page.getTotal()));
    }

    @PutMapping("/dispose")
    @Operation(summary = "处置缺陷")
    @Parameter(name = "defectId", description = "缺陷记录 ID", required = true, example = "1")
    @Parameter(name = "disposition", description = "处置方式: 1=返工 2=返修 3=让步接收 4=报废 5=退货", required = true, example = "1")
    public CommonResult<Boolean> dispose(@RequestParam("defectId") Long defectId,
                                         @RequestParam("disposition") Integer disposition) {
        defectService.dispose(defectId, disposition);
        return success(true);
    }

    // ---------- VO 转换 ----------

    private static QmsDefectRespVO toRespVO(QmsDefectDO d) {
        return new QmsDefectRespVO(d.getId(), d.getInspectionOrderId(), d.getDefectCode(), d.getDefectName(),
                d.getDefectCategory(), d.getSeverity(), d.getQuantity(), d.getDisposition(),
                d.getResponsibleProcess(), d.getImageUrl(), d.getRemark(), d.getCreateTime(), d.getUpdateTime());
    }
}
