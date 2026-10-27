package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangjiaming.dto.ItemPageRequest;
import com.zhangjiaming.entity.Announcement;
import com.zhangjiaming.entity.Item;
import com.zhangjiaming.mapper.ItemMapper;
import com.zhangjiaming.service.AnnouncementService;
import com.zhangjiaming.service.ItemService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 为 AI 提供失物、招领和公告查询及操作工具，并在服务端复用身份权限校验。
 */
public class ChatFunctionTools {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final AnnouncementService announcementService;
    private final Long userId;
    private final String username;
    private final Integer userRole;

    public ChatFunctionTools(ItemService itemService, ItemMapper itemMapper,
                             AnnouncementService announcementService, Long userId,
                             String username, Integer userRole) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.announcementService = announcementService;
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;
    }

    /** 按条件查询失物招领信息，并限制单次返回数量。 */
    @Tool(name = "searchItems", description = "按关键词和筛选条件查询失物招领列表。每次最多返回10条。")
    public List<ItemSummary> searchItems(
            @ToolParam(description = "关键词，可匹配标题、描述、特征或地点", required = false) String keyword,
            @ToolParam(description = "发布类型：0表示丢失，1表示捡到", required = false) Integer publishType,
            @ToolParam(description = "物品分类：1证件、2钥匙、3电子设备、4衣物、5钱包、6其他", required = false) Integer itemType,
            @ToolParam(description = "状态：0表示未找回/未归还，1表示已找回/已归还", required = false) Integer status,
            @ToolParam(description = "页码，从1开始", required = false) Integer pageNum,
            @ToolParam(description = "每页条数，最多10条", required = false) Integer pageSize) {
        validateEnum(publishType, 0, 1, "发布类型");
        validateEnum(itemType, 1, 6, "物品分类");
        validateEnum(status, 0, 1, "物品状态");

        ItemPageRequest request = new ItemPageRequest();
        request.setKeyword(keyword);
        request.setPublishType(publishType);
        request.setItemType(itemType);
        request.setStatus(status);
        request.setPageNum(pageNum == null || pageNum < 1 ? 1 : pageNum);
        request.setPageSize(pageSize == null || pageSize < 1 ? 5 : Math.min(pageSize, 10));

        Page<Item> result = itemService.findPage(request);
        return result.getRecords().stream().map(ChatFunctionTools::toItemSummary).toList();
    }

    /** 查询指定物品的详情。 */
    @Tool(name = "getItemDetails", description = "根据物品ID查询失物招领详情")
    public ItemSummary getItemDetails(
            @ToolParam(description = "物品ID") Long itemId) {
        if (itemId == null || itemId < 1) {
            throw new IllegalArgumentException("物品ID必须为正整数");
        }
        return toItemSummary(itemService.findById(itemId));
    }

    /** 使用当前登录用户身份发布物品信息。 */
    @Tool(name = "createItem", description = "为当前登录用户发布一条丢失或捡到的物品信息")
    public ItemSummary createItem(
            @ToolParam(description = "物品标题") String title,
            @ToolParam(description = "物品分类：1证件、2钥匙、3电子设备、4衣物、5钱包、6其他") Integer itemType,
            @ToolParam(description = "发布类型：0表示丢失，1表示捡到") Integer publishType,
            @ToolParam(description = "物品描述") String description,
            @ToolParam(description = "物品特征", required = false) String features,
            @ToolParam(description = "丢失或捡到地点") String location,
            @ToolParam(description = "事件时间，ISO格式，例如2026-10-27T14:30:00", required = false) String eventTime,
            @ToolParam(description = "联系信息", required = false) String contact,
            @ToolParam(description = "物品存放地点", required = false) String storageLocation,
            @ToolParam(description = "图片URL，多个URL用逗号分隔", required = false) String images) {
        requireLogin();
        requireText(title, "物品标题");
        requireText(description, "物品描述");
        requireText(location, "物品地点");
        validateEnum(publishType, 0, 1, "发布类型");
        validateEnum(itemType, 1, 6, "物品分类");

        Item item = new Item();
        item.setTitle(title);
        item.setItemType(itemType);
        item.setPublishType(publishType);
        item.setDescription(description);
        item.setFeatures(features);
        item.setLocation(location);
        item.setEventTime(parseEventTime(eventTime));
        item.setContact(contact);
        item.setStorageLocation(storageLocation);
        item.setImages(images);
        item.setPublisherId(userId);
        item.setPublisherName(username);
        return toItemSummary(itemService.createItem(item));
    }

    /** 更新物品信息，并通过现有服务校验发布者或管理员权限。 */
    @Tool(name = "updateItem", description = "编辑物品信息，仅允许物品发布者或管理员操作；只更新提供的字段")
    public ItemSummary updateItem(
            @ToolParam(description = "物品ID") Long itemId,
            @ToolParam(description = "物品标题", required = false) String title,
            @ToolParam(description = "物品分类：1证件、2钥匙、3电子设备、4衣物、5钱包、6其他", required = false) Integer itemType,
            @ToolParam(description = "发布类型：0表示丢失，1表示捡到", required = false) Integer publishType,
            @ToolParam(description = "物品描述", required = false) String description,
            @ToolParam(description = "物品特征", required = false) String features,
            @ToolParam(description = "丢失或捡到地点", required = false) String location,
            @ToolParam(description = "事件时间，ISO格式，例如2026-10-27T14:30:00", required = false) String eventTime,
            @ToolParam(description = "联系信息", required = false) String contact,
            @ToolParam(description = "物品存放地点", required = false) String storageLocation,
            @ToolParam(description = "图片URL，多个URL用逗号分隔", required = false) String images) {
        requireLogin();
        if (itemId == null || itemId < 1) {
            throw new IllegalArgumentException("物品ID必须为正整数");
        }
        validateEnum(publishType, 0, 1, "发布类型");
        validateEnum(itemType, 1, 6, "物品分类");

        Item existing = itemMapper.selectById(itemId);
        if (existing == null) {
            throw new IllegalArgumentException("未找到指定物品");
        }
        Item update = new Item();
        update.setItemId(itemId);
        update.setTitle(valueOr(title, existing.getTitle()));
        update.setItemType(valueOr(itemType, existing.getItemType()));
        update.setPublishType(valueOr(publishType, existing.getPublishType()));
        update.setDescription(valueOr(description, existing.getDescription()));
        update.setFeatures(valueOr(features, existing.getFeatures()));
        update.setImages(valueOr(images, existing.getImages()));
        update.setLocation(valueOr(location, existing.getLocation()));
        update.setEventTime(eventTime == null ? existing.getEventTime() : parseEventTime(eventTime));
        update.setContact(valueOr(contact, existing.getContact()));
        update.setStorageLocation(valueOr(storageLocation, existing.getStorageLocation()));
        return toItemSummary(itemService.updateItem(update, userId, userRole));
    }

    /** 删除物品，并由现有服务验证操作权限。 */
    @Tool(name = "deleteItem", description = "删除物品，仅允许物品发布者或管理员操作；调用前应确认用户明确要求删除该物品")
    public String deleteItem(
            @ToolParam(description = "要删除的物品ID") Long itemId) {
        requireLogin();
        if (itemId == null || itemId < 1) {
            throw new IllegalArgumentException("物品ID必须为正整数");
        }
        itemService.deleteItem(itemId, userId, userRole);
        return "物品已删除";
    }

    /** 更新物品找回或归还状态，并由现有服务验证操作权限。 */
    @Tool(name = "updateItemStatus", description = "更新物品找回或归还状态，仅允许发布者或管理员操作")
    public String updateItemStatus(
            @ToolParam(description = "物品ID") Long itemId,
            @ToolParam(description = "目标状态：0表示未找回/未归还，1表示已找回/已归还") Integer status) {
        requireLogin();
        if (itemId == null || itemId < 1) {
            throw new IllegalArgumentException("物品ID必须为正整数");
        }
        validateEnum(status, 0, 1, "物品状态");
        itemService.updateStatus(itemId, status, userId, userRole);
        return "物品状态已更新";
    }

    /** 查询平台公告，并限制单次返回数量。 */
    @Tool(name = "searchAnnouncements", description = "查询平台公告列表，可按是否置顶筛选；最多返回10条")
    public List<AnnouncementSummary> searchAnnouncements(
            @ToolParam(description = "置顶筛选：0不置顶，1置顶；不筛选时省略", required = false) Integer isTop) {
        validateEnum(isTop, 0, 1, "置顶状态");
        return announcementService.findAll(isTop).stream()
                .limit(10)
                .map(ChatFunctionTools::toAnnouncementSummary)
                .toList();
    }

    /** 查询指定公告的详情。 */
    @Tool(name = "getAnnouncementDetails", description = "根据公告ID查询公告详情")
    public AnnouncementSummary getAnnouncementDetails(
            @ToolParam(description = "公告ID") Long announcementId) {
        requirePositiveId(announcementId, "公告ID");
        return toAnnouncementSummary(announcementService.findById(announcementId));
    }

    /** 校验管理员身份后发布公告。 */
    @Tool(name = "createAnnouncement", description = "发布平台公告，仅管理员可操作")
    public AnnouncementSummary createAnnouncement(
            @ToolParam(description = "公告标题") String title,
            @ToolParam(description = "公告正文") String content,
            @ToolParam(description = "是否置顶：0否，1是；默认不置顶", required = false) Integer isTop) {
        requireAdmin();
        requireText(title, "公告标题");
        requireText(content, "公告正文");
        validateEnum(isTop, 0, 1, "置顶状态");
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setIsTop(isTop == null ? 0 : isTop);
        announcement.setPublisherId(userId);
        announcement.setPublisherName(username);
        return toAnnouncementSummary(announcementService.createAnnouncement(announcement));
    }

    /** 校验管理员身份后更新公告中明确提供的字段。 */
    @Tool(name = "updateAnnouncement", description = "编辑平台公告，仅管理员可操作；只更新提供的字段")
    public AnnouncementSummary updateAnnouncement(
            @ToolParam(description = "公告ID") Long announcementId,
            @ToolParam(description = "公告标题", required = false) String title,
            @ToolParam(description = "公告正文", required = false) String content,
            @ToolParam(description = "是否置顶：0否，1是", required = false) Integer isTop) {
        requireAdmin();
        requirePositiveId(announcementId, "公告ID");
        if (title == null && content == null && isTop == null) {
            throw new IllegalArgumentException("至少提供一个需要更新的公告字段");
        }
        validateEnum(isTop, 0, 1, "置顶状态");
        Announcement existing = announcementService.findById(announcementId);
        Announcement update = new Announcement();
        update.setAnnouncementId(announcementId);
        update.setTitle(valueOr(title, existing.getTitle()));
        update.setContent(valueOr(content, existing.getContent()));
        update.setIsTop(valueOr(isTop, existing.getIsTop()));
        return toAnnouncementSummary(announcementService.updateAnnouncement(update));
    }

    /** 校验管理员身份后删除指定公告。 */
    @Tool(name = "deleteAnnouncement", description = "删除平台公告，仅管理员可操作；调用前应确认用户明确要求删除该公告")
    public String deleteAnnouncement(
            @ToolParam(description = "要删除的公告ID") Long announcementId) {
        requireAdmin();
        requirePositiveId(announcementId, "公告ID");
        announcementService.deleteAnnouncement(announcementId);
        return "公告已删除";
    }

    private void requireLogin() {
        if (userId == null) {
            throw new IllegalStateException("请先登录后再执行此操作");
        }
    }

    private void requireAdmin() {
        requireLogin();
        if (userRole == null || userRole != 1) {
            throw new IllegalStateException("只有管理员可以管理公告");
        }
    }

    private static void requirePositiveId(Long id, String label) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException(label + "必须为正整数");
        }
    }

    private static void requireText(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(label + "不能为空");
        }
    }

    private static void validateEnum(Integer value, int min, int max, String label) {
        if (value != null && (value < min || value > max)) {
            throw new IllegalArgumentException(label + "取值无效");
        }
    }

    private static LocalDateTime parseEventTime(String eventTime) {
        if (!StringUtils.hasText(eventTime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(eventTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("事件时间必须使用ISO格式，例如2026-10-27T14:30:00", e);
        }
    }

    private static <T> T valueOr(T value, T existing) {
        return value == null ? existing : value;
    }

    private static ItemSummary toItemSummary(Item item) {
        return new ItemSummary(item.getItemId(), item.getTitle(), item.getItemType(),
                item.getPublishType(), item.getDescription(), item.getFeatures(), item.getLocation(),
                item.getEventTime(), item.getStorageLocation(), item.getStatus());
    }

    private static AnnouncementSummary toAnnouncementSummary(Announcement announcement) {
        return new AnnouncementSummary(announcement.getAnnouncementId(), announcement.getTitle(),
                announcement.getContent(), announcement.getIsTop(), announcement.getCreateTime());
    }

    public record ItemSummary(Long itemId, String title, Integer itemType, Integer publishType,
                              String description, String features, String location,
                              LocalDateTime eventTime, String storageLocation, Integer status) {
    }

    public record AnnouncementSummary(Long announcementId, String title, String content,
                                      Integer isTop, LocalDateTime createTime) {
    }
}
