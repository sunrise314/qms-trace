package cn.iocoder.yudao.module.qms.framework.qms.rule;

import cn.iocoder.yudao.module.qms.enums.QmsDefectDisposition;
import cn.iocoder.yudao.module.qms.enums.QmsDefectSeverity;
import lombok.experimental.UtilityClass;

/**
 * 不合格品处置规则引擎。
 *
 * 依据缺陷严重度 + 缺陷类别，给出**建议**处置方式。规则如下：
 * - 致命（CRITICAL）：来料/供应商来源 → 退货；否则 → 报废；
 * - 严重（MAJOR）：尺寸/外观类可修复 → 返修；其余 → 返工；
 * - 轻微（MINOR）：让步接收。
 *
 * MRB（Material Review Board，物料评审）：本引擎只做"系统建议"，不做最终裁决。
 * 致命缺陷的报废/退货、以及让步接收都必须经 MRB 会议评审后由授权人签核，
 * 系统建议仅作为 MRB 决策的输入。商用实现需把签核结果与建议做差异审计。
 */
@UtilityClass
public class DispositionRuleEngine {

    /** 来源类关键词：命中则判为供应商/来料问题 */
    private static final String[] SUPPLIER_ORIGIN_KEYWORDS = {"来料", "供应商", "原料"};

    /** 可修复类关键词：命中则优先返修而非返工 */
    private static final String[] REPAIRABLE_KEYWORDS = {"尺寸", "外观"};

    /**
     * 给出建议处置方式。
     *
     * @param severity       缺陷严重度
     * @param defectCategory 缺陷类别（如 "外观" / "尺寸" / "性能"）
     * @return 建议处置方式；severity 为 null 时按严重处理（保守）
     */
    public static QmsDefectDisposition suggest(QmsDefectSeverity severity, String defectCategory) {
        if (severity == null) {
            return QmsDefectDisposition.REWORK;
        }
        String category = defectCategory == null ? "" : defectCategory;
        if (severity == QmsDefectSeverity.CRITICAL) {
            return containsAny(category, SUPPLIER_ORIGIN_KEYWORDS)
                    ? QmsDefectDisposition.RETURN
                    : QmsDefectDisposition.SCRAP;
        }
        if (severity == QmsDefectSeverity.MAJOR) {
            return containsAny(category, REPAIRABLE_KEYWORDS)
                    ? QmsDefectDisposition.REPAIR
                    : QmsDefectDisposition.REWORK;
        }
        return QmsDefectDisposition.CONCESSION;
    }

    private static boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
