package com.zhangjiaming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("announcements")
@Schema(description = "公告实体")
public class Announcement {

    @Schema(description = "公告ID")
    @TableId(type = IdType.AUTO)
    private Long announcementId;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "发布者ID")
    private Long publisherId;

    @Schema(description = "发布者名称")
    private String publisherName;

    @Schema(description = "是否置顶：0-不置顶,1-置顶")
    private Integer isTop;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
