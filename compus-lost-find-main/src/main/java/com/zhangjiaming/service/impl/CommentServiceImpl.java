package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.entity.Comment;
import com.zhangjiaming.entity.Item;
import com.zhangjiaming.entity.User;
import com.zhangjiaming.mapper.CommentMapper;
import com.zhangjiaming.mapper.ItemMapper;
import com.zhangjiaming.mapper.UserMapper;
import com.zhangjiaming.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 查询物品的所有评论（按创建时间升序）
     *
     * @param itemId 物品ID
     * @return 评论列表
     */
    @Override
    public List<Comment> findByItemId(Long itemId) {
        return commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getItemId, itemId)
                        .orderByAsc(Comment::getCreateTime));
    }

    /**
     * 发表评论：填充评论者信息后写入，并更新物品评论数
     *
     * @param comment 评论信息
     * @return 创建后的评论
     */
    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        if (comment.getItemId() == null) {
            throw new RuntimeException(ErrorContext.PARAM_ERROR);
        }
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new RuntimeException(ErrorContext.COMMENT_CONTENT_BLANK);
        }

        // 校验物品是否存在
        Item item = itemMapper.selectById(comment.getItemId());
        if (item == null) {
            throw new RuntimeException(ErrorContext.ITEM_NOT_EXISTS);
        }

        // 填充评论者昵称/头像
        User user = userMapper.selectById(comment.getUserId());
        if (user == null) {
            throw new RuntimeException(ErrorContext.USER_NOT_EXISTS);
        }
        comment.setUserNickname(user.getNickname());
        comment.setUserAvatar(user.getAvatar());

        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getIsRead() == null) {
            comment.setIsRead(0);
        }

        commentMapper.insert(comment);

        // 物品评论数 +1
        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, comment.getItemId())
                .setSql("comment_count = comment_count + 1"));

        return comment;
    }

    /**
     * 删除评论（仅评论作者或管理员），并更新物品评论数
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId, Integer userRole) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException(ErrorContext.COMMENT_NOT_EXISTS);
        }
        // 只有评论作者或管理员可删除
        if (!comment.getUserId().equals(userId) && (userRole == null || userRole != 1)) {
            throw new RuntimeException(ErrorContext.NO_PERMISSION_DELETE_COMMENT);
        }

        commentMapper.deleteById(commentId);

        // 物品评论数 -1（避免减为负数）
        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, comment.getItemId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));

        return true;
    }
}
