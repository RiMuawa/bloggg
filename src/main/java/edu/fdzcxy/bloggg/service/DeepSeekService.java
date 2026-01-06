package edu.fdzcxy.bloggg.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Service
public class DeepSeekService {
    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;
    private final String prompt;

    public DeepSeekService(
            @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}") String apiUrl,
            @Value("${deepseek.api.key:}") String apiKey,
            @Value("${deepseek.prompt.file:classpath:deepseek-prompt.txt}") String promptFile,
            ResourceLoader resourceLoader
    ) {
        this.restTemplate = new RestTemplate();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.prompt = loadPrompt(promptFile, resourceLoader);
    }

    /**
     * 从文件加载 prompt
     */
    private String loadPrompt(String promptFile, ResourceLoader resourceLoader) {
        try {
            Resource resource = resourceLoader.getResource(promptFile);
            if (!resource.exists()) {
                System.err.println("警告: Prompt 文件不存在: " + promptFile + "，使用默认 prompt");
                return "请总结以下网页内容的新增内容：";
            }
            try (InputStream inputStream = resource.getInputStream()) {
                String promptText = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                return promptText.trim();
            }
        } catch (IOException e) {
            System.err.println("读取 Prompt 文件失败: " + promptFile + "，使用默认 prompt");
            e.printStackTrace();
            return "请总结以下网页内容的新增内容：";
        }
    }

    /**
     * 调用 DeepSeek API 总结内容
     * @param content 要总结的内容
     * @return 总结结果，如果失败返回 null
     */
    public String summarize(String content) {
        System.out.println("[DeepSeek] ========== 开始 DeepSeek API 调用 ==========");
        
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[DeepSeek] 警告: DeepSeek API Key 未配置，跳过总结");
            return null;
        }

        if (content == null || content.isBlank()) {
            System.out.println("[DeepSeek] 警告: 内容为空，跳过总结");
            return null;
        }

        System.out.println("[DeepSeek] API URL: " + apiUrl);
        System.out.println("[DeepSeek] 请求内容长度: " + content.length() + " 字符");
        System.out.println("[DeepSeek] 请求内容预览: " + 
            (content.length() > 300 ? content.substring(0, 300) + "..." : content));

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", prompt);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", content);
            
            requestBody.put("messages", List.of(systemMessage, userMessage));
            requestBody.put("temperature", 0.7);

            System.out.println("[DeepSeek] 请求体构建完成，模型: deepseek-chat");

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            System.out.println("[DeepSeek] 正在发送请求到 DeepSeek API...");
            long startTime = System.currentTimeMillis();

            // 发送请求
            ResponseEntity<DeepSeekResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    DeepSeekResponse.class
            );

            long endTime = System.currentTimeMillis();
            System.out.println("[DeepSeek] API 响应接收完成，耗时: " + (endTime - startTime) + " ms");
            System.out.println("[DeepSeek] HTTP 状态码: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                DeepSeekResponse body = response.getBody();
                if (body != null && body.choices != null && !body.choices.isEmpty()) {
                    Choice firstChoice = body.choices.get(0);
                    if (firstChoice != null && firstChoice.message != null && firstChoice.message.content != null) {
                        String result = firstChoice.message.content.trim();
                        System.out.println("[DeepSeek] API 调用成功，返回总结长度: " + result.length() + " 字符");
                        System.out.println("[DeepSeek] ========== DeepSeek API 调用完成 ==========");
                        return result;
                    }
                }
            }

            System.out.println("[DeepSeek] API 响应异常: " + response.getStatusCode());
            System.out.println("[DeepSeek] ========== DeepSeek API 调用失败 ==========");
            return null;
        } catch (RestClientException e) {
            System.err.println("[DeepSeek] 调用 DeepSeek API 失败: " + e.getMessage());
            e.printStackTrace();
            System.out.println("[DeepSeek] ========== DeepSeek API 调用异常 ==========");
            return null;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeepSeekResponse {
        private List<Choice> choices;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private Message message;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Message {
        private String role;
        private String content;
    }
}

