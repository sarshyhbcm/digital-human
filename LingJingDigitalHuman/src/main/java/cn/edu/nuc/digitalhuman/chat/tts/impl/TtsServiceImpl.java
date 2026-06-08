package cn.edu.nuc.digitalhuman.chat.tts.impl;

import cn.edu.nuc.digitalhuman.chat.tts.TtsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class TtsServiceImpl implements TtsService {

    private final String apiKey;
    private final String model;
    private final String voice;
    private final String baseUrl;
    private final String uploadPath;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TtsServiceImpl(
            @Value("${tts.api-key}") String apiKey,
            @Value("${tts.model}") String model,
            @Value("${tts.voice}") String voice,
            @Value("${tts.base-url}") String baseUrl,
            @Value("${upload.path}") String uploadPath) {
        this.apiKey = apiKey;
        this.model = model;
        this.voice = voice;
        this.baseUrl = baseUrl;
        this.uploadPath = uploadPath;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String synthesize(String text, double speed) {
        try {
            Map<String, Object> input = Map.of(
                    "text", text,
                    "voice", voice,
                    "format", "mp3",
                    "sample_rate", 24000,
                    "speed", speed
            );
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "input", input
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/SpeechSynthesizer"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("TTS API error: status={}, body={}", response.statusCode(), response.body());
                return null;
            }

            // V3 Flash 非流式 API 返回 JSON: output.audio.url
            JsonNode root = objectMapper.readTree(response.body());
            String audioFileUrl = root.path("output").path("audio").path("url").asText();
            if (audioFileUrl == null || audioFileUrl.isBlank()) {
                log.error("TTS API 响应中没有 audio URL: {}", response.body());
                return null;
            }

            // 下载音频文件
            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(audioFileUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<InputStream> downloadResponse = httpClient.send(downloadRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (downloadResponse.statusCode() != 200) {
                log.error("下载音频文件失败: status={}", downloadResponse.statusCode());
                return null;
            }

            // 保存音频文件
            String fileName = "tts_" + UUID.randomUUID().toString().replace("-", "") + ".mp3";
            Path voiceDir = Paths.get(uploadPath, "voice");
            Files.createDirectories(voiceDir);
            Path targetPath = voiceDir.resolve(fileName);

            try (InputStream is = downloadResponse.body()) {
                Files.copy(is, targetPath);
            }

            String audioUrl = "/uploads/voice/" + fileName;
            log.info("TTS V3 Flash 合成成功: textLen={}, url={}", text.length(), audioUrl);
            return audioUrl;

        } catch (Exception e) {
            log.error("TTS API call failed", e);
            return null;
        }
    }
}
