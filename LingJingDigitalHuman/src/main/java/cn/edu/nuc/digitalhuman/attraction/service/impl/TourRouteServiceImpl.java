package cn.edu.nuc.digitalhuman.attraction.service.impl;

import cn.edu.nuc.digitalhuman.attraction.dto.RouteRecommendation;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.entity.RouteAttraction;
import cn.edu.nuc.digitalhuman.attraction.entity.TourRoute;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.attraction.mapper.RouteAttractionMapper;
import cn.edu.nuc.digitalhuman.attraction.mapper.TourRouteMapper;
import cn.edu.nuc.digitalhuman.attraction.service.TourRouteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐路线业务实现
 *
 * 路线详情通过 route_attraction 关联表查询该路线包含的景点，
 * 按 sort_order 排序后返回。
 * 推荐逻辑基于路线 tags 字段与用户偏好进行匹配打分。
 */
@Service
@RequiredArgsConstructor
public class TourRouteServiceImpl implements TourRouteService {

    private final TourRouteMapper tourRouteMapper;
    private final RouteAttractionMapper routeAttractionMapper;
    private final AttractionMapper attractionMapper;

    @Override
    public List<TourRoute> listAll() {
        return tourRouteMapper.selectList(
                new LambdaQueryWrapper<TourRoute>()
                        .eq(TourRoute::getStatus, 1)
                        .orderByAsc(TourRoute::getSortOrder)
        );
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        TourRoute route = tourRouteMapper.selectById(id);

        // 查询该路线的景点关联，按 sort_order 排序
        List<RouteAttraction> relations = routeAttractionMapper.selectList(
                new LambdaQueryWrapper<RouteAttraction>()
                        .eq(RouteAttraction::getRouteId, id)
                        .orderByAsc(RouteAttraction::getSortOrder)
        );

        // 收集景点ID列表
        List<Long> attractionIds = relations.stream()
                .map(RouteAttraction::getAttractionId)
                .collect(Collectors.toList());

        // 批量查询景点，并按关联表的顺序排列
        List<Attraction> attractions = new ArrayList<>();
        if (!attractionIds.isEmpty()) {
            List<Attraction> attractionList = attractionMapper.selectList(
                    new LambdaQueryWrapper<Attraction>().in(Attraction::getId, attractionIds));
            Map<Long, Attraction> attractionMap = attractionList.stream()
                    .collect(Collectors.toMap(Attraction::getId, a -> a));
            for (Long aid : attractionIds) {
                Attraction a = attractionMap.get(aid);
                if (a != null) {
                    attractions.add(a);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("route", route);
        result.put("attractions", attractions);
        return result;
    }

    @Override
    public List<RouteRecommendation> recommend(String preference) {
        List<TourRoute> routes = listAll();
        if (routes.isEmpty()) {
            return Collections.emptyList();
        }

        List<RouteRecommendation> result = new ArrayList<>();
        for (TourRoute route : routes) {
            double score = calculateMatchScore(route, preference);
            List<String> matchedTags = findMatchedTags(route, preference);
            result.add(new RouteRecommendation(route, score, matchedTags));
        }

        // 按匹配度降序排列
        result.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        return result;
    }

    /**
     * 计算路线与偏好的匹配度（0.0 ~ 1.0）
     */
    private double calculateMatchScore(TourRoute route, String preference) {
        if (preference == null || preference.isEmpty()) {
            return 0;
        }

        double score = 0.0;

        // 1. tags 精确匹配（最高优先级）
        if (route.getTags() != null) {
            String[] tags = route.getTags().split(",");
            for (String tag : tags) {
                if (tag.trim().equals(preference)) {
                    score = Math.max(score, 1.0);
                }
            }
        }

        // 2. 路线名称包含偏好关键词
        if (route.getName() != null && route.getName().contains(preference)) {
            score = Math.max(score, 0.8);
        }

        // 3. 路线描述包含偏好关键词
        if (route.getDescription() != null && route.getDescription().contains(preference)) {
            score = Math.max(score, 0.6);
        }

        return score;
    }

    /**
     * 找出路线中与偏好匹配的标签
     */
    private List<String> findMatchedTags(TourRoute route, String preference) {
        List<String> matched = new ArrayList<>();
        if (route.getTags() != null) {
            String[] tags = route.getTags().split(",");
            for (String tag : tags) {
                String trimmed = tag.trim();
                if (trimmed.equals(preference)) {
                    matched.add(trimmed);
                }
            }
        }
        // 如果名称匹配但没有精确标签匹配，用偏好本身作为说明
        if (matched.isEmpty() && route.getName() != null && route.getName().contains(preference)) {
            matched.add(preference);
        }
        return matched;
    }
}
