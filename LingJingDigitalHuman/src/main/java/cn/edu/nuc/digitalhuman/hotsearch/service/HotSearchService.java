package cn.edu.nuc.digitalhuman.hotsearch.service;

import cn.edu.nuc.digitalhuman.hotsearch.entity.HotSearch;

import java.util.List;

public interface HotSearchService {

    List<HotSearch> listAll();

    List<HotSearch> listEnabled();

    HotSearch create(HotSearch hotSearch);

    HotSearch update(HotSearch hotSearch);

    void delete(Long id);
}
