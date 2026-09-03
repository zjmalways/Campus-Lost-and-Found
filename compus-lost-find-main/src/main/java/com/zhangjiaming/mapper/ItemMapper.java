package com.zhangjiaming.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangjiaming.annotation.Autofill;
import com.zhangjiaming.annotation.Autofill.OperationType;
import com.zhangjiaming.entity.Item;

/**
 * 物品 Mapper：继承 BaseMapper 获得通用 CRUD 能力
 */
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 新增物品（自动填充 createTime / updateTime）
     */
    @Override
    @Autofill(OperationType.INSERT)
    int insert(Item entity);

    /**
     * 更新物品（自动填充 updateTime）
     */
    @Override
    @Autofill(OperationType.UPDATE)
    int updateById(Item entity);
}
