package cn.edu.nuc.digitalhuman.attraction.controller;

import cn.edu.nuc.digitalhuman.attraction.dto.OptimizeRouteRequest;
import cn.edu.nuc.digitalhuman.attraction.dto.OptimizeRouteResult;
import cn.edu.nuc.digitalhuman.attraction.service.CustomRouteService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes/custom")
@RequiredArgsConstructor
public class CustomRouteController {

    private final CustomRouteService customRouteService;

    /**
     * 自定义路线优化：使用贪心最近邻算法对所选景点进行游览顺序排序
     *
     * @param request 包含用户选择的景点ID列表
     * @return 优化后的路线（含排序景点、路段距离、总时长）
     */
    @PostMapping("/optimize")
    public R<OptimizeRouteResult> optimize(@Valid @RequestBody OptimizeRouteRequest request) {
        return R.success(customRouteService.optimize(request.getAttractionIds()));
    }
}
