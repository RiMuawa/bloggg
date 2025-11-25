package edu.fdzcxy.bloggg.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.fdzcxy.bloggg.DTO.SubscriptionRequest;
import edu.fdzcxy.bloggg.dao.BookmarkDao;
import edu.fdzcxy.bloggg.dao.SubscriptionDao;
import edu.fdzcxy.bloggg.model.Bookmark;
import edu.fdzcxy.bloggg.model.Subscription;
import edu.fdzcxy.bloggg.service.FetchService;
import edu.fdzcxy.bloggg.service.SchedulerService;
import java.util.Base64;


@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final BookmarkDao bookmarkDao;
    private final SubscriptionDao subscriptionDao;
    private final FetchService fetchService;
    private final SchedulerService schedulerService;

    public ApiController(BookmarkDao bookmarkDao, SubscriptionDao subscriptionDao, FetchService fetchService, SchedulerService schedulerService){
        this.bookmarkDao = bookmarkDao;
        this.subscriptionDao = subscriptionDao;
        this.fetchService = fetchService;
        this.schedulerService = schedulerService;
    }

    // Bookmark endpoints
    @GetMapping("/bookmarks")
    public List<Bookmark> listBookmarks(){ return bookmarkDao.findAll(); }

    @PostMapping("/bookmarks")
    public Long addBookmark(@RequestBody Bookmark b){
        return bookmarkDao.insert(b);
    }

    @PutMapping("/bookmarks/{id}")
    public void updateBookmark(@PathVariable Long id, @RequestBody Bookmark b){
        b.setId(id); bookmarkDao.update(b);
    }

    @DeleteMapping("/bookmarks/{id}")
    public void deleteBookmark(@PathVariable Long id){ bookmarkDao.delete(id); }

    @PostMapping("/bookmarks/{id}/open")
    public void clearUpdateFlag(@PathVariable Long id){
        bookmarkDao.setHasUpdate(id, false);
    }

    /**
     * 获取书签图标（Base64）
     */
    @GetMapping("/bookmarks/{id}/icon")
    public IconResponse getIcon(@PathVariable Long id){
        IconResponse resp = new IconResponse();
        Bookmark bookmark = bookmarkDao.findById(id);

        if (bookmark == null) {
            resp.success = false;
            resp.message = "书签不存在";
            return resp;
        }

        byte[] iconBytes = bookmark.getIcon();
        if (iconBytes == null || iconBytes.length == 0) {
            resp.success = false;
            resp.message = "图标未设置";
            return resp;
        }

        resp.success = true;
        resp.iconBase64 = Base64.getEncoder().encodeToString(iconBytes);
        resp.message = "成功";
        return resp;
    }

    /**
     * 抓取并更新书签图标（BLOB）
     */
    @PostMapping("/bookmarks/{id}/fetch-icon")
    public ResponseEntity<IconResponse> fetchIcon(@PathVariable Long id){
        IconResponse resp = new IconResponse();
        try {
            Bookmark bookmark = bookmarkDao.findById(id);
            if (bookmark == null) {
                resp.success = false;
                resp.message = "书签不存在";
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }

            if (bookmark.getUrl() == null || bookmark.getUrl().isBlank()) {
                resp.success = false;
                resp.message = "书签 URL 为空";
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }

            FetchService.FetchResult result = fetchService.fetch(bookmark.getUrl());
            if (!result.success) {
                resp.success = false;
                resp.message = "抓取失败: " + result.error;
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
            }

            // ✅ 更新数据库 icon BLOB
            bookmarkDao.updateIcon(id, result.iconBytes);

            resp.success = true;
            resp.iconBase64 = result.iconBytes != null
                    ? Base64.getEncoder().encodeToString(result.iconBytes)
                    : null;
            resp.message = "图标更新成功";

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.success = false;
            resp.message = "内部错误: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }


    public static class IconResponse {
        public boolean success;
        public String iconBase64;
        public String message;
    }


    // Subscription endpoints
    @GetMapping("/subscriptions")
    public List<Subscription> listSubscriptions(){ return subscriptionDao.findAll(); }

    @PostMapping("/subscriptions")
    public Long addSubscription(@RequestBody SubscriptionRequest req) {
        Subscription s = new Subscription();
        s.setUrl(req.getUrl());
        s.setNotifyEmail(req.getNotifyEmail());
        Integer reqPeriod = req.getPeriodHours();
        s.setPeriodHours(reqPeriod == null ? 24 : reqPeriod);
        s.setEnabled(true);

        // 简单校验，避免写入空值
        if (s.getUrl() == null || s.getUrl().isBlank()) {
            throw new IllegalArgumentException("url 不能为空");
        }
        if (s.getNotifyEmail() == null || s.getNotifyEmail().isBlank()) {
            throw new IllegalArgumentException("notify_email 不能为空");
        }

        return subscriptionDao.insert(s);
    }

    @PutMapping("/subscriptions/{id}")
    public void updateSubscription(@PathVariable Long id, @RequestBody Subscription s){
        s.setId(id); subscriptionDao.update(s);
    }

    @DeleteMapping("/subscriptions/{id}")
    public void deleteSubscription(@PathVariable Long id){ subscriptionDao.delete(id); }

    // 测试抓取接口（手动触发）
    @PostMapping("/fetch")
    public ResponseEntity<FetchService.FetchResult> fetchNow(@RequestParam String url){
        logger.info("手动触发抓取, url={}", url);
        FetchService.FetchResult result = fetchService.fetch(url);

        if (!result.success) {
            String errorMsg = result.error != null ? result.error : "未知错误";
            logger.error("抓取失败, url={}, 错误信息: {}", url, errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        logger.info("抓取成功, url={}, contentHash={}, iconBytes={} bytes",
                url, result.contentHash,
                result.iconBytes != null ? result.iconBytes.length : 0);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/subscriptions/{id}/check-now")
    public SchedulerService.CheckNowResponse checkSubscriptionNow(@PathVariable Long id){
        return schedulerService.checkNow(id);
    }
}
