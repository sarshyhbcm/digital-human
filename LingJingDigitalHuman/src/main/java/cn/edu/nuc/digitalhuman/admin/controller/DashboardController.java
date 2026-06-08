package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.auth.mapper.SysUserMapper;
import cn.edu.nuc.digitalhuman.chat.mapper.ConversationMapper;
import cn.edu.nuc.digitalhuman.chat.mapper.MessageMapper;
import cn.edu.nuc.digitalhuman.checkin.mapper.CheckInMapper;
import cn.edu.nuc.digitalhuman.common.result.R;
import cn.edu.nuc.digitalhuman.interaction.mapper.InteractionLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserMapper sysUserMapper;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final CheckInMapper checkInMapper;
    private final InteractionLogMapper interactionLogMapper;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("userCount", sysUserMapper.selectCount(null));
        stats.put("conversationCount", conversationMapper.selectCount(null));
        stats.put("messageCount", messageMapper.selectCount(null));
        stats.put("checkInCount", checkInMapper.selectCount(null));
        return R.success(stats);
    }

    @GetMapping("/trends")
    public R<List<Map<String, Object>>> getTrends() {
        // 近7天每日对话数
        QueryWrapper<cn.edu.nuc.digitalhuman.chat.entity.Conversation> convQw = new QueryWrapper<>();
        convQw.select("DATE(created_at) as date", "COUNT(*) as count")
                .apply("created_at >= DATE_ADD(CURDATE(), INTERVAL -6 DAY)")
                .groupBy("DATE(created_at)")
                .orderByAsc("DATE(created_at)");
        List<Map<String, Object>> convData = conversationMapper.selectMaps(convQw);
        Map<String, Long> convMap = new HashMap<>();
        for (Map<String, Object> row : convData) {
            convMap.put(String.valueOf(row.get("date")), ((Number) row.get("count")).longValue());
        }

        // 近7天每日打卡数
        QueryWrapper<cn.edu.nuc.digitalhuman.checkin.entity.CheckIn> checkQw = new QueryWrapper<>();
        checkQw.select("DATE(created_at) as date", "COUNT(*) as count")
                .apply("created_at >= DATE_ADD(CURDATE(), INTERVAL -6 DAY)")
                .groupBy("DATE(created_at)")
                .orderByAsc("DATE(created_at)");
        List<Map<String, Object>> checkData = checkInMapper.selectMaps(checkQw);
        Map<String, Long> checkMap = new HashMap<>();
        for (Map<String, Object> row : checkData) {
            checkMap.put(String.valueOf(row.get("date")), ((Number) row.get("count")).longValue());
        }

        // 填充完整7天
        List<Map<String, Object>> trends = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date);
            day.put("conversations", convMap.getOrDefault(date, 0L));
            day.put("checkIns", checkMap.getOrDefault(date, 0L));
            trends.add(day);
        }
        return R.success(trends);
    }

    @GetMapping("/popular-attractions")
    public R<List<Map<String, Object>>> getPopularAttractions() {
        String sql = "SELECT a.id, a.name, a.area, COALESCE(c.cnt, 0) AS checkInCount " +
                     "FROM attraction a " +
                     "LEFT JOIN (SELECT attraction_id, COUNT(*) AS cnt FROM check_in GROUP BY attraction_id) c " +
                     "ON a.id = c.attraction_id " +
                     "ORDER BY checkInCount DESC, a.sort_order ASC";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        return R.success(result);
    }

    @GetMapping("/sentiment")
    public R<List<Map<String, Object>>> getSentiment() {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql = "SELECT COALESCE(sentiment, 'unknown') AS name, COUNT(*) AS value FROM interaction_log GROUP BY sentiment";
        List<Map<String, Object>> dbRows;
        try {
            dbRows = jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            dbRows = Collections.emptyList();
        }

        Map<String, Long> sentimentMap = new HashMap<>();
        for (Map<String, Object> row : dbRows) {
            sentimentMap.put(String.valueOf(row.get("name")), ((Number) row.get("value")).longValue());
        }

        result.add(Map.of("name", "正面", "value", sentimentMap.getOrDefault("positive", 0L)));
        result.add(Map.of("name", "中性", "value", sentimentMap.getOrDefault("neutral", 0L)));
        result.add(Map.of("name", "负面", "value", sentimentMap.getOrDefault("negative", 0L)));
        return R.success(result);
    }

    @GetMapping("/top-questions")
    public R<List<Map<String, Object>>> getTopQuestions() {
        // 无意义的问候语/语气词，精确匹配时过滤
        Set<String> noiseSet = Set.of(
                "你好", "您好", "谢谢", "感谢", "在吗",
                "好的", "嗯", "哦", "哈哈", "嘿嘿",
                "在不在", "你好呀", "您好呀", "谢谢啊"
        );

        QueryWrapper<cn.edu.nuc.digitalhuman.chat.entity.Message> qw = new QueryWrapper<>();
        qw.select("TRIM(content) AS question", "COUNT(*) as count")
                .eq("role", "user")
                .isNotNull("content")
                .ne("content", "")
                .apply("CHAR_LENGTH(TRIM(content)) >= 2")
                .groupBy("TRIM(content)")
                .orderByDesc("COUNT(*)")
                .last("LIMIT 30");
        List<Map<String, Object>> result = messageMapper.selectMaps(qw);

        // 后过滤噪声 + 热度分级
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : result) {
            String q = (String) row.get("question");
            if (q == null) continue;
            q = q.trim();
            // 去标点后判断是否噪声
            String clean = q.replaceAll("[，。！？、；：…～·\"'“”【】《》（）\\s]", "");
            if (clean.isEmpty() || noiseSet.contains(clean) || clean.length() <= 1) {
                continue;
            }
            row.put("question", q);
            row.put("count", ((Number) row.get("count")).longValue());
            filtered.add(row);
            if (filtered.size() >= 10) break;
        }

        // 热度等级：bao/re/huo/wen（前端 CSS 类名映射）
        if (!filtered.isEmpty()) {
            long maxCount = ((Number) filtered.get(0).get("count")).longValue();
            for (Map<String, Object> row : filtered) {
                long count = ((Number) row.get("count")).longValue();
                double ratio = (double) count / maxCount;
                String heat;
                if (ratio > 0.7)       heat = "bao";
                else if (ratio > 0.4)  heat = "re";
                else if (ratio > 0.2)  heat = "huo";
                else                   heat = "wen";
                row.put("heat", heat);
            }
        }

        return R.success(filtered);
    }

    @GetMapping("/interaction-types")
    public R<List<Map<String, Object>>> getInteractionTypes() {
        QueryWrapper<cn.edu.nuc.digitalhuman.interaction.entity.InteractionLog> qw = new QueryWrapper<>();
        qw.select("interaction_type AS name", "COUNT(*) AS value")
                .isNotNull("interaction_type")
                .ne("interaction_type", "")
                .groupBy("interaction_type")
                .orderByDesc("COUNT(*)");
        List<Map<String, Object>> result = interactionLogMapper.selectMaps(qw);
        return R.success(result);
    }
}
