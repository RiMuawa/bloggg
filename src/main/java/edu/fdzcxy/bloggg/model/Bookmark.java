package edu.fdzcxy.bloggg.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Bookmark {
    private Long id;
    private String title;
    private String url;
    private String description;

    // ✅ 改为图标二进制数据（对应 MEDIUMBLOB）
    private byte[] icon;

    private boolean hasUpdate;
    private LocalDateTime lastFetchedAt;
    private String lastContentHash;
}
