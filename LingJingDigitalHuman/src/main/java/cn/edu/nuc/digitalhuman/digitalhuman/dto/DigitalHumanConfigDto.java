package cn.edu.nuc.digitalhuman.digitalhuman.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DigitalHumanConfigDto {
    private String name;
    private String greeting;
    private String style;
    private Boolean ttsEnabled;
    private Boolean asrEnabled;
    private String personality;
    private String model;
    private Double ttsSpeed;
}
