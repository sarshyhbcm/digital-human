package cn.edu.nuc.digitalhuman.attraction.dto;

import cn.edu.nuc.digitalhuman.attraction.entity.Attraction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OptimizeRouteResult {
    private List<Attraction> attractions;
    private List<RouteSegment> segments;
    private int totalDistanceMeters;
    private int estimatedMinutes;
}
