package cn.edu.nuc.digitalhuman.attraction.service.impl;

import cn.edu.nuc.digitalhuman.attraction.dto.OptimizeRouteResult;
import cn.edu.nuc.digitalhuman.attraction.dto.SaveRouteRequest;
import cn.edu.nuc.digitalhuman.attraction.dto.UserRouteDetail;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.entity.TourRoute;
import cn.edu.nuc.digitalhuman.attraction.entity.UserRoute;
import cn.edu.nuc.digitalhuman.attraction.mapper.AttractionMapper;
import cn.edu.nuc.digitalhuman.attraction.mapper.UserRouteMapper;
import cn.edu.nuc.digitalhuman.attraction.service.CustomRouteService;
import cn.edu.nuc.digitalhuman.attraction.service.TourRouteService;
import cn.edu.nuc.digitalhuman.attraction.service.UserRouteService;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRouteServiceImpl implements UserRouteService {

    private final UserRouteMapper userRouteMapper;
    private final AttractionMapper attractionMapper;
    private final CustomRouteService customRouteService;
    private final TourRouteService tourRouteService;
    private final ObjectMapper objectMapper;

    @Override
    public void saveRoute(Long userId, SaveRouteRequest request) {
        try {
            String idsJson = objectMapper.writeValueAsString(request.getAttractionIds());
            UserRoute route = new UserRoute();
            route.setUserId(userId);
            route.setName(request.getName());
            route.setAttractionIds(idsJson);
            route.setTotalDistanceMeters(request.getTotalDistanceMeters());
            route.setEstimatedMinutes(request.getEstimatedMinutes());
            route.setAttractionCount(request.getAttractionIds().size());
            userRouteMapper.insert(route);
        } catch (JsonProcessingException e) {
            throw new ServiceException(500, "数据解析失败");
        }
    }

    @Override
    public void updateRoute(Long userId, Long routeId, SaveRouteRequest request) {
        UserRoute existing = userRouteMapper.selectOne(
                new LambdaQueryWrapper<UserRoute>()
                        .eq(UserRoute::getId, routeId)
                        .eq(UserRoute::getUserId, userId));
        if (existing == null) {
            throw new ServiceException(404, "路线不存在");
        }
        try {
            String idsJson = objectMapper.writeValueAsString(request.getAttractionIds());
            existing.setName(request.getName());
            existing.setAttractionIds(idsJson);
            existing.setTotalDistanceMeters(request.getTotalDistanceMeters());
            existing.setEstimatedMinutes(request.getEstimatedMinutes());
            existing.setAttractionCount(request.getAttractionIds().size());
            userRouteMapper.updateById(existing);
        } catch (JsonProcessingException e) {
            throw new ServiceException(500, "数据解析失败");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveFromTourRoute(Long userId, Long routeId) {
        Map<String, Object> detail = tourRouteService.getDetail(routeId);
        TourRoute tourRoute = (TourRoute) detail.get("route");
        List<Attraction> attractions = (List<Attraction>) detail.get("attractions");

        if (tourRoute == null || attractions == null || attractions.isEmpty()) {
            throw new ServiceException(404, "路线不存在");
        }

        // 计算路程和耗时
        OptimizeRouteResult result = customRouteService.calculateSegments(attractions);

        try {
            List<Long> attractionIds = attractions.stream()
                    .map(Attraction::getId)
                    .collect(Collectors.toList());
            String idsJson = objectMapper.writeValueAsString(attractionIds);

            UserRoute userRoute = new UserRoute();
            userRoute.setUserId(userId);
            userRoute.setName(tourRoute.getName());
            userRoute.setAttractionIds(idsJson);
            userRoute.setTotalDistanceMeters(result.getTotalDistanceMeters());
            userRoute.setEstimatedMinutes(result.getEstimatedMinutes());
            userRoute.setAttractionCount(attractionIds.size());
            userRouteMapper.insert(userRoute);
        } catch (JsonProcessingException e) {
            throw new ServiceException(500, "数据解析失败");
        }
    }

    @Override
    public List<UserRoute> listByUser(Long userId) {
        return userRouteMapper.selectList(
                new LambdaQueryWrapper<UserRoute>()
                        .eq(UserRoute::getUserId, userId)
                        .orderByDesc(UserRoute::getCreatedAt)
        );
    }

    @Override
    public UserRouteDetail getDetail(Long id, Long userId) {
        UserRoute route = userRouteMapper.selectOne(
                new LambdaQueryWrapper<UserRoute>()
                        .eq(UserRoute::getId, id)
                        .eq(UserRoute::getUserId, userId)
        );
        if (route == null) {
            throw new ServiceException(404, "路线不存在");
        }

        List<Long> attractionIds;
        try {
            attractionIds = objectMapper.readValue(route.getAttractionIds(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
        } catch (Exception e) {
            throw new ServiceException(500, "数据解析失败");
        }

        // 查询景点并按存储顺序排列
        List<Attraction> attractions = attractionMapper.selectList(
                new LambdaQueryWrapper<Attraction>().in(Attraction::getId, attractionIds));
        Map<Long, Attraction> attractionMap = attractions.stream()
                .collect(Collectors.toMap(Attraction::getId, a -> a));
        List<Attraction> sortedAttractions = new ArrayList<>();
        for (Long aid : attractionIds) {
            Attraction a = attractionMap.get(aid);
            if (a != null) sortedAttractions.add(a);
        }

        // 重算路段
        OptimizeRouteResult optimizeResult = customRouteService.calculateSegments(sortedAttractions);

        return new UserRouteDetail(
                route.getId(), route.getName(),
                sortedAttractions,
                optimizeResult.getSegments(),
                optimizeResult.getTotalDistanceMeters(),
                optimizeResult.getEstimatedMinutes(),
                sortedAttractions.size()
        );
    }

    @Override
    public void deleteRoute(Long id, Long userId) {
        userRouteMapper.delete(
                new LambdaQueryWrapper<UserRoute>()
                        .eq(UserRoute::getId, id)
                        .eq(UserRoute::getUserId, userId)
        );
    }
}
