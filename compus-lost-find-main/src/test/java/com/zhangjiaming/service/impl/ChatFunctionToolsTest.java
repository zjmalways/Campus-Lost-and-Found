package com.zhangjiaming.service.impl;

import com.zhangjiaming.entity.Announcement;
import com.zhangjiaming.entity.Item;
import com.zhangjiaming.mapper.ItemMapper;
import com.zhangjiaming.service.AnnouncementService;
import com.zhangjiaming.service.ItemService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class ChatFunctionToolsTest {

    @Test
    void registersItemAndAnnouncementFunctionCallingTools() {
        ChatFunctionTools tools = new ChatFunctionTools(
                mock(ItemService.class), mock(ItemMapper.class), mock(AnnouncementService.class),
                42L, "alice", 0);

        Set<String> toolNames = Arrays.stream(MethodToolCallbackProvider.builder()
                        .toolObjects(tools)
                        .build()
                        .getToolCallbacks())
                .map(ToolCallback::getToolDefinition)
                .map(definition -> definition.name())
                .collect(Collectors.toSet());

        assertEquals(Set.of("searchItems", "getItemDetails", "createItem", "updateItem",
                        "deleteItem", "updateItemStatus", "searchAnnouncements",
                        "getAnnouncementDetails", "createAnnouncement", "updateAnnouncement",
                        "deleteAnnouncement"), toolNames);
    }

    @Test
    void createItemUsesAuthenticatedUserIdentity() {
        ItemService itemService = mock(ItemService.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        AnnouncementService announcementService = mock(AnnouncementService.class);
        when(itemService.createItem(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ChatFunctionTools tools = new ChatFunctionTools(
                itemService, itemMapper, announcementService, 42L, "alice", 0);

        ChatFunctionTools.ItemSummary result = tools.createItem(
                "遗失钥匙", 2, 0, "一串钥匙", "有蓝色钥匙扣",
                "图书馆", null, null, null, null);

        assertEquals("遗失钥匙", result.title());
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemService).createItem(itemCaptor.capture());
        assertEquals(42L, itemCaptor.getValue().getPublisherId());
        assertEquals("alice", itemCaptor.getValue().getPublisherName());
    }

    @Test
    void nonAdminCannotCreateAnnouncement() {
        ItemService itemService = mock(ItemService.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        AnnouncementService announcementService = mock(AnnouncementService.class);
        ChatFunctionTools tools = new ChatFunctionTools(
                itemService, itemMapper, announcementService, 42L, "alice", 0);

        assertThrows(IllegalStateException.class,
                () -> tools.createAnnouncement("公告", "公告内容", null));
        verify(announcementService, never()).createAnnouncement(any(Announcement.class));
    }

    @Test
    void updateItemPreservesFieldsNotMentionedByUser() {
        ItemService itemService = mock(ItemService.class);
        ItemMapper itemMapper = mock(ItemMapper.class);
        AnnouncementService announcementService = mock(AnnouncementService.class);
        Item existing = new Item();
        existing.setItemId(7L);
        existing.setTitle("遗失钥匙");
        existing.setItemType(2);
        existing.setPublishType(0);
        existing.setDescription("原描述");
        existing.setLocation("图书馆");
        when(itemMapper.selectById(7L)).thenReturn(existing);
        when(itemService.updateItem(any(Item.class), eq(42L), eq(0)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ChatFunctionTools tools = new ChatFunctionTools(
                itemService, itemMapper, announcementService, 42L, "alice", 0);

        ChatFunctionTools.ItemSummary result = tools.updateItem(
                7L, null, null, null, "更新后的描述",
                null, null, null, null, null, null);

        assertEquals("遗失钥匙", result.title());
        assertEquals("更新后的描述", result.description());
        assertEquals("图书馆", result.location());
        verify(itemService).updateItem(any(Item.class), eq(42L), eq(0));
    }
}
