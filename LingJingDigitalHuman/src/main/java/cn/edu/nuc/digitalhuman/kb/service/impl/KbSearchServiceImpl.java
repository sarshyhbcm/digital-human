package cn.edu.nuc.digitalhuman.kb.service.impl;

import cn.edu.nuc.digitalhuman.kb.entity.KbChunk;
import cn.edu.nuc.digitalhuman.kb.mapper.KbChunkMapper;
import cn.edu.nuc.digitalhuman.kb.service.KbSearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KbSearchServiceImpl implements KbSearchService {

    private final KbChunkMapper chunkMapper;

    // 中文停用词
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一",
            "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
            "没有", "看", "好", "自己", "这", "他", "她", "它", "们", "那", "些",
            "为", "什么", "怎么", "如何", "吗", "吧", "呢", "啊", "哦", "嗯",
            "请", "问", "能", "可以", "让", "把", "被", "从", "对", "与",
            "这个", "那个", "哪个", "这些", "那些", "因为", "所以", "但是", "如果");

    @Override
    public List<KbChunk> search(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();

        // 提取关键词
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) return List.of();

        log.debug("KB搜索关键词: {}", keywords);

        // 获取所有 chunks（如果 KB 小可以全量查；大时可加 LIMIT）
        List<KbChunk> allChunks = chunkMapper.selectList(
                new LambdaQueryWrapper<KbChunk>()
                        .orderByAsc(KbChunk::getDocumentId)
                        .orderByAsc(KbChunk::getSeq));

        if (allChunks.isEmpty()) return List.of();

        // 评分：匹配关键词数量 + 位置权重
        List<ScoredChunk> scored = new ArrayList<>();
        for (KbChunk chunk : allChunks) {
            String content = chunk.getContent();
            int matchCount = 0;
            for (String kw : keywords) {
                if (content.contains(kw)) {
                    matchCount++;
                }
            }
            if (matchCount > 0) {
                // 匹配数占比 × 长文本权重（避免过短片段排在前面）
                double score = (double) matchCount / keywords.size()
                        * Math.min(1.0, content.length() / 50.0);
                scored.add(new ScoredChunk(chunk, score));
            }
        }

        // 按评分降序，取 topN
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        return scored.stream()
                .limit(limit)
                .map(s -> s.chunk)
                .collect(Collectors.toList());
    }

    private List<String> extractKeywords(String query) {
        // 按非中文字符分割，保留中文词
        List<String> words = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        Pattern chinese = Pattern.compile("[\\u4e00-\\u9fa5]");

        for (char c : query.toCharArray()) {
            if (chinese.matcher(String.valueOf(c)).matches()) {
                buf.append(c);
            } else {
                if (!buf.isEmpty()) {
                    words.add(buf.toString());
                    buf = new StringBuilder();
                }
            }
        }
        if (!buf.isEmpty()) {
            words.add(buf.toString());
        }

        // 过滤停用词和单字
        return words.stream()
                .filter(w -> w.length() >= 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .distinct()
                .collect(Collectors.toList());
    }

    private record ScoredChunk(KbChunk chunk, double score) {}
}
