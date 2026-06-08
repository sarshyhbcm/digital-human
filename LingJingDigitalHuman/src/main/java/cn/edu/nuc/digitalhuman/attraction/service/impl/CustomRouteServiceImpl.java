package cn.edu.nuc.digitalhuman.attraction.service.impl;

import cn.edu.nuc.digitalhuman.attraction.dto.OptimizeRouteResult;
import cn.edu.nuc.digitalhuman.attraction.dto.RouteSegment;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.attraction.service.CustomRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomRouteServiceImpl implements CustomRouteService {

    private final AttractionMapper attractionMapper;

    // 景区入口坐标（作为游览起点）
    private static final double ENTRANCE_LINGSHAN_LAT = 31.4380;
    private static final double ENTRANCE_LINGSHAN_LNG = 120.0870;
    private static final double ENTRANCE_NIANHUA_LAT = 31.4185;
    private static final double ENTRANCE_NIANHUA_LNG = 120.1030;

    // 步行速度：米/分钟
    private static final double WALK_SPEED = 70;
    // 每个景点停留时间：分钟
    private static final int VISIT_TIME_PER_ATTRACTION = 20;

    @Override
    public OptimizeRouteResult optimize(List<Long> attractionIds) {
        if (attractionIds == null || attractionIds.isEmpty()) {
            return new OptimizeRouteResult(Collections.emptyList(), Collections.emptyList(), 0, 0);
        }

        List<Attraction> attractions = attractionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attraction>()
                        .in(Attraction::getId, attractionIds));
        if (attractions.isEmpty()) {
            return new OptimizeRouteResult(Collections.emptyList(), Collections.emptyList(), 0, 0);
        }

        // 确定入口坐标
        boolean hasLingshan = attractions.stream().anyMatch(a -> "灵山胜境".equals(a.getArea()));
        double startLat = hasLingshan ? ENTRANCE_LINGSHAN_LAT : ENTRANCE_NIANHUA_LAT;
        double startLng = hasLingshan ? ENTRANCE_LINGSHAN_LNG : ENTRANCE_NIANHUA_LNG;

        // 贪心最近邻排序
        List<Attraction> sorted = greedyNearestNeighbor(attractions, startLat, startLng);

        // 计算路段和总距离
        List<RouteSegment> segments = new ArrayList<>();
        double totalDistance = 0;

        // 从入口到第一个景点
        double firstDist = haversine(startLat, startLng,
                sorted.get(0).getLatitude().doubleValue(),
                sorted.get(0).getLongitude().doubleValue());
        totalDistance += firstDist;

        // 景点之间的路段
        for (int i = 0; i < sorted.size() - 1; i++) {
            Attraction from = sorted.get(i);
            Attraction to = sorted.get(i + 1);
            double dist = haversine(
                    from.getLatitude().doubleValue(), from.getLongitude().doubleValue(),
                    to.getLatitude().doubleValue(), to.getLongitude().doubleValue());
            int distInt = (int) Math.round(dist);
            segments.add(new RouteSegment(from.getId(), from.getName(), to.getId(), to.getName(), distInt));
            totalDistance += dist;
        }

        int totalDistInt = (int) Math.round(totalDistance);
        int estimatedMinutes = (int) Math.round(totalDistance / WALK_SPEED) + sorted.size() * VISIT_TIME_PER_ATTRACTION;

        return new OptimizeRouteResult(sorted, segments, totalDistInt, estimatedMinutes);
    }

    @Override
    public OptimizeRouteResult calculateSegments(List<Attraction> sortedAttractions) {
        if (sortedAttractions == null || sortedAttractions.isEmpty()) {
            return new OptimizeRouteResult(Collections.emptyList(), Collections.emptyList(), 0, 0);
        }

        List<RouteSegment> segments = new ArrayList<>();
        double totalDistance = 0;

        for (int i = 0; i < sortedAttractions.size() - 1; i++) {
            Attraction from = sortedAttractions.get(i);
            Attraction to = sortedAttractions.get(i + 1);
            double dist = haversine(
                    from.getLatitude().doubleValue(), from.getLongitude().doubleValue(),
                    to.getLatitude().doubleValue(), to.getLongitude().doubleValue());
            int distInt = (int) Math.round(dist);
            segments.add(new RouteSegment(from.getId(), from.getName(), to.getId(), to.getName(), distInt));
            totalDistance += dist;
        }

        int totalDistInt = (int) Math.round(totalDistance);
        int estimatedMinutes = (int) Math.round(totalDistance / WALK_SPEED)
                + sortedAttractions.size() * VISIT_TIME_PER_ATTRACTION;

        return new OptimizeRouteResult(sortedAttractions, segments, totalDistInt, estimatedMinutes);
    }

    /**
     * 贪心最近邻算法：每次选择离当前位置最近的未访问景点
     */
    private List<Attraction> greedyNearestNeighbor(List<Attraction> attractions, double startLat, double startLng) {
        List<Attraction> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        // 第一步：找离入口最近的景点
        Attraction current = null;
        double minDist = Double.MAX_VALUE;
        for (Attraction a : attractions) {
            double d = haversine(startLat, startLng,
                    a.getLatitude().doubleValue(), a.getLongitude().doubleValue());
            if (d < minDist) {
                minDist = d;
                current = a;
            }
        }

        if (current == null) return result;

        result.add(current);
        visited.add(current.getId());

        // 后续步骤：每次找离当前景点最近的未访问景点
        while (result.size() < attractions.size()) {
            Attraction nearest = null;
            double nearestDist = Double.MAX_VALUE;
            double curLat = current.getLatitude().doubleValue();
            double curLng = current.getLongitude().doubleValue();

            for (Attraction a : attractions) {
                if (visited.contains(a.getId())) continue;
                double d = haversine(curLat, curLng,
                        a.getLatitude().doubleValue(), a.getLongitude().doubleValue());
                if (d < nearestDist) {
                    nearestDist = d;
                    nearest = a;
                }
            }

            if (nearest == null) break;
            result.add(nearest);
            visited.add(nearest.getId());
            current = nearest;
        }

        return result;
    }

    /**
     * Haversine公式计算两点间距离（米）
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
