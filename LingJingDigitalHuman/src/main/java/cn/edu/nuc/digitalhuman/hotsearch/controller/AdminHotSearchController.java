package cn.edu.nuc.digitalhuman.hotsearch.controller;

import cn.edu.nuc.digitalhuman.common.result.R;
import cn.edu.nuc.digitalhuman.hotsearch.entity.HotSearch;
import cn.edu.nuc.digitalhuman.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hot-searches")
@RequiredArgsConstructor
public class AdminHotSearchController {

    private final HotSearchService hotSearchService;

    @GetMapping
    public R<List<HotSearch>> list() {
        return R.success(hotSearchService.listAll());
    }

    @PostMapping
    public R<HotSearch> create(@RequestBody HotSearch hotSearch) {
        return R.success(hotSearchService.create(hotSearch));
    }

    @PutMapping("/{id}")
    public R<HotSearch> update(@PathVariable Long id, @RequestBody HotSearch hotSearch) {
        hotSearch.setId(id);
        return R.success(hotSearchService.update(hotSearch));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        hotSearchService.delete(id);
        return R.success(null);
    }
}
