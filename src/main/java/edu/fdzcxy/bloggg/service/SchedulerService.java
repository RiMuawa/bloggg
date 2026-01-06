package edu.fdzcxy.bloggg.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.fdzcxy.bloggg.dao.BookmarkDao;
import edu.fdzcxy.bloggg.dao.ContentSummaryDao;
import edu.fdzcxy.bloggg.dao.SubscriptionDao;
import edu.fdzcxy.bloggg.model.ContentSummary;
import edu.fdzcxy.bloggg.model.Subscription;

@Service
public class SchedulerService {
    private final SubscriptionDao subscriptionDao;
    private final BookmarkDao bookmarkDao;
    private final FetchService fetchService;
    private final NotificationService notificationService;
    private final DeepSeekService deepSeekService;
    private final ContentSummaryDao contentSummaryDao;
    private final HtmlDiffService htmlDiffService;

    @Value("${app.notify.subject:博客管管提醒您：}")
    private String notifySubject;

    @Value("${app.notify.body:您订阅的博客[博客地址]更新啦！}")
    private String notifyBody;

    public SchedulerService(
            SubscriptionDao subscriptionDao,
            BookmarkDao bookmarkDao,
            FetchService fetchService,
            NotificationService notificationService,
            DeepSeekService deepSeekService,
            ContentSummaryDao contentSummaryDao,
            HtmlDiffService htmlDiffService
    ) {
        this.subscriptionDao = subscriptionDao;
        this.bookmarkDao = bookmarkDao;
        this.fetchService = fetchService;
        this.notificationService = notificationService;
        this.deepSeekService = deepSeekService;
        this.contentSummaryDao = contentSummaryDao;
        this.htmlDiffService = htmlDiffService;
    }

    public static class CheckNowResponse {
        public boolean success;
        public boolean updated;
        public boolean emailSent;
        public String message;
        public String url;
    }

    /**
     * 手动触发订阅检查
     */
    public CheckNowResponse checkNow(Long subscriptionId) {
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

            // ✅ 更新书签图标（二进制）
            updateBookmarkIconIfNeeded(s.getBookmarkId(), s.getUrl(), res.iconBytes);

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

            // ✅ 内容有更新
            if (!prevHash.equals(res.contentHash)) {
                System.out.println("[更新检测] 检测到内容更新 - 订阅ID: " + s.getId() + ", URL: " + s.getUrl());
                
                ContentSummary previousSummary = contentSummaryDao.findByContentHash(prevHash);
                String previousContent = previousSummary != null ? previousSummary.getRawContent() : null;

                htmlDiffService.printDiff(previousContent, res.content);

                // 提取新增的内容
                String addedContent = htmlDiffService.extractAddedContent(previousContent, res.content);
                
                if (addedContent != null && !addedContent.isBlank()) {
                    System.out.println("[更新检测] 提取到新增内容，长度: " + addedContent.length() + " 字符");
                    System.out.println("[更新检测] 新增内容预览: " + 
                        (addedContent.length() > 200 ? addedContent.substring(0, 200) + "..." : addedContent));
                } else {
                    System.out.println("[更新检测] 未提取到新增内容，将使用整个内容进行总结");
                }
                
                // 使用新增的内容进行总结，如果没有新增内容则使用整个内容
                String contentToSummarize = (addedContent != null && !addedContent.isBlank()) 
                        ? addedContent 
                        : res.content;
                
                System.out.println("[DeepSeek] 开始调用 DeepSeek API 进行内容总结...");
                System.out.println("[DeepSeek] 待总结内容长度: " + contentToSummarize.length() + " 字符");
                
                String summary = (contentToSummarize != null && !contentToSummarize.isBlank())
                        ? deepSeekService.summarize(contentToSummarize)
                        : null;
                
                if (summary != null && !summary.isBlank()) {
                    System.out.println("[DeepSeek] 总结完成，总结长度: " + summary.length() + " 字符");
                    System.out.println("[DeepSeek] 总结内容: " + summary);
                } else {
                    System.out.println("[DeepSeek] 总结失败或返回为空");
                }

                ContentSummary contentSummary = new ContentSummary();
                contentSummary.setSubscriptionId(s.getId());
                contentSummary.setContentHash(res.contentHash);
                contentSummary.setSummary(summary);
                contentSummary.setRawContent(res.content);
                contentSummary.setCreatedAt(LocalDateTime.now());
                contentSummaryDao.insert(contentSummary);

                String subject = notifySubject;
                String body = notifyBody.replace("[博客地址]", s.getUrl());
                System.out.println("[邮件通知] 准备发送更新通知邮件到: " + s.getNotifyEmail());
                notificationService.sendUpdateEmail(s.getNotifyEmail(), subject, body, addedContent, summary);
                System.out.println("[邮件通知] 邮件发送完成");

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

    /**
     * 计划任务：周期性检查更新
     */
    @Scheduled(fixedDelayString = "${app.scheduler.fixedDelayMs:600000}")
    public void periodicCheck() {
        List<Subscription> subs = subscriptionDao.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Subscription s : subs) {
            try {
                boolean due = (s.getLastCheckedAt() == null) ||
                        Duration.between(s.getLastCheckedAt(), now).toHours() >= s.getPeriodHours();
                if (!due) continue;

                FetchService.FetchResult res = fetchService.fetch(s.getUrl());
                s.setLastCheckedAt(now);

                if (!res.success) {
                    subscriptionDao.update(s);
                    continue;
                }

                // ✅ 更新图标（BLOB）
                updateBookmarkIconIfNeeded(s.getBookmarkId(), s.getUrl(), res.iconBytes);

                String prevHash = s.getLastContentHash();

                if (prevHash == null) {
                    s.setLastContentHash(res.contentHash);
                    subscriptionDao.update(s);
                } else if (!prevHash.equals(res.contentHash)) {
                    System.out.println("[更新检测] 检测到内容更新 - 订阅ID: " + s.getId() + ", URL: " + s.getUrl());
                    
                    ContentSummary previousSummary = contentSummaryDao.findByContentHash(prevHash);
                    String previousContent = previousSummary != null ? previousSummary.getRawContent() : null;

                    htmlDiffService.printDiff(previousContent, res.content);

                    // 提取新增的内容
                    String addedContent = htmlDiffService.extractAddedContent(previousContent, res.content);
                    
                    if (addedContent != null && !addedContent.isBlank()) {
                        System.out.println("[更新检测] 提取到新增内容，长度: " + addedContent.length() + " 字符");
                        System.out.println("[更新检测] 新增内容预览: " + 
                            (addedContent.length() > 200 ? addedContent.substring(0, 200) + "..." : addedContent));
                    } else {
                        System.out.println("[更新检测] 未提取到新增内容，将使用整个内容进行总结");
                    }
                    
                    // 使用新增的内容进行总结，如果没有新增内容则使用整个内容
                    String contentToSummarize = (addedContent != null && !addedContent.isBlank()) 
                            ? addedContent 
                            : res.content;
                    
                    System.out.println("[DeepSeek] 开始调用 DeepSeek API 进行内容总结...");
                    System.out.println("[DeepSeek] 待总结内容长度: " + contentToSummarize.length() + " 字符");
                    
                    String summary = (contentToSummarize != null && !contentToSummarize.isBlank())
                            ? deepSeekService.summarize(contentToSummarize)
                            : null;
                    
                    if (summary != null && !summary.isBlank()) {
                        System.out.println("[DeepSeek] 总结完成，总结长度: " + summary.length() + " 字符");
                        System.out.println("[DeepSeek] 总结内容: " + summary);
                    } else {
                        System.out.println("[DeepSeek] 总结失败或返回为空");
                    }

                    ContentSummary contentSummary = new ContentSummary();
                    contentSummary.setSubscriptionId(s.getId());
                    contentSummary.setContentHash(res.contentHash);
                    contentSummary.setSummary(summary);
                    contentSummary.setRawContent(res.content);
                    contentSummary.setCreatedAt(LocalDateTime.now());
                    contentSummaryDao.insert(contentSummary);

                    String subject = notifySubject;
                    String body = notifyBody.replace("[博客地址]", s.getUrl());
                    System.out.println("[邮件通知] 准备发送更新通知邮件到: " + s.getNotifyEmail());
                    notificationService.sendUpdateEmail(s.getNotifyEmail(), subject, body, addedContent, summary);
                    System.out.println("[邮件通知] 邮件发送完成");

                    s.setLastContentHash(res.contentHash);
                    subscriptionDao.update(s);

                    if (s.getBookmarkId() != null) {
                        bookmarkDao.setHasUpdate(s.getBookmarkId(), true);
                    }

                } else {
                    subscriptionDao.update(s);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ✅ 图标更新逻辑：基于 byte[]
     */
    private void updateBookmarkIconIfNeeded(Long bookmarkId, String url, byte[] iconBytes) {
        if (iconBytes == null || iconBytes.length == 0) {
            return;
        }

        if (bookmarkId != null) {
            System.out.println("更新书签图标 -> bookmarkId=" + bookmarkId + ", bytes=" + iconBytes.length);
            bookmarkDao.updateIcon(bookmarkId, iconBytes);
            return;
        }

        if (url == null || url.isBlank()) {
            return;
        }

        int affected = bookmarkDao.updateIconByUrl(url, iconBytes);
        if (affected > 0) {
            System.out.println("更新书签图标(按URL) -> url=" + url + ", bytes=" + iconBytes.length + ", rows=" + affected);
        }
    }
}
