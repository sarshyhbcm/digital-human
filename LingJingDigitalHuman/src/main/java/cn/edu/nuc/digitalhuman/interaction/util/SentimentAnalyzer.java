package cn.edu.nuc.digitalhuman.interaction.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 简单中文情感分析工具，基于关键词匹配。
 */
@UtilityClass
public class SentimentAnalyzer {

    private static final List<String> POSITIVE_KEYWORDS = List.of(
            "谢谢", "感谢", "好", "棒", "赞", "喜欢", "不错", "满意",
            "厉害", "美", "漂亮", "开心", "好看", "精彩", "震撼",
            "壮观", "雄伟", "精致", "有趣", "丰富", "值得", "实用",
            "方便", "清晰", "详细", "热情", "周到", "nice", "great",
            "good", "beautiful", "amazing", "awesome", "excellent"
    );

    private static final List<String> NEGATIVE_KEYWORDS = List.of(
            "差", "不好", "垃圾", "失望", "无聊", "没用", "烦",
            "慢", "错", "不对", "错误", "卡", "死机", "崩溃",
            "糟糕", "难用", "差劲", "太差", "不行", "假的",
            "骗人", "坑", "bad", "terrible", "disappointed",
            "slow", "wrong", "useless", "boring"
    );

    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{P}\\s]+");

    /**
     * 分析文本情感倾向。
     *
     * @param text 用户消息文本
     * @return "positive" / "negative" / "neutral"
     */
    public String analyze(String text) {
        if (text == null || text.isBlank()) {
            return "neutral";
        }

        String cleaned = PUNCTUATION.matcher(text.toLowerCase()).replaceAll(" ");

        int positiveScore = countKeywords(cleaned, POSITIVE_KEYWORDS);
        int negativeScore = countKeywords(cleaned, NEGATIVE_KEYWORDS);

        if (positiveScore > negativeScore) {
            return "positive";
        } else if (negativeScore > positiveScore) {
            return "negative";
        }
        return "neutral";
    }

    private static int countKeywords(String text, List<String> keywords) {
        int score = 0;
        for (String kw : keywords) {
            if (text.contains(kw)) {
                score++;
            }
        }
        return score;
    }
}
