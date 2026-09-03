package com.zhangjiaming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("items")
@Schema(description = "物品实体")
public class Item {

    @Schema(description = "物品ID")
    @TableId(type = IdType.AUTO)
    private Long itemId;

    @Schema(description = "物品类型：1-证件,2-钥匙,3-电子设备,4-衣物,5-钱包,6-其他")
    private Integer itemType;

    @Schema(description = "发布类型：0-丢失,1-捡到")
    private Integer publishType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "详细描述")
    private String description;

    @Schema(description = "物品特征")
    private String features;

    @Schema(description = "图片URL列表（逗号分隔）")
    private String images;

    @Schema(description = "丢失/捡到地点")
    private String location;

    @Schema(description = "事件发生时间")
    private LocalDateTime eventTime;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "物品存放地点")
    private String storageLocation;

    @Schema(description = "状态：0-未找回/未归还,1-已找回/已归还")
    private Integer status;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
