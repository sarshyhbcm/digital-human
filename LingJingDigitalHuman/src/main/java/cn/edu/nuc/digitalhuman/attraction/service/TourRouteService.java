package cn.edu.nuc.digitalhuman.attraction.service;

import cn.edu.nuc.digitalhuman.attraction.dto.RouteRecommendation;
import cn.edu.nuc.digitalhuman.attraction.entity.TourRoute;

import java.util.List;
import java.util.Map;

/**
 * 推荐路线业务接口
 */
public interface TourRouteService {

    /**
     * 查询所有启用的路线列表
     */
    List<TourRoute> listAll();

    /**
     * 获取路线详情（含路线信息和该路线包含的景点列表）
     *
     * @param id 路线ID
     * @return 包含 route 和 attractions 的 Map
     */
    Map<String, Object> getDetail(Long id);

    /**
     * 根据偏好推荐路线
     *
     * @param preference 用户偏好（如"历史文化""自然风光""亲子家庭"）
     * @return 按匹配度降序排列的推荐结果列表
     */
    List<RouteRecommendation> recommend(String preference);
}
