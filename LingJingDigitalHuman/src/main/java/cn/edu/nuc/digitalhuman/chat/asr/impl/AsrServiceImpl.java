package cn.edu.nuc.digitalhuman.chat.asr.impl;

import cn.edu.nuc.digitalhuman.chat.asr.AsrService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class AsrServiceImpl implements AsrService {

    private final String apiKey;
    private final String secretKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String ASR_URL = "https://vop.baidu.com/server_api";

    // access token 缓存
    private String cachedToken = null;
    private Instant tokenExpiry = Instant.EPOCH;

    public AsrServiceImpl(
            @Value("${baidu.api-key}") String apiKey,
            @Value("${baidu.secret-key}") String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String transcribe(MultipartFile audioFile) {
        try {
            return transcribe(audioFile.getBytes(), audioFile.getOriginalFilename());
        } catch (IOException e) {
            log.error("读取音频文件失败", e);
            return null;
        }
    }

    @Override
    public String transcribe(byte[] audioData, String fileName) {
        try {
            String token = getAccessToken();
            if (token == null) {
                log.error("获取百度 access_token 失败");
                return null;
            }

            String base64Audio = Base64.getEncoder().encodeToString(audioData);
            int sampleRate = detectSampleRate(audioData);

            Map<String, Object> body = Map.of(
                    "format", "wav",
                    "rate", sampleRate,
                    "channel", 1,
                    "cuid", "lingjing-digital-human",
                    "token", token,
                    "speech", base64Audio,
                    "len", audioData.length,
                    "dev_pid", 1537
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ASR_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            int errNo = root.path("err_no").asInt(-1);
            if (errNo != 0) {
                log.error("百度 ASR 识别失败: err_no={}, err_msg={}, body={}",
                        errNo, root.path("err_msg").asText(), response.body());
                return null;
            }

            JsonNode result = root.path("result");
            if (result.isArray() && result.size() > 0) {
                String text = result.get(0).asText();
                if (text != null && !text.isBlank()) {
                    text = correctText(text);
                    log.info("百度 ASR 识别成功: text={}", text);
                    return text;
                }
            }

            log.warn("百度 ASR 响应中没有识别文本: {}", response.body());
            return null;

        } catch (Exception e) {
            log.error("百度 ASR API call failed", e);
            return null;
        }
    }

    /**
     * 纠正常见 ASR 同音字错误（景区名称）
     */
    private String correctText(String text) {
        if (text == null) {
            return null;
        }
        // 灵山胜境 / 拈花湾相关
        String corrected = text
                .replace("圣境", "胜境")
                .replace("莲花湾", "拈花湾")
                .replace("年花湾", "拈花湾")
                .replace("粘花湾", "拈花湾");
        return corrected;
    }

    /**
     * 从 WAV 文件头读取采样率
     */
    private int detectSampleRate(byte[] wavData) {
        try {
            if (wavData.length < 44) {
                return 16000;
            }
            // WAV header sample rate is at bytes 24-27 (little-endian)
            int rate = (wavData[24] & 0xFF)
                    | ((wavData[25] & 0xFF) << 8)
                    | ((wavData[26] & 0xFF) << 16)
                    | ((wavData[27] & 0xFF) << 24);
            if (rate == 44100 || rate == 48000 || rate == 8000) {
                return rate;
            }
            return 16000;
        } catch (Exception e) {
            return 16000;
        }
    }

    /**
     * 获取百度 access_token（带缓存，有效期 30 天）
     */
    private String getAccessToken() throws Exception {
        if (cachedToken != null && !cachedToken.isBlank() && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        String params = "grant_type=client_credentials&client_id="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&client_secret="
                + URLEncoder.encode(secretKey, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL + "?" + params))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("获取百度 token 失败: status={}", response.statusCode());
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        String token = root.path("access_token").asText();
        if (token == null || token.isBlank()) {
            log.error("百度 token 响应中没有 access_token: {}", response.body());
            return null;
        }

        int expiresIn = root.path("expires_in").asInt(2592000);
        cachedToken = token;
        tokenExpiry = Instant.now().plusSeconds(expiresIn - 300); // 提前 5 分钟过期
        log.info("获取百度 access_token 成功，有效期 {} 秒", expiresIn);
        return token;
    }
}
