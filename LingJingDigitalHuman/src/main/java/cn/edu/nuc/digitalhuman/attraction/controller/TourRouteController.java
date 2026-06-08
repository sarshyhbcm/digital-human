package cn.edu.nuc.digitalhuman.attraction.controller;

import cn.edu.nuc.digitalhuman.attraction.dto.RouteRecommendation;
import cn.edu.nuc.digitalhuman.attraction.entity.TourRoute;
import cn.edu.nuc.digitalhuman.attraction.service.TourRouteService;
import cn.edu.nuc.digitalhuman.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 推荐路线控制器
 *
 * 提供路线列表、路线详情和偏好推荐接口。
 * 详情接口会返回该路线包含的景点列表（按游览顺序排列）。
 * 推荐接口根据用户偏好（如"历史文化"）返回匹配度排序的路线列表。
 */
@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class TourRouteController {

    private final TourRouteService tourRouteService;

    /**
     * 路线列表
     */
    @GetMapping
    public R<List<TourRoute>> list() {
        return R.success(tourRouteService.listAll());
    }

    /**
     * 根据偏好推荐路线
     *
     * @param preference 用户偏好：历史文化、自然风光、亲子家庭
     * @return 按匹配度降序排列的路线列表，包含匹配分数和匹配标签
     */
    @GetMapping("/recommend")
    public R<List<RouteRecommendation>> recommend(@RequestParam String preference) {
        return R.success(tourRouteService.recommend(preference));
    }

    /**
     * 路线详情（含景点列表）
     *
     * @param id 路线ID
     * @return 包含 route 信息和 attractions 景点列表
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.success(tourRouteService.getDetail(id));
    }
}
