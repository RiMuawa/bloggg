package edu.fdzcxy.bloggg.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.fdzcxy.bloggg.model.Subscription;

@Repository
public class SubscriptionDao {
    private final JdbcTemplate jdbc;
    public SubscriptionDao(JdbcTemplate jdbc){ this.jdbc = jdbc; }

    // ✅ 改为查询 b.icon（二进制）
    private static final String BASE_SELECT = """
            SELECT s.*, b.icon AS bookmark_icon
            FROM subscriptions s
            LEFT JOIN bookmarks b ON s.bookmark_id = b.id
            """;

    private Subscription mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        Subscription s = new Subscription();
        s.setId(rs.getLong("id"));
        s.setBookmarkId(rs.getObject("bookmark_id") == null ? null : rs.getLong("bookmark_id"));
        s.setUrl(rs.getString("url"));
        s.setNotifyEmail(rs.getString("notify_email"));
        s.setPeriodHours(rs.getInt("period_hours"));
        s.setEnabled(rs.getBoolean("enabled"));
        s.setLastCheckedAt(rs.getTimestamp("last_checked_at") == null ? null : rs.getTimestamp("last_checked_at").toLocalDateTime());
        s.setLastContentHash(rs.getString("last_content_hash"));

        // ✅ 从数据库读取图标二进制数据
        byte[] iconBytes = rs.getBytes("bookmark_icon");
        s.setIcon(iconBytes);

        return s;
    }

    public List<Subscription> findAll(){
        return jdbc.query(BASE_SELECT + " WHERE s.enabled=TRUE", this::mapRow);
    }

    public List<Subscription> findDue(long cutoffEpochMillis){
        return findAll();
    }

    public Long insert(Subscription s){
        jdbc.update("""
            INSERT INTO subscriptions(bookmark_id,url,notify_email,period_hours,enabled,last_checked_at,last_content_hash)
            VALUES(?,?,?,?,?,?,?)
            """,
                s.getBookmarkId(), s.getUrl(), s.getNotifyEmail(), s.getPeriodHours(), s.isEnabled(), s.getLastCheckedAt(), s.getLastContentHash()
        );
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Subscription s){
        jdbc.update("""
            UPDATE subscriptions SET bookmark_id=?,url=?,notify_email=?,period_hours=?,enabled=?,last_checked_at=?,last_content_hash=? WHERE id=?
            """,
                s.getBookmarkId(), s.getUrl(), s.getNotifyEmail(), s.getPeriodHours(), s.isEnabled(), s.getLastCheckedAt(), s.getLastContentHash(), s.getId()
        );
    }

    public void delete(Long id){
        jdbc.update("DELETE FROM subscriptions WHERE id=?", id);
    }

    public Subscription findById(Long id){
        return jdbc.queryForObject(BASE_SELECT + " WHERE s.id=?", this::mapRow, id);
    }
}
