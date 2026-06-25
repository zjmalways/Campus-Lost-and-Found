package org.zhangjiamin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long commentId;
    private Long itemId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String content;
    private Long parentId;          // 0-顶级评论
    private LocalDateTime createTime;
    private Integer isRead;         // 0-未读，1-已读
}
