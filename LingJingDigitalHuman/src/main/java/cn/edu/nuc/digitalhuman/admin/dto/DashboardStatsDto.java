package cn.edu.nuc.digitalhuman.admin.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardStatsDto {
    private long userCount;
    private long conversationCount;
    private long messageCount;
    private long checkInCount;
    private List<Map<String, Object>> dailyTrends;
    private List<Map<String, Object>> popularAttractions;
    private List<Map<String, Object>> sentimentDistribution;
    private List<Map<String, Object>> topQuestions;
}
