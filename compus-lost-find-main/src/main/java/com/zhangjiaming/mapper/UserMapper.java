package com.zhangjiaming.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangjiaming.annotation.Autofill;
import com.zhangjiaming.annotation.Autofill.OperationType;
import com.zhangjiaming.entity.User;

/**
 * 用户 Mapper：继承 BaseMapper 获得通用 CRUD 能力
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 新增用户（自动填充 createTime / updateTime）
     */
    @Override
    @Autofill(OperationType.INSERT)
    int insert(User entity);

    /**
     * 更新用户（自动填充 updateTime）
     */
    @Override
    @Autofill(OperationType.UPDATE)
    int updateById(User entity);
}
