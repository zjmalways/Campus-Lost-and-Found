package com.zhangjiaming.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangjiaming.dto.ItemPageRequest;
import com.zhangjiaming.entity.Item;

public interface ItemService {

    /**
     * 分页查询物品列表
     *
     * @param request 分页及筛选条件
     * @return 分页结果
     */
    Page<Item> findPage(ItemPageRequest request);

    /**
     * 查询物品详情（并增加浏览量）
     *
     * @param itemId 物品ID
     * @return 物品详情
     */
    Item findById(Long itemId);

    /**
     * 发布物品
     *
     * @param item 物品信息
     * @return 创建后的物品
     */
    Item createItem(Item item);

    /**
     * 编辑物品（仅发布者或管理员）
     *
     * @param item      待更新的物品信息
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 更新后的物品
     */
    Item updateItem(Item item, Long userId, Integer userRole);

    /**
     * 删除物品（仅发布者或管理员）
     *
     * @param itemId    物品ID
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 是否删除成功
     */
    boolean deleteItem(Long itemId, Long userId, Integer userRole);

    /**
     * 更新物品状态（找回/归还，仅发布者或管理员）
     *
     * @param itemId    物品ID
     * @param status    目标状态
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 是否更新成功
     */
    boolean updateStatus(Long itemId, Integer status, Long userId, Integer userRole);
}
