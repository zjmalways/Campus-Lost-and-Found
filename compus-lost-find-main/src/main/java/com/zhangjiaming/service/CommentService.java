package com.zhangjiaming.service;

import com.zhangjiaming.entity.Comment;

import java.util.List;

public interface CommentService {

    /**
     * 查询物品的所有评论
     *
     * @param itemId 物品ID
     * @return 评论列表
     */
    List<Comment> findByItemId(Long itemId);

    /**
     * 发表评论
     *
     * @param comment 评论信息
     * @return 创建后的评论
     */
    Comment createComment(Comment comment);

    /**
     * 删除评论（仅评论作者或管理员）
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 是否删除成功
     */
    boolean deleteComment(Long commentId, Long userId, Integer userRole);
}
