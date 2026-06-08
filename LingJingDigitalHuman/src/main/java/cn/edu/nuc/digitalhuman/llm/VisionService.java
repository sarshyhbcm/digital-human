package cn.edu.nuc.digitalhuman.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@Slf4j
public class VisionService {

    private final String apiKey;
    private final String secretKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public VisionService(
            @Value("${baidu.api-key}") String apiKey,
            @Value("${baidu.secret-key}") String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 识别图片中的景点，返回景点名称
     * @param imageBytes 图片字节数组
     * @param prompt     忽略（百度 API 不使用 prompt）
     */
    public String recognize(byte[] imageBytes, String prompt) {
        try {
            // 1. 优先用地标识别
            String landmark = recognizeLandmark(imageBytes);
            if (landmark != null && !landmark.isBlank()) {
                log.info("地标识别结果: {}", landmark);
                return landmark;
            }

            // 2. 降级为通用物体识别
            String general = recognizeGeneral(imageBytes);
            if (general != null && !general.isBlank()) {
                log.info("通用识别结果: {}", general);
                return general;
            }

            return null;
        } catch (Exception e) {
            log.error("百度图像识别失败", e);
            return null;
        }
    }

    /** 地标识别 */
    private String recognizeLandmark(byte[] imageBytes) throws Exception {
        String token = getAccessToken();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String body = "image=" + URLEncoder.encode(base64, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://aip.baidubce.com/rest/2.0/image-classify/v1/landmark?access_token=" + token))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());

        if (root.has("error_code") && root.get("error_code").asInt() != 0) {
            log.error("地标识别错误: {} {}", root.get("error_code"), root.get("error_msg"));
            return null;
        }

        JsonNode result = root.path("result");
        if (result.isArray() && result.size() > 0) {
            return result.get(0).path("landmark").asText();
        }
        return null;
    }

    /** 通用物体识别（降级） */
    private String recognizeGeneral(byte[] imageBytes) throws Exception {
        String token = getAccessToken();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String body = "image=" + URLEncoder.encode(base64, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=" + token))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());

        if (root.has("error_code") && root.get("error_code").asInt() != 0) {
            log.error("通用识别错误: {} {}", root.get("error_code"), root.get("error_msg"));
            return null;
        }

        JsonNode result = root.path("result");
        if (result.isArray() && result.size() > 0) {
            // 取最高分的识别结果
            return result.get(0).path("keyword").asText();
        }
        return null;
    }

    /** 获取/刷新百度 access_token（有效期约30天，缓存自动续期） */
    private String getAccessToken() throws Exception {
        if (Instant.now().isBefore(tokenExpiry) && accessToken != null) {
            return accessToken;
        }

        String url = "https://aip.baidubce.com/oauth/2.0/token"
                + "?grant_type=client_credentials"
                + "&client_id=" + apiKey
                + "&client_secret=" + secretKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());

        if (root.has("error")) {
            throw new RuntimeException("百度 token 获取失败: " + root.path("error_description").asText());
        }

        accessToken = root.get("access_token").asText();
        int expiresIn = root.get("expires_in").asInt();
        // 提前 1 天刷新
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 86400);
        log.info("百度 access_token 已刷新，有效期至 {}", tokenExpiry);
        return accessToken;
    }
}
