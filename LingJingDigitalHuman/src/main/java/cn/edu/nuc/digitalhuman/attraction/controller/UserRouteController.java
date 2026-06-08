package cn.edu.nuc.digitalhuman.attraction.controller;

import cn.edu.nuc.digitalhuman.attraction.dto.SaveRouteRequest;
import cn.edu.nuc.digitalhuman.attraction.dto.UserRouteDetail;
import cn.edu.nuc.digitalhuman.attraction.entity.UserRoute;
import cn.edu.nuc.digitalhuman.attraction.service.UserRouteService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-routes")
@RequiredArgsConstructor
public class UserRouteController {

    private final UserRouteService userRouteService;

    @PostMapping
    public R<Void> save(@Valid @RequestBody SaveRouteRequest dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userRouteService.saveRoute(userId, dto);
        return R.success(null);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SaveRouteRequest dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userRouteService.updateRoute(userId, id, dto);
        return R.success(null);
    }

    @PostMapping("/from-tour/{routeId}")
    public R<Void> saveFromTourRoute(@PathVariable Long routeId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userRouteService.saveFromTourRoute(userId, routeId);
        return R.success(null);
    }

    @GetMapping
    public R<List<UserRoute>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(userRouteService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public R<UserRouteDetail> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(userRouteService.getDetail(id, userId));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userRouteService.deleteRoute(id, userId);
        return R.success(null);
    }
}
