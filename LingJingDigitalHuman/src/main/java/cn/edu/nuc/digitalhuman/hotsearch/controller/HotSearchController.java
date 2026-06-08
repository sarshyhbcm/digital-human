package cn.edu.nuc.digitalhuman.hotsearch.controller;

import cn.edu.nuc.digitalhuman.common.result.R;
import cn.edu.nuc.digitalhuman.hotsearch.entity.HotSearch;
import cn.edu.nuc.digitalhuman.hotsearch.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hot-searches")
@RequiredArgsConstructor
public class HotSearchController {

    private final HotSearchService hotSearchService;

    /**
     * 游客端：获取启用的热搜列表
     */
    @GetMapping
    public R<List<HotSearch>> getEnabledList() {
        return R.success(hotSearchService.listEnabled());
    }
}
