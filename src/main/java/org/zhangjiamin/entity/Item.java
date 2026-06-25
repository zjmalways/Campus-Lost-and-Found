package org.zhangjiamin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Item {
    private Long itemId;
    private Integer itemType;       // 1-证件, 2-钥匙, 3-电子设备, 4-衣物, 5-钱包, 6-其他
    private Integer publishType;    // 0-丢失, 1-捡到
    private String title;
    private String description;
    private String features;
    private String images;
    private String location;
    private LocalDateTime eventTime;
    private Long publisherId;
    private String publisherName;
    private String contact;
    private String storageLocation;
    private Integer status;         // 0-未找回/未归还, 1-已找回/已归还
    private Integer viewCount;
    private Integer collectCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
