package org.zhangjiamin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zhangjiamin.common.Result;
import org.zhangjiamin.entity.Comment;
import org.zhangjiamin.service.CommentService;
import org.zhangjiamin.util.ThreadLocalUtil;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取物品的所有评论
     */
    @GetMapping("/item/{itemId}")
    public Result<List<Comment>> getCommentsByItem(@PathVariable Long itemId) {
        List<Comment> comments = commentService.findByItemId(itemId);
        return Result.success(comments);
    }

    /**
     * 发表评论（需要登录）
     */
    @PostMapping("/create")
    public Result<Comment> createComment(@RequestBody Comment comment) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            String username = ThreadLocalUtil.getCurrentUsername();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            comment.setUserId(userId);
            comment.setUserNickname(username);
            Comment created = commentService.createComment(comment);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{commentId}")
    public Result<?> deleteComment(@PathVariable Long commentId) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            Integer role = ThreadLocalUtil.getCurrentRole();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            commentService.deleteComment(commentId, userId, role);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
