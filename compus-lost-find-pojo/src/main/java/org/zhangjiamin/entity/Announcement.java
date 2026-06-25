package org.zhangjiamin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Announcement {
    private Long announcementId;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private Integer isTop;          // 0-不置顶，1-置顶
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
