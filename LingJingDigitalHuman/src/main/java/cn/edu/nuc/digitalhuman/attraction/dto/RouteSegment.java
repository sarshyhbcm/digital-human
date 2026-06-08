package cn.edu.nuc.digitalhuman.attraction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegment {
    private Long fromId;
    private String fromName;
    private Long toId;
    private String toName;
    private int distanceMeters;
}
