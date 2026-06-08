package cn.edu.nuc.digitalhuman.digitalhuman.service;

import cn.edu.nuc.digitalhuman.digitalhuman.dto.DigitalHumanConfigDto;
import cn.edu.nuc.digitalhuman.digitalhuman.dto.UpdateDigitalHumanConfigDto;

public interface DigitalHumanConfigService {
    DigitalHumanConfigDto getDefaultConfig();
    void updateDefaultConfig(UpdateDigitalHumanConfigDto dto);
}
