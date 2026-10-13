package cn.iocoder.yudao.module.qms.framework.qms.trace;

import lombok.experimental.UtilityClass;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 批次谱系图引擎——QMS 追溯的两大核心查询。
 *
 * 正向追溯（forward）：从原材料/上游批次出发，BFS 找出它的料流向了哪些下游批次。
 *   —— 场景：某批原料被判定不合格，需要召回所有用到它的成品（"我的料去哪儿了"）。
 * 反向追溯（backward）：从成品/不合格批次出发，BFS 找出它用了哪些上游批次。
 *   —— 场景：成品出现质量事故，需要定位是哪批料/哪道工序引入的（"问题根源在哪里"）。
 *
 * 两者都是对同构邻接表的广度优先遍历，仅遍历方向相反，故共用一个内部实现。
 *
 * 防环：用 visited 集合保证每个批次只入队一次；即便脏数据形成环状谱系，
 * BFS 也不会无限循环。同一批次的邻居按 linkTime 升序展开，保证"按深度、时间有序"。
 */
@UtilityClass
public class GenealogyGraph {

    /**
     * 一次遍历的结果。
     *
     * @param order 按 BFS 层序排列的批次 ID（不含根批次）
     * @param depth 最大层号（根批次为 0）
     */
    public record Traversal(List<Long> order, int depth) {
    }

    /** 正向追溯：返回根批次的所有下游批次 ID（按层序、层内按流转时间升序）。 */
    public static List<Long> forward(List<TraceLinkVO> links, Long rootBatchId) {
        return traverse(links, rootBatchId, true).order();
    }

    /** 反向追溯：返回根批次的所有上游批次 ID（按层序、层内按流转时间升序）。 */
    public static List<Long> backward(List<TraceLinkVO> links, Long rootBatchId) {
        return traverse(links, rootBatchId, false).order();
    }

    /**
     * 通用 BFS 遍历。forward=true 沿 from→to 方向展开，false 则沿 to→from 方向展开。
     *
     * @param links       全量追溯链路（调用方一次性加载；大批量场景建议改递归 CTE）
     * @param rootBatchId 根批次 ID
     * @param forward     遍历方向
     * @return 有序批次 ID + 最大深度
     */
    public static Traversal traverse(List<TraceLinkVO> links, Long rootBatchId, boolean forward) {
        if (rootBatchId == null || links == null || links.isEmpty()) {
            return new Traversal(List.of(), 0);
        }
        // 构建邻接表：按遍历方向，把每条边的"起点"作为 key
        Map<Long, List<TraceLinkVO>> adjacency = new HashMap<>();
        for (TraceLinkVO link : links) {
            Long key = forward ? link.fromBatchId() : link.toBatchId();
            if (key == null) {
                continue;
            }
            adjacency.computeIfAbsent(key, k -> new ArrayList<>()).add(link);
        }
        // 邻居按流转时间升序，保证同一层内"按时间"有序（null 排最后）
        adjacency.values().forEach(list -> list.sort(
                Comparator.comparing(TraceLinkVO::linkTime, Comparator.nullsLast(Comparator.naturalOrder()))));

        List<Long> order = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Map<Long, Integer> depthOf = new HashMap<>();
        Deque<Long> queue = new ArrayDeque<>();

        visited.add(rootBatchId);
        depthOf.put(rootBatchId, 0);
        queue.add(rootBatchId);
        int maxDepth = 0;

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            int currentDepth = depthOf.getOrDefault(current, 0);
            for (TraceLinkVO link : adjacency.getOrDefault(current, List.of())) {
                Long next = forward ? link.toBatchId() : link.fromBatchId();
                if (next == null || visited.contains(next)) {
                    continue; // 已访问（防环）
                }
                visited.add(next);
                depthOf.put(next, currentDepth + 1);
                maxDepth = Math.max(maxDepth, currentDepth + 1);
                order.add(next);
                queue.add(next);
            }
        }
        return new Traversal(order, maxDepth);
    }
}
