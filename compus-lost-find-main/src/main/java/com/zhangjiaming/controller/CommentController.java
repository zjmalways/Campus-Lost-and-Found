package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.entity.Comment;
import com.zhangjiaming.service.CommentService;
import com.zhangjiaming.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论管理", description = "评论发表、查询、删除")
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取物品的所有评论（公开）
     */
    @Operation(summary = "获取评论列表", description = "获取物品的所有评论（公开）")
    @GetMapping("/item/{itemId}")
    public Result<List<Comment>> getCommentsByItem(@PathVariable Long itemId) {
        return Result.success(commentService.findByItemId(itemId));
    }

    /**
     * 发表评论（需登录）
     */
    @Operation(summary = "发表评论", description = "对物品发表评论（需登录）")
    @PostMapping("/create")
    public Result<Comment> createComment(@RequestBody Comment comment) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        comment.setUserId(userId);
        return Result.success(commentService.createComment(comment));
    }

    /**
     * 删除评论（需登录，仅评论作者或管理员）
     */
    @Operation(summary = "删除评论", description = "删除评论，仅评论作者或管理员（需登录）")
    @DeleteMapping("/delete/{commentId}")
    public Result<?> deleteComment(@PathVariable Long commentId) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        commentService.deleteComment(commentId, userId, role);
        return Result.success();
    }
}
