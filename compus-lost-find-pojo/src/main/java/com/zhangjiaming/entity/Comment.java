package com.zhangjiaming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comments")
@Schema(description = "评论实体")
public class Comment {

    @Schema(description = "评论ID")
    @TableId(type = IdType.AUTO)
    private Long commentId;

    @Schema(description = "物品ID")
    private Long itemId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String userNickname;

    @Schema(description = "评论用户头像")
    private String userAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID：0-顶级评论")
    private Long parentId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否已读：0-未读,1-已读")
    private Integer isRead;
}
