package cn.edu.nuc.digitalhuman.admin.controller;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.service.AttractionService;
import cn.edu.nuc.digitalhuman.common.result.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理端景点 CRUD
 */
@RestController
@RequestMapping("/api/admin/attractions")
@RequiredArgsConstructor
public class AttractionAdminController {

    private final AttractionService attractionService;

    @GetMapping
    public R<IPage<Attraction>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String keyword) {
        return R.success(attractionService.adminPage(new Page<>(page, pageSize), area, keyword));
    }

    @GetMapping("/{id}")
    public R<Attraction> detail(@PathVariable Long id) {
        return R.success(attractionService.getById(id));
    }

    @PostMapping
    public R<Attraction> create(@RequestBody Attraction attraction) {
        return R.success(attractionService.create(attraction));
    }

    @PutMapping("/{id}")
    public R<Attraction> update(@PathVariable Long id, @RequestBody Attraction attraction) {
        attraction.setId(id);
        return R.success(attractionService.update(attraction));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        attractionService.delete(id);
        return R.success(null);
    }

    @PostMapping("/{id}/cover")
    public R<String> uploadCover(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String url = attractionService.uploadCover(id, file);
        return R.success(url);
    }

    @PostMapping("/{id}/images")
    public R<String> uploadImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        String images = attractionService.uploadImages(id, files);
        return R.success(images);
    }

    @DeleteMapping("/{id}/images")
    public R<Attraction> removeImage(@PathVariable Long id, @RequestBody RemoveImageDto dto) {
        return R.success(attractionService.removeImage(id, dto.getUrl()));
    }

    @lombok.Data
    public static class RemoveImageDto {
        private String url;
    }
}
