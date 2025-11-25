package edu.fdzcxy.bloggg.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.fdzcxy.bloggg.model.ContentSummary;

@Repository
public class ContentSummaryDao {
    private final JdbcTemplate jdbc;

    public ContentSummaryDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private ContentSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        ContentSummary cs = new ContentSummary();
        cs.setId(rs.getLong("id"));
        cs.setSubscriptionId(rs.getObject("subscription_id") == null ? null : rs.getLong("subscription_id"));
        cs.setContentHash(rs.getString("content_hash"));
        cs.setSummary(rs.getString("summary"));
        cs.setRawContent(rs.getString("raw_content"));
        cs.setCreatedAt(rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime());
        return cs;
    }

    public Long insert(ContentSummary cs) {
        jdbc.update("INSERT INTO content_summaries(subscription_id, content_hash, summary, raw_content, created_at) VALUES(?,?,?,?,?)",
                cs.getSubscriptionId(), cs.getContentHash(), cs.getSummary(), cs.getRawContent(), cs.getCreatedAt());
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public List<ContentSummary> findBySubscriptionId(Long subscriptionId) {
        return jdbc.query("SELECT * FROM content_summaries WHERE subscription_id = ? ORDER BY created_at DESC",
                this::mapRow, subscriptionId);
    }

    public ContentSummary findByContentHash(String contentHash) {
        try {
            return jdbc.queryForObject("SELECT * FROM content_summaries WHERE content_hash = ? ORDER BY created_at DESC LIMIT 1",
                    this::mapRow, contentHash);
        } catch (Exception e) {
            return null;
        }
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM content_summaries WHERE id=?", id);
    }
}

