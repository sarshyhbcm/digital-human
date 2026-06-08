package cn.edu.nuc.digitalhuman.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckInDto {

    @NotNull(message = "景点ID不能为空")
    private Long attractionId;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String checkInType;
}
