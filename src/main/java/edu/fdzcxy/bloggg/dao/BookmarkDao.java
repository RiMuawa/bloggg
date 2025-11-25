package edu.fdzcxy.bloggg.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.fdzcxy.bloggg.model.Bookmark;

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

        // ✅ 读取 BLOB 字段
        b.setIcon(rs.getBytes("icon"));

        b.setHasUpdate(rs.getBoolean("has_update"));
        b.setLastFetchedAt(rs.getTimestamp("last_fetched_at") == null ? null :
                rs.getTimestamp("last_fetched_at").toLocalDateTime());
        b.setLastContentHash(rs.getString("last_content_hash"));
        return b;
    }

    public List<Bookmark> findAll() {
        return jdbc.query("SELECT * FROM bookmarks ORDER BY created_at DESC", this::mapRow);
    }

    public Bookmark findById(Long id) {
        return jdbc.queryForObject(
                "SELECT * FROM bookmarks WHERE id = ?",
                this::mapRow,
                id
        );
    }

    public Long insert(Bookmark b) {
        jdbc.update("""
            INSERT INTO bookmarks(title,url,description,icon,has_update,last_fetched_at,last_content_hash)
            VALUES(?,?,?,?,?,?,?)
            """,
                b.getTitle(),
                b.getUrl(),
                b.getDescription(),
                b.getIcon(),          // ✅ 改为 icon BLOB
                b.isHasUpdate(),
                b.getLastFetchedAt(),
                b.getLastContentHash()
        );
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Bookmark b) {
        jdbc.update("""
            UPDATE bookmarks SET title=?,url=?,description=?,icon=?,has_update=?,last_fetched_at=?,last_content_hash=? WHERE id=?
            """,
                b.getTitle(),
                b.getUrl(),
                b.getDescription(),
                b.getIcon(),          // ✅ 更新 icon BLOB
                b.isHasUpdate(),
                b.getLastFetchedAt(),
                b.getLastContentHash(),
                b.getId()
        );
    }

    public void setHasUpdate(Long id, boolean hasUpdate) {
        jdbc.update("UPDATE bookmarks SET has_update=? WHERE id=?", hasUpdate, id);
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM bookmarks WHERE id=?", id);
    }

    /**
     * ✅ 更新图标 BLOB
     */
    public void updateIcon(Long id, byte[] icon) {
        jdbc.update("UPDATE bookmarks SET icon=? WHERE id=?", icon, id);
    }

    /**
     * ✅ 根据 URL 更新所有匹配书签的图标 BLOB
     */
    public int updateIconByUrl(String url, byte[] icon) {
        return jdbc.update("UPDATE bookmarks SET icon=? WHERE url=?", icon, url);
    }

    /**
     * ✅ 读取图标 BLOB
     */
    public byte[] getIcon(Long id) {
        try {
            return jdbc.queryForObject(
                    "SELECT icon FROM bookmarks WHERE id=?",
                    byte[].class,
                    id
            );
        } catch (Exception e) {
            return null;
        }
    }
}
