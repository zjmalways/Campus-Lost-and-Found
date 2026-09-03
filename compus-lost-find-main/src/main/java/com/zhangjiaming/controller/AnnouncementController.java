package com.zhangjiaming.controller;

import com.zhangjiaming.common.Result;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.entity.Announcement;
import com.zhangjiaming.service.AnnouncementService;
import com.zhangjiaming.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公告管理", description = "公告发布、查询、编辑、删除")
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 公告列表（公开，可选按是否置顶筛选）
     */
    @Operation(summary = "公告列表", description = "查询公告列表，置顶优先，可按是否置顶筛选（公开）")
    @GetMapping("/list")
    public Result<List<Announcement>> list(@RequestParam(required = false) Integer isTop) {
        return Result.success(announcementService.findAll(isTop));
    }

    /**
     * 公告详情（公开）
     */
    @Operation(summary = "公告详情", description = "查询公告详情（公开）")
    @GetMapping("/detail/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        return Result.success(announcementService.findById(id));
    }

    /**
     * 发布公告（仅管理员）
     */
    @Operation(summary = "发布公告", description = "发布公告（仅管理员）")
    @PostMapping("/create")
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        Long userId = ThreadLocalUtil.getCurrentUserId();
        String username = ThreadLocalUtil.getCurrentUsername();
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (userId == null) {
            return Result.unauthorized(ErrorContext.NOT_LOGIN);
        }
        if (role == null || role != 1) {
            return Result.forbidden(ErrorContext.NOT_ADMIN_CREATE);
        }
        announcement.setPublisherId(userId);
        announcement.setPublisherName(username);
        return Result.success(announcementService.createAnnouncement(announcement));
    }

    /**
     * 编辑公告（仅管理员）
     */
    @Operation(summary = "编辑公告", description = "编辑公告（仅管理员）")
    @PutMapping("/update")
    public Result<Announcement> update(@RequestBody Announcement announcement) {
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (role == null || role != 1) {
            return Result.forbidden(ErrorContext.NOT_ADMIN_UPDATE);
        }
        return Result.success(announcementService.updateAnnouncement(announcement));
    }

    /**
     * 删除公告（仅管理员）
     */
    @Operation(summary = "删除公告", description = "删除公告（仅管理员）")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Integer role = ThreadLocalUtil.getCurrentRole();
        if (role == null || role != 1) {
            return Result.forbidden(ErrorContext.NOT_ADMIN_DELETE);
        }
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }
}
