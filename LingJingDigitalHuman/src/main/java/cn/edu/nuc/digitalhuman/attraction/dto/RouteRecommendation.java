package cn.edu.nuc.digitalhuman.attraction.dto;

import cn.edu.nuc.digitalhuman.attraction.entity.TourRoute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 路线推荐结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRecommendation {
    private TourRoute route;
    private double matchScore;
    private List<String> matchedTags;
}
