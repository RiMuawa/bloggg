package edu.fdzcxy.bloggg.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentSummary {
    private Long id;

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("content_hash")
    private String contentHash;

    private String summary;

    @JsonProperty("raw_content")
    private String rawContent;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

