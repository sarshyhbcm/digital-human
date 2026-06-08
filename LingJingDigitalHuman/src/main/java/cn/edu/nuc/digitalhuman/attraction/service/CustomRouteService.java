package cn.edu.nuc.digitalhuman.attraction.service;

import cn.edu.nuc.digitalhuman.attraction.dto.OptimizeRouteResult;
import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;

import java.util.List;

public interface CustomRouteService {

    /**
     * 使用贪心最近邻算法对景点进行游览顺序优化
     */
    OptimizeRouteResult optimize(List<Long> attractionIds);

    /**
     * 根据已排序的景点列表计算路段距离和总耗时
     */
    OptimizeRouteResult calculateSegments(List<Attraction> sortedAttractions);
}
