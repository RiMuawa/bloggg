package edu.fdzcxy.bloggg.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class FetchService {
    private final int timeoutMs;

    public FetchService(org.springframework.core.env.Environment env){
        this.timeoutMs = Integer.parseInt(env.getProperty("app.fetch.timeoutMs","10000"));
    }

    /**
     * 抓取页面并返回“主内容”或完整文本（这里选取 body 的 text()）
     */
    public FetchResult fetch(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("BlogMgrBot/1.0")
                    .timeout(timeoutMs)
                    .get();
            String bodyText = doc.body().text();
            String hash = sha256(bodyText);
            return new FetchResult(true, bodyText, hash, LocalDateTime.now(), null);
        } catch (Exception e) {
            return new FetchResult(false, null, null, null, e.getMessage());
        }
    }

    private String sha256(String s) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] h = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static class FetchResult {
        public final boolean success;
        public final String content;
        public final String contentHash;
        public final LocalDateTime fetchedAt;
        public final String error;

        public FetchResult(boolean success, String content, String contentHash, LocalDateTime fetchedAt, String error) {
            this.success = success;
            this.content = content;
            this.contentHash = contentHash;
            this.fetchedAt = fetchedAt;
            this.error = error;
        }
    }
}
