package cn.edu.nuc.digitalhuman.banner.controller;

import cn.edu.nuc.digitalhuman.banner.entity.Banner;
import cn.edu.nuc.digitalhuman.banner.service.BannerService;
import cn.edu.nuc.digitalhuman.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 游客端轮播图接口（无需登录）
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public R<List<Banner>> list() {
        return R.success(bannerService.listEnabled());
    }
}
