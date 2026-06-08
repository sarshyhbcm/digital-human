package cn.edu.nuc.digitalhuman.attraction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaveRouteRequest {
    @NotBlank
    private String name;
    @NotNull
    private List<Long> attractionIds;
    private int totalDistanceMeters;
    private int estimatedMinutes;
}
