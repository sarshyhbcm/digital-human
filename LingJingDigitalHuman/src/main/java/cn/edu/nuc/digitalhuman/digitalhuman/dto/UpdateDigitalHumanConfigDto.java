package cn.edu.nuc.digitalhuman.digitalhuman.dto;

import lombok.Data;

@Data
public class UpdateDigitalHumanConfigDto {
    private String name;
    private String greeting;
    private String style;
    private Boolean ttsEnabled;
    private Boolean asrEnabled;
    private String personality;
    private String model;
    private Double ttsSpeed;
}
