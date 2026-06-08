package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.banner.entity.Banner;
import cn.edu.nuc.digitalhuman.banner.service.BannerService;
import cn.edu.nuc.digitalhuman.common.config.UploadConfig;
import cn.edu.nuc.digitalhuman.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 管理端轮播图 CRUD
 */
@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class BannerAdminController {

    private final BannerService bannerService;
    private final UploadConfig uploadConfig;

    @GetMapping
    public R<List<Banner>> list() {
        return R.success(bannerService.listEnabled());
    }

    @PostMapping
    public R<Banner> create(@RequestBody Banner banner) {
        return R.success(bannerService.create(banner));
    }

    @PutMapping("/{id}")
    public R<Banner> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        return R.success(bannerService.update(banner));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return R.success(null);
    }

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        String url = uploadConfig.saveFile(file, "banners");
        return R.success(url);
    }
}
