package cn.edu.nuc.digitalhuman.attraction.service;

import cn.edu.nuc.digitalhuman.attraction.dto.SaveRouteRequest;
import cn.edu.nuc.digitalhuman.attraction.dto.UserRouteDetail;
import cn.edu.nuc.digitalhuman.attraction.entity.UserRoute;

import java.util.List;

public interface UserRouteService {

    void saveRoute(Long userId, SaveRouteRequest request);

    /**
     * 更新自定义路线
     */
    void updateRoute(Long userId, Long routeId, SaveRouteRequest request);

    /**
     * 将推荐路线保存为用户的自定义路线
     *
     * @param userId  用户ID
     * @param routeId 推荐路线ID
     */
    void saveFromTourRoute(Long userId, Long routeId);

    List<UserRoute> listByUser(Long userId);

    UserRouteDetail getDetail(Long id, Long userId);

    void deleteRoute(Long id, Long userId);
}
