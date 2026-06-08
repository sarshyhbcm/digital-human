package cn.edu.nuc.digitalhuman.hotsearch.service.impl;

import cn.edu.nuc.digitalhuman.hotsearch.entity.HotSearch;
import cn.edu.nuc.digitalhuman.hotsearch.mapper.HotSearchMapper;
import cn.edu.nuc.digitalhuman.hotsearch.service.HotSearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class HotSearchServiceImpl implements HotSearchService {

    private final HotSearchMapper hotSearchMapper;

    @Override
    public List<HotSearch> listAll() {
        return hotSearchMapper.selectList(
                new LambdaQueryWrapper<HotSearch>()
                        .orderByAsc(HotSearch::getSortOrder)
                        .orderByDesc(HotSearch::getId));
    }

    @Override
    public List<HotSearch> listEnabled() {
        return hotSearchMapper.selectList(
                new LambdaQueryWrapper<HotSearch>()
                        .eq(HotSearch::getEnabled, true)
                        .orderByAsc(HotSearch::getSortOrder));
    }

    @Override
    public HotSearch create(HotSearch hotSearch) {
        if (hotSearch.getSortOrder() == null) {
            hotSearch.setSortOrder(0);
        }
        if (hotSearch.getHeatLabel() == null) {
            hotSearch.setHeatLabel("wen");
        }
        if (hotSearch.getEnabled() == null) {
            hotSearch.setEnabled(true);
        }
        hotSearchMapper.insert(hotSearch);
        return hotSearch;
    }

    @Override
    public HotSearch update(HotSearch hotSearch) {
        hotSearchMapper.updateById(hotSearch);
        return hotSearchMapper.selectById(hotSearch.getId());
    }

    @Override
    public void delete(Long id) {
        hotSearchMapper.deleteById(id);
    }
}
