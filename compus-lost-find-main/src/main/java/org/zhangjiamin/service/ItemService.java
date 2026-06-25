package org.zhangjiamin.service;

import com.github.pagehelper.PageInfo;
import org.zhangjiamin.dto.ItemPageRequest;
import org.zhangjiamin.entity.Item;

public interface ItemService {
    PageInfo<Item> findPage(ItemPageRequest request);
    Item findById(Long itemId);
    Item createItem(Item item);
    Item updateItem(Item item);
    boolean deleteItem(Long itemId, Long userId, Integer userRole);
    boolean updateStatus(Long itemId, Integer status, Long userId, Integer userRole);
}
