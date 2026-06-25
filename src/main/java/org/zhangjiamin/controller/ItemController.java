package org.zhangjiamin.controller;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zhangjiamin.common.Result;
import org.zhangjiamin.dto.ItemPageRequest;
import org.zhangjiamin.entity.Item;
import org.zhangjiamin.service.ItemService;
import org.zhangjiamin.util.ThreadLocalUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 分页查询物品列表（公开接口）
     */
    @GetMapping("/list")
    public Result<PageInfo<Item>> list(ItemPageRequest request) {
        PageInfo<Item> pageInfo = itemService.findPage(request);
        return Result.success(pageInfo);
    }

    /**
     * 查询物品详情（公开接口）
     */
    @GetMapping("/detail/{itemId}")
    public Result<Item> detail(@PathVariable Long itemId) {
        try {
            Item item = itemService.findById(itemId);
            return Result.success(item);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布物品（需要登录）
     */
    @PostMapping("/create")
    public Result<Item> create(@RequestBody Item item) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            String username = ThreadLocalUtil.getCurrentUsername();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            item.setPublisherId(userId);
            item.setPublisherName(username);
            Item created = itemService.createItem(item);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新物品信息
     */
    @PutMapping("/update")
    public Result<Item> update(@RequestBody Item item) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            Integer role = ThreadLocalUtil.getCurrentRole();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            // 验证权限在service层处理
            Item updated = itemService.updateItem(item);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除物品
     */
    @DeleteMapping("/delete/{itemId}")
    public Result<?> delete(@PathVariable Long itemId) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            Integer role = ThreadLocalUtil.getCurrentRole();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            itemService.deleteItem(itemId, userId, role);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新物品状态（找回/归还）
     */
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            Integer role = ThreadLocalUtil.getCurrentRole();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            Long itemId = Long.valueOf(params.get("itemId").toString());
            Integer status = Integer.valueOf(params.get("status").toString());
            itemService.updateStatus(itemId, status, userId, role);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取物品类型列表
     */
    @GetMapping("/types")
    public Result<Map<Integer, String>> getItemTypes() {
        Map<Integer, String> types = new HashMap<>();
        types.put(1, "证件");
        types.put(2, "钥匙");
        types.put(3, "电子设备");
        types.put(4, "衣物");
        types.put(5, "钱包");
        types.put(6, "其他");
        return Result.success(types);
    }
}
