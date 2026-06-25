package org.zhangjiamin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhangjiamin.entity.Comment;
import org.zhangjiamin.mapper.CommentMapper;
import org.zhangjiamin.mapper.ItemMapper;
import org.zhangjiamin.service.CommentService;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public List<Comment> findByItemId(Long itemId) {
        return commentMapper.findByItemId(itemId);
    }

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getIsRead() == null) {
            comment.setIsRead(0);
        }
        commentMapper.insert(comment);
        // 更新物品的评论数
        itemMapper.incrementCommentCount(comment.getItemId());
        return comment;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId, Integer userRole) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只有评论作者或管理员可以删除
        if (!comment.getUserId().equals(userId) && userRole != 1) {
            throw new RuntimeException("无权删除此评论");
        }

        commentMapper.deleteById(commentId);
        return true;
    }
}
