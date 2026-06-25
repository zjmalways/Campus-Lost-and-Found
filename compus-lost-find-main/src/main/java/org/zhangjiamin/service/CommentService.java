package org.zhangjiamin.service;

import org.zhangjiamin.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> findByItemId(Long itemId);
    Comment createComment(Comment comment);
    boolean deleteComment(Long commentId, Long userId, Integer userRole);
}
