package cn.edu.nuc.digitalhuman.digitalhuman.service.impl;

import cn.edu.nuc.digitalhuman.digitalhuman.dto.DigitalHumanConfigDto;
import cn.edu.nuc.digitalhuman.digitalhuman.dto.UpdateDigitalHumanConfigDto;
import cn.edu.nuc.digitalhuman.digitalhuman.entity.DigitalHumanConfig;
import cn.edu.nuc.digitalhuman.digitalhuman.mapper.DigitalHumanConfigMapper;
import cn.edu.nuc.digitalhuman.digitalhuman.service.DigitalHumanConfigService;
import cn.edu.nuc.digitalhuman.common.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DigitalHumanConfigServiceImpl implements DigitalHumanConfigService {

    private final DigitalHumanConfigMapper configMapper;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_KEY = "default";

    private static final DigitalHumanConfigDto DEFAULT_CONFIG = DigitalHumanConfigDto.builder()
            .name("灵宝")
            .greeting("你好！我是灵宝，你的智能导游。欢迎来到灵山胜境！我可以为你介绍景点、推荐路线、解答问题。请问有什么可以帮你的吗？")
            .style("古风典雅")
            .ttsEnabled(true)
            .asrEnabled(true)
            .personality("温柔知性")
            .model("qwen-max")
            .ttsSpeed(1.0)
            .build();

    private static final String DEFAULT_CONFIG_JSON = """
{"name":"灵宝","greeting":"你好！我是灵宝，你的智能导游。欢迎来到灵山胜境！我可以为你介绍景点、推荐路线、解答问题。请问有什么可以帮你的吗？","style":"古风典雅","tts_enabled":true,"asr_enabled":true,"personality":"温柔知性","model":"qwen-max","tts_speed":1.0}
            """;

    @Override
    public DigitalHumanConfigDto getDefaultConfig() {
        DigitalHumanConfig entity = configMapper.selectOne(
                new LambdaQueryWrapper<DigitalHumanConfig>()
                        .eq(DigitalHumanConfig::getConfigKey, DEFAULT_KEY));
        if (entity == null || entity.getConfigValue() == null) {
            return DEFAULT_CONFIG;
        }
        try {
            return parseConfig(entity.getConfigValue());
        } catch (Exception e) {
            return DEFAULT_CONFIG;
        }
    }

    @Override
    public void updateDefaultConfig(UpdateDigitalHumanConfigDto dto) {
        DigitalHumanConfig entity = configMapper.selectOne(
                new LambdaQueryWrapper<DigitalHumanConfig>()
                        .eq(DigitalHumanConfig::getConfigKey, DEFAULT_KEY));

        if (entity == null) {
            entity = new DigitalHumanConfig();
            entity.setConfigKey(DEFAULT_KEY);
            entity.setConfigValue(DEFAULT_CONFIG_JSON);
            entity.setDescription("数字人默认配置");
        }

        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(entity.getConfigValue());

            if (dto.getName() != null) root.put("name", dto.getName());
            if (dto.getGreeting() != null) root.put("greeting", dto.getGreeting());
            if (dto.getStyle() != null) root.put("style", dto.getStyle());
            if (dto.getTtsEnabled() != null) root.put("tts_enabled", dto.getTtsEnabled());
            if (dto.getAsrEnabled() != null) root.put("asr_enabled", dto.getAsrEnabled());
            if (dto.getPersonality() != null) root.put("personality", dto.getPersonality());
            if (dto.getModel() != null) root.put("model", dto.getModel());
            if (dto.getTtsSpeed() != null) root.put("tts_speed", dto.getTtsSpeed());

            entity.setConfigValue(objectMapper.writeValueAsString(root));
            entity.setUpdatedAt(LocalDateTime.now());

            if (entity.getId() != null) {
                configMapper.updateById(entity);
            } else {
                configMapper.insert(entity);
            }
        } catch (Exception e) {
            throw new ServiceException(500, "配置解析失败");
        }
    }

    private DigitalHumanConfigDto parseConfig(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return DigitalHumanConfigDto.builder()
                    .name(getText(root, "name"))
                    .greeting(getText(root, "greeting"))
                    .style(getText(root, "style"))
                    .ttsEnabled(getBool(root, "tts_enabled"))
                    .asrEnabled(getBool(root, "asr_enabled"))
                    .personality(getText(root, "personality"))
                    .model(getText(root, "model"))
                    .ttsSpeed(getDouble(root, "tts_speed"))
                    .build();
        } catch (Exception e) {
            throw new ServiceException(500, "配置解析失败");
        }
    }

    private String getText(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asText(null) : null;
    }

    private Boolean getBool(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asBoolean() : null;
    }

    private Double getDouble(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asDouble() : null;
    }
}
