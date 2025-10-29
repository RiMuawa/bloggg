package edu.fdzcxy.bloggg.service;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.fdzcxy.bloggg.dao.BookmarkDao;
import edu.fdzcxy.bloggg.dao.SubscriptionDao;
import edu.fdzcxy.bloggg.model.Subscription;

@Service
public class SchedulerService {
    private final SubscriptionDao subscriptionDao;
    private final BookmarkDao bookmarkDao;
    private final FetchService fetchService;
    private final NotificationService notificationService;

    @Value("${app.notify.subject:博客管管提醒您：}")
    private String notifySubject;

    @Value("${app.notify.body:您订阅的博客[博客地址]  更新啦！}")
    private String notifyBody;

    public SchedulerService(SubscriptionDao subscriptionDao, BookmarkDao bookmarkDao, FetchService fetchService, NotificationService notificationService) {
        this.subscriptionDao = subscriptionDao;
        this.bookmarkDao = bookmarkDao;
        this.fetchService = fetchService;
        this.notificationService = notificationService;
    }

    public static class CheckNowResponse {
        public boolean success;
        public boolean updated;
        public boolean emailSent;
        public String message;
        public String url;
    }

    public CheckNowResponse checkNow(Long subscriptionId){
        CheckNowResponse resp = new CheckNowResponse();
        try {
            Subscription s = subscriptionDao.findById(subscriptionId);
            if (s == null || !s.isEnabled()) {
                resp.success = false;
                resp.updated = false;
                resp.emailSent = false;
                resp.message = "订阅不存在或未启用";
                return resp;
            }
            resp.url = s.getUrl();

            LocalDateTime now = LocalDateTime.now();
            FetchService.FetchResult res = fetchService.fetch(s.getUrl());
            s.setLastCheckedAt(now);
            if (!res.success) {
                subscriptionDao.update(s);
                resp.success = false;
                resp.updated = false;
                resp.emailSent = false;
                resp.message = "抓取失败";
                return resp;
            }

            String prevHash = s.getLastContentHash();
            if (prevHash == null) {
                s.setLastContentHash(res.contentHash);
                subscriptionDao.update(s);
                resp.success = true;
                resp.updated = false;
                resp.emailSent = false;
                resp.message = "首次抓取，已记录内容指纹";
                return resp;
            }

            if (!prevHash.equals(res.contentHash)) {
                String subject = notifySubject;
                String body = notifyBody.replace("[博客地址]", s.getUrl());
                notificationService.sendUpdateEmail(s.getNotifyEmail(), subject, body);
                System.out.println("已发送更新邮件 -> 收件人: " + s.getNotifyEmail() + ", 标题: " + subject + ", 地址: " + s.getUrl());

                s.setLastContentHash(res.contentHash);
                subscriptionDao.update(s);
                if (s.getBookmarkId() != null) {
                    bookmarkDao.setHasUpdate(s.getBookmarkId(), true);
                }
                resp.success = true;
                resp.updated = true;
                resp.emailSent = true;
                resp.message = "发现更新并已发送邮件";
                return resp;
            } else {
                subscriptionDao.update(s);
                resp.success = true;
                resp.updated = false;
                resp.emailSent = false;
                resp.message = "无变化";
                return resp;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.success = false;
            resp.updated = false;
            resp.emailSent = false;
            resp.message = "内部错误";
            return resp;
        }
    }

    // 每 10 分钟检查一次是否有到期的 subscription（避免大量定时器），也可改为更短/更长
    @Scheduled(fixedDelayString = "${app.scheduler.fixedDelayMs:600000}")
    public void periodicCheck() {
        List<Subscription> subs = subscriptionDao.findAll(); // 仅 enabled
        LocalDateTime now = LocalDateTime.now();
        for (Subscription s : subs) {
            try {
                // 如果从未检查过，或已过期（lastChecked + periodHours <= now）
                boolean due = (s.getLastCheckedAt() == null) ||
                        Duration.between(s.getLastCheckedAt(), now).toHours() >= s.getPeriodHours();
                if (!due) continue;

                // 抓取
                FetchService.FetchResult res = fetchService.fetch(s.getUrl());
                s.setLastCheckedAt(now);
                if (!res.success) {
                    // 仅更新 lastChecked 记录
                    subscriptionDao.update(s);
                    continue;
                }
                // 比较 Hash
                String prevHash = s.getLastContentHash();
                if (prevHash == null) {
                    // 初次抓取：保存 hash
                    s.setLastContentHash(res.contentHash);
                    subscriptionDao.update(s);
                } else if (!prevHash.equals(res.contentHash)) {
                    // 发现更新：发送邮件、更新 subscription.lastContentHash、更新 bookmark.has_update
                    String subject = notifySubject;
                    String body = notifyBody.replace("[博客地址]", s.getUrl());
                    notificationService.sendUpdateEmail(s.getNotifyEmail(), subject, body);
                    System.out.println("已发送更新邮件 -> 收件人: " + s.getNotifyEmail() + ", 标题: " + subject + ", 地址: " + s.getUrl());

                    s.setLastContentHash(res.contentHash);
                    subscriptionDao.update(s);

                    // 如果有关联的 bookmark，设置 has_update=true（导航处用于提示）
                    if (s.getBookmarkId() != null) {
                        bookmarkDao.setHasUpdate(s.getBookmarkId(), true);
                    } else {
                        // 也可以根据 url 找 bookmark（可选实现）
                    }
                } else {
                    // 无变化：仅更新时间戳
                    subscriptionDao.update(s);
                }
            } catch (Exception e){
                // 日志记录（这里省略日志框）
                e.printStackTrace();
            }   
        }
    }
}
