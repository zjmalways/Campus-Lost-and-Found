package com.zhangjiaming.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.dto.ItemPageRequest;
import com.zhangjiaming.entity.Item;
import com.zhangjiaming.service.ItemService;
import com.zhangjiaming.util.AliyunOSSUtil;
import com.zhangjiaming.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "物品管理", description = "物品发布、查询、编辑、删除、图片上传")
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;

    /**
     * 分页查询物品列表（公开）
     */
    @Operation(summary = "分页查询物品列表", description = "支持按发布类型/物品类型/状态/发布者筛选及关键词搜索（公开）")
    @GetMapping("/list")
    public Result<Page<Item>> list(ItemPageRequest request) {
        return Result.success(itemService.findPage(request));
    }

    /**
     * 物品详情（公开）
     */
    @Operation(summary = "物品详情", description = "查询物品详情并增加浏览量（公开）")
    @GetMapping("/detail/{itemId}")
    public Result<Item> detail(@PathVariable Long itemId) {
        return Result.success(itemService.findById(itemId));
    }

    /**
     * 发布物品（需登录）
     */
    @Operation(summary = "发布物品", description = "发布一条丢失/捡到物品信息（需登录）")
    @PostMapping("/create")
    public Result<Item> create(@RequestBody Item item) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        String username = ThreadLocalUtil.getCurrentUsername();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        item.setPublisherId(userId);
        item.setPublisherName(username);
        return Result.success(itemService.createItem(item));
    }

    /**
     * 编辑物品（需登录，仅发布者或管理员）
     */
    @Operation(summary = "编辑物品", description = "编辑物品信息，仅发布者或管理员（需登录）")
    @PutMapping("/update")
    public Result<Item> update(@RequestBody Item item) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        return Result.success(itemService.updateItem(item, userId, role));
    }

    /**
     * 删除物品（需登录，仅发布者或管理员）
     */
    @Operation(summary = "删除物品", description = "删除物品，仅发布者或管理员（需登录）")
    @DeleteMapping("/delete/{itemId}")
    public Result<?> delete(@PathVariable Long itemId) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        itemService.deleteItem(itemId, userId, role);
        return Result.success();
    }

    /**
     * 更新物品状态（找回/归还，需登录，仅发布者或管理员）
     */
    @Operation(summary = "更新物品状态", description = "更新物品状态（找回/归还），仅发布者或管理员（需登录）")
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        Object itemIdObj = params.get("itemId");
        Object statusObj = params.get("status");
        if (itemIdObj == null || statusObj == null) {
            return Result.error(ErrorContext.PARAM_ERROR);
        }
        Long itemId = Long.valueOf(itemIdObj.toString());
        Integer status = Integer.valueOf(statusObj.toString());
        itemService.updateStatus(itemId, status, userId, role);
        return Result.success();
    }

    /**
     * 获取物品分类（公开）
     */
    @Operation(summary = "获取物品分类", description = "获取物品类型字典（公开）")
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

    /**
     * 上传单张物品图片（需登录）
     */
    @Operation(summary = "上传单张物品图片", description = "上传单张图片到 OSS（需登录）")
    @PostMapping("/upload/image")
    public Result<String> uploadItemImage(@RequestParam("file") MultipartFile file) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(ErrorContext.ONLY_IMAGE_ALLOWED);
        }
        String url = aliyunOSSUtil.uploadItemImage(file);
        log.info("物品图片上传成功 —— userId: {}, url: {}", userId, url);
        return Result.success(url);
    }

    /**
     * 批量上传物品图片（需登录）
     */
    @Operation(summary = "批量上传物品图片", description = "批量上传图片到 OSS（需登录）")
    @PostMapping("/upload/images")
    public Result<List<String>> uploadItemImages(@RequestParam("files") MultipartFile[] files) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        List<String> urls = aliyunOSSUtil.uploadItemImages(files);
        log.info("物品图片批量上传成功 —— userId: {}, 数量: {}", userId, urls.size());
        return Result.success(urls);
    }

    /**
     * 更新物品图片列表（需登录，仅发布者或管理员）
     */
    @Operation(summary = "更新物品图片列表", description = "替换物品图片列表，仅发布者或管理员（需登录）")
    @PutMapping("/images/{itemId}")
    public Result<?> updateItemImages(@PathVariable Long itemId, @RequestBody Map<String, Object> params) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }

        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) params.get("imageUrls");

        Item item = new Item();
        item.setItemId(itemId);
        item.setImages(imageUrls != null && !imageUrls.isEmpty() ? String.join(",", imageUrls) : "");

        itemService.updateItem(item, userId, role);
        return Result.success();
    }

    /**
     * 删除 OSS 上的物品图片（需登录）
     */
    @Operation(summary = "删除 OSS 图片", description = "删除 OSS 上的单张图片（需登录）")
    @DeleteMapping("/upload/image")
    public Result<?> deleteItemImage(@RequestBody Map<String, String> params) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        String imageUrl = params.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.error(ErrorContext.IMAGE_URL_EMPTY);
        }
        aliyunOSSUtil.deleteFile(imageUrl);
        return Result.success();
    }
}
