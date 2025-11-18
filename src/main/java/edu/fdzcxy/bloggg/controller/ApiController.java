package edu.fdzcxy.bloggg.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
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

@RestController
@RequestMapping("/api")
public class ApiController {
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
    public FetchService.FetchResult fetchNow(@RequestParam String url){
        return fetchService.fetch(url);
    }

    @PostMapping("/subscriptions/{id}/check-now")
    public SchedulerService.CheckNowResponse checkSubscriptionNow(@PathVariable Long id){
        return schedulerService.checkNow(id);
    }
}
