package cn.edu.nuc.digitalhuman.attraction.controller;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import cn.edu.nuc.digitalhuman.attraction.service.AttractionService;
import cn.edu.nuc.digitalhuman.common.result.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 景点控制器
 *
 * 提供景点列表（分页+按区域筛选）和景点详情的查询接口。
 * 这两个接口都是游客端高频访问的，无需登录即可查看。
 */
@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    /**
     * 景点列表（分页）
     *
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param area 区域筛选（可选），传"灵山胜境"或"拈花湾"
     */
    @GetMapping
    public R<IPage<Attraction>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String area) {
        IPage<Attraction> result = attractionService.page(new Page<>(page, size), area);
        return R.success(result);
    }

    /**
     * 景点详情
     *
     * @param id 景点ID
     */
    @GetMapping("/{id}")
    public R<Attraction> detail(@PathVariable Long id) {
        Attraction attraction = attractionService.getById(id);
        return R.success(attraction);
    }
}
