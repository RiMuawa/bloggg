package edu.fdzcxy.bloggg.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription {
    private Long id;

    @JsonProperty("bookmark_id")
    private Long bookmarkId;

    private String url;

    @JsonProperty("notify_email")
    private String notifyEmail;

    @JsonProperty("period_hours")
    private int periodHours;

    private boolean enabled;

    @JsonProperty("last_checked_at")
    private LocalDateTime lastCheckedAt;

    @JsonProperty("last_content_hash")
    private String lastContentHash;
    // getters/setters
}
