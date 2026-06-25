package org.zhangjiamin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhangjiamin.dto.ItemPageRequest;
import org.zhangjiamin.entity.Item;
import org.zhangjiamin.mapper.ItemMapper;
import org.zhangjiamin.service.ItemService;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public PageInfo<Item> findPage(ItemPageRequest request) {
        PageHelper.startPage(request.getPageNum(), request.getPageSize());
        List<Item> items = itemMapper.findPage(
                request.getPublishType(),
                request.getItemType(),
                request.getStatus(),
                request.getKeyword(),
                request.getPublisherId()
        );
        return new PageInfo<>(items);
    }

    @Override
    public Item findById(Long itemId) {
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }
        // 增加浏览量
        itemMapper.incrementViewCount(itemId);
        item.setViewCount(item.getViewCount() == null ? 1 : item.getViewCount() + 1);
        return item;
    }

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

    @Override
    @Transactional
    public Item updateItem(Item item) {
        Item existingItem = itemMapper.findById(item.getItemId());
        if (existingItem == null) {
            throw new RuntimeException("物品不存在");
        }

        itemMapper.update(item);
        return itemMapper.findById(item.getItemId());
    }

    @Override
    @Transactional
    public boolean deleteItem(Long itemId, Long userId, Integer userRole) {
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        // 只有发布者或管理员可以删除
        if (!item.getPublisherId().equals(userId) && userRole != 1) {
            throw new RuntimeException("无权删除此物品");
        }

        itemMapper.deleteById(itemId);
        return true;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long itemId, Integer status, Long userId, Integer userRole) {
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            throw new RuntimeException("物品不存在");
        }

        // 只有发布者或管理员可以修改状态
        if (!item.getPublisherId().equals(userId) && userRole != 1) {
            throw new RuntimeException("无权修改此物品状态");
        }

        item.setStatus(status);
        itemMapper.update(item);
        return true;
    }
}
