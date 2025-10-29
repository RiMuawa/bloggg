package edu.fdzcxy.bloggg.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionRequest {
    private String url;

    @JsonProperty("notify_email")
    private String notifyEmail;

    @JsonProperty("period_hours")
    private Integer periodHours;

    // getter 和 setter 方法
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(String notifyEmail) { this.notifyEmail = notifyEmail; }

    public Integer getPeriodHours() { return periodHours; }
    public void setPeriodHours(Integer periodHours) { this.periodHours = periodHours; }
}