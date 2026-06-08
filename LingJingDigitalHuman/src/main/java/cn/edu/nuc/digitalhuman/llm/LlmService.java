package cn.edu.nuc.digitalhuman.llm;

import cn.edu.nuc.digitalhuman.digitalhuman.service.DigitalHumanConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@Slf4j
public class LlmService {

    private final String apiKey;
    private final String defaultModel;
    private final String baseUrl;
    private final DigitalHumanConfigService configService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LlmService(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.model}") String defaultModel,
            @Value("${llm.base-url}") String baseUrl,
            DigitalHumanConfigService configService) {
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.baseUrl = baseUrl;
        this.configService = configService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private String resolveModel() {
        try {
            String model = configService.getDefaultConfig().getModel();
            if (model != null && !model.isEmpty()) {
                return model;
            }
        } catch (Exception e) {
            log.warn("Failed to read model from config, using default", e);
        }
        return defaultModel;
    }

    public String chat(String systemPrompt, List<Map<String, String>> messages) {
        try {
            List<Object> requestMessages = new ArrayList<>();
            requestMessages.add(Map.of("role", "system", "content", systemPrompt));
            requestMessages.addAll(messages);

            String model = resolveModel();

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", requestMessages,
                    "temperature", 0.7,
                    "max_tokens", 1024
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("LLM API error: status={}, body={}", response.statusCode(), response.body());
                return "抱歉，我暂时无法回答，请稍后再试。";
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("LLM API call failed", e);
            return "抱歉，我暂时无法回答，请稍后再试。";
        }
    }

    /**
     * 流式聊天：逐 token 回调 onChunk，完成后回调 onComplete
     */
    public void chatStream(String systemPrompt, List<Map<String, String>> messages,
                           Consumer<String> onChunk, Runnable onComplete, Consumer<String> onError) {
        try {
            List<Object> requestMessages = new ArrayList<>();
            requestMessages.add(Map.of("role", "system", "content", systemPrompt));
            requestMessages.addAll(messages);

            String model = resolveModel();

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", requestMessages,
                    "temperature", 0.7,
                    "max_tokens", 1024,
                    "stream", true
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(120))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            log.error("LLM stream error: status={}", response.statusCode());
                            onError.accept("LLM API error: " + response.statusCode());
                            return;
                        }
                        boolean[] receivedDone = { false };
                        response.body().forEach(line -> {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if ("[DONE]".equals(data)) {
                                    onComplete.run();
                                    receivedDone[0] = true;
                                    return;
                                }
                                try {
                                    JsonNode root = objectMapper.readTree(data);
                                    JsonNode delta = root.path("choices").get(0).path("delta");
                                    if (delta.has("content")) {
                                        onChunk.accept(delta.path("content").asText());
                                    }
                                } catch (Exception e) {
                                    // skip unparseable lines
                                }
                            }
                        });
                        if (!receivedDone[0]) {
                            onComplete.run();
                        }
                    })
                    .exceptionally(e -> {
                        log.error("LLM stream error", e);
                        onError.accept(e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("LLM stream setup failed", e);
            onError.accept(e.getMessage());
        }
    }
}
