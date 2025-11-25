package edu.fdzcxy.bloggg.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class FetchService {
    private final int timeoutMs;

    public FetchService(org.springframework.core.env.Environment env){
        this.timeoutMs = Integer.parseInt(env.getProperty("app.fetch.timeoutMs","10000"));
    }

    /**
     * 抓取页面，并返回内容 + 图标二进制数据
     */
    public FetchResult fetch(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Bloggg/1.0")
                    .timeout(timeoutMs)
                    .get();

            String bodyText = doc.body() != null ? doc.body().text() : doc.text();
            String normalized = bodyText.replaceAll("\\s+", " ").trim();
            String hash = sha256(normalized);

            // ✅ 1. 提取 icon 地址
            String iconUrl = extractIconUrl(doc, url);

            // ✅ 2. 下载 icon 为二进制
            byte[] iconBytes = downloadIcon(iconUrl);

            return new FetchResult(
                    true,
                    bodyText,
                    hash,
                    iconBytes,
                    LocalDateTime.now(),
                    null
            );

        } catch (Exception e) {
            return new FetchResult(false, null, null, null, null, e.getMessage());
        }
    }

    /**
     * 查找 rel 属性包含 icon 的 <link> 标签
     */
    private String extractIconUrl(Document doc, String baseUrl) {
        try {
            Element iconLink = doc.selectFirst("link[rel*=icon]");
            if (iconLink != null) {
                String href = iconLink.attr("href");
                if (href != null && !href.isEmpty()) {
                    return resolveUrl(baseUrl, href);
                }
            }

            // fallback: /favicon.ico
            URL u = new URL(baseUrl);
            String base = u.getProtocol() + "://" + u.getHost();
            if (u.getPort() != -1) {
                base += ":" + u.getPort();
            }
            return base + "/favicon.ico";

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 下载 icon，返回 byte[]
     */
    private byte[] downloadIcon(String iconUrl) {
        if (iconUrl == null) return null;
        try {
            URL url = new URL(iconUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("User-Agent", "Bloggg/1.0");

            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 相对路径转换为绝对路径
     */
    private String resolveUrl(String baseUrl, String relativeUrl) {
        try {
            URL base = new URL(baseUrl);
            URL resolved = new URL(base, relativeUrl);
            return resolved.toString();
        } catch (Exception ignored) {
            return relativeUrl;
        }
    }

    private String sha256(String s) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] h = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * ✅ FetchResult 只包含 iconBytes，不再包含 iconUrl
     */
    public static class FetchResult {
        public final boolean success;
        public final String content;
        public final String contentHash;
        public final byte[] iconBytes;
        public final LocalDateTime fetchedAt;
        public final String error;

        public FetchResult(boolean success, String content, String contentHash,
                           byte[] iconBytes, LocalDateTime fetchedAt, String error) {
            this.success = success;
            this.content = content;
            this.contentHash = contentHash;
            this.iconBytes = iconBytes;
            this.fetchedAt = fetchedAt;
            this.error = error;
        }
    }
}
