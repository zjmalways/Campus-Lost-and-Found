package com.zhangjiaming.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangjiaming.annotation.Autofill;
import com.zhangjiaming.annotation.Autofill.OperationType;
import com.zhangjiaming.entity.Comment;

/**
 * 评论 Mapper：继承 BaseMapper 获得通用 CRUD 能力
 */
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 新增评论（自动填充 createTime）
     */
    @Override
    @Autofill(OperationType.INSERT)
    int insert(Comment entity);
}
