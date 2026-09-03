package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.dto.ItemPageRequest;
import com.zhangjiaming.entity.Item;
import com.zhangjiaming.mapper.ItemMapper;
import com.zhangjiaming.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 分页查询物品列表（支持按发布类型/物品类型/状态/发布者筛选、关键词模糊搜索）
     *
     * @param request 分页及筛选条件
     * @return 分页结果
     */
    @Override
    public Page<Item> findPage(ItemPageRequest request) {
        Page<Item> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<Item>()
                .eq(request.getPublishType() != null, Item::getPublishType, request.getPublishType())
                .eq(request.getItemType() != null, Item::getItemType, request.getItemType())
                .eq(request.getStatus() != null, Item::getStatus, request.getStatus())
                .eq(request.getPublisherId() != null, Item::getPublisherId, request.getPublisherId())
                .and(StringUtils.hasText(request.getKeyword()), w -> w
                        .like(Item::getTitle, request.getKeyword())
                        .or().like(Item::getDescription, request.getKeyword())
                        .or().like(Item::getFeatures, request.getKeyword())
                        .or().like(Item::getLocation, request.getKeyword()))
                .orderByDesc(Item::getCreateTime);

        return itemMapper.selectPage(page, wrapper);
    }

    /**
     * 查询物品详情，并增加浏览量
     *
     * @param itemId 物品ID
     * @return 物品详情
     */
    @Override
    public Item findById(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException(ErrorContext.ITEM_NOT_EXISTS);
        }

        // 浏览量 +1
        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getItemId, itemId)
                .setSql("view_count = view_count + 1"));
        item.setViewCount(item.getViewCount() == null ? 1 : item.getViewCount() + 1);
        return item;
    }

    /**
     * 发布物品
     *
     * @param item 物品信息
     * @return 创建后的物品
     */
    @Override
    @Transactional
    public Item createItem(Item item) {
        if (item.getStatus() == null) {
            item.setStatus(0);
        }
        if (item.getViewCount() == null) {
            item.setViewCount(0);
        }
        if (item.getCollectCount() == null) {
            item.setCollectCount(0);
        }
        if (item.getCommentCount() == null) {
            item.setCommentCount(0);
        }
        itemMapper.insert(item);
        return item;
    }

    /**
     * 编辑物品（仅发布者或管理员）
     *
     * @param item      待更新的物品信息
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 更新后的物品
     */
    @Override
    @Transactional
    public Item updateItem(Item item, Long userId, Integer userRole) {
        Item existing = itemMapper.selectById(item.getItemId());
        if (existing == null) {
            throw new RuntimeException(ErrorContext.ITEM_NOT_EXISTS);
        }
        if (!existing.getPublisherId().equals(userId) && (userRole == null || userRole != 1)) {
            throw new RuntimeException(ErrorContext.NO_PERMISSION_UPDATE);
        }

        // 仅允许更新可编辑字段，防止越权修改发布者/浏览量/状态等
        Item update = new Item();
        update.setItemId(item.getItemId());
        update.setItemType(item.getItemType());
        update.setPublishType(item.getPublishType());
        update.setTitle(item.getTitle());
        update.setDescription(item.getDescription());
        update.setFeatures(item.getFeatures());
        update.setImages(item.getImages());
        update.setLocation(item.getLocation());
        update.setEventTime(item.getEventTime());
        update.setContact(item.getContact());
        update.setStorageLocation(item.getStorageLocation());

        itemMapper.updateById(update);
        return itemMapper.selectById(item.getItemId());
    }

    /**
     * 删除物品（仅发布者或管理员）
     *
     * @param itemId    物品ID
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteItem(Long itemId, Long userId, Integer userRole) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException(ErrorContext.ITEM_NOT_EXISTS);
        }
        if (!item.getPublisherId().equals(userId) && (userRole == null || userRole != 1)) {
            throw new RuntimeException(ErrorContext.NO_PERMISSION_DELETE);
        }
        itemMapper.deleteById(itemId);
        return true;
    }

    /**
     * 更新物品状态（找回/归还，仅发布者或管理员）
     *
     * @param itemId    物品ID
     * @param status    目标状态
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateStatus(Long itemId, Integer status, Long userId, Integer userRole) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException(ErrorContext.ITEM_NOT_EXISTS);
        }
        if (!item.getPublisherId().equals(userId) && (userRole == null || userRole != 1)) {
            throw new RuntimeException(ErrorContext.NO_PERMISSION_STATUS);
        }

        Item update = new Item();
        update.setItemId(itemId);
        update.setStatus(status);
        itemMapper.updateById(update);
        return true;
    }
}
