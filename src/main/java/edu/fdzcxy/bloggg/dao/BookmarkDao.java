package edu.fdzcxy.bloggg.dao;

import edu.fdzcxy.bloggg.model.Bookmark;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class BookmarkDao {
    private final JdbcTemplate jdbc;

    public BookmarkDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private Bookmark mapRow(ResultSet rs, int rowNum) throws SQLException {
        Bookmark b = new Bookmark();
        b.setId(rs.getLong("id"));
        b.setTitle(rs.getString("title"));
        b.setUrl(rs.getString("url"));
        b.setDescription(rs.getString("description"));
        b.setHasUpdate(rs.getBoolean("has_update"));
        b.setLastFetchedAt(rs.getTimestamp("last_fetched_at") == null ? null : rs.getTimestamp("last_fetched_at").toLocalDateTime());
        b.setLastContentHash(rs.getString("last_content_hash"));
        return b;
    }

    public List<Bookmark> findAll() {
        return jdbc.query("SELECT * FROM bookmarks ORDER BY created_at DESC", this::mapRow);
    }

    public Bookmark findById(Long id) {
        return jdbc.queryForObject("SELECT * FROM bookmarks WHERE id = ?", new Object[]{id}, this::mapRow);
    }

    public Long insert(Bookmark b) {
        jdbc.update("INSERT INTO bookmarks(title,url,description,has_update,last_fetched_at,last_content_hash) VALUES(?,?,?,?,?,?)",
                b.getTitle(), b.getUrl(), b.getDescription(), b.isHasUpdate(), b.getLastFetchedAt(), b.getLastContentHash());
        // 返回自增 id（简单方式）
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Bookmark b) {
        jdbc.update("UPDATE bookmarks SET title=?,url=?,description=?,has_update=?,last_fetched_at=?,last_content_hash=? WHERE id=?",
                b.getTitle(), b.getUrl(), b.getDescription(), b.isHasUpdate(), b.getLastFetchedAt(), b.getLastContentHash(), b.getId());
    }

    public void setHasUpdate(Long id, boolean hasUpdate) {
        jdbc.update("UPDATE bookmarks SET has_update=? WHERE id=?", hasUpdate, id);
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM bookmarks WHERE id=?", id);
    }
}
