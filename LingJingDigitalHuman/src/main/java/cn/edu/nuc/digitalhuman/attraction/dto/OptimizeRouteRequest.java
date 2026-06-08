package cn.edu.nuc.digitalhuman.attraction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OptimizeRouteRequest {
    @NotNull
    private List<Long> attractionIds;
}
