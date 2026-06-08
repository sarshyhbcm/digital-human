package cn.edu.nuc.digitalhuman.digitalhuman.controller;

import cn.edu.nuc.digitalhuman.digitalhuman.dto.DigitalHumanConfigDto;
import cn.edu.nuc.digitalhuman.digitalhuman.dto.UpdateDigitalHumanConfigDto;
import cn.edu.nuc.digitalhuman.digitalhuman.service.DigitalHumanConfigService;
import cn.edu.nuc.digitalhuman.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/digital-human")
@RequiredArgsConstructor
public class DigitalHumanConfigController {

    private final DigitalHumanConfigService configService;

    @GetMapping("/config")
    public R<DigitalHumanConfigDto> getConfig() {
        return R.success(configService.getDefaultConfig());
    }

    @PutMapping("/config")
    public R<Void> updateConfig(@Valid @RequestBody UpdateDigitalHumanConfigDto dto) {
        configService.updateDefaultConfig(dto);
        return R.success();
    }
}
