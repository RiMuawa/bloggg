package edu.fdzcxy.bloggg.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Bookmark {
    private Long id;
    private String title;
    private String url;
    private String description;
    private boolean hasUpdate;
    private LocalDateTime lastFetchedAt;
    private String lastContentHash;

}
