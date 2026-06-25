package org.zhangjiamin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zhangjiamin.common.Result;
import org.zhangjiamin.entity.Announcement;
import org.zhangjiamin.service.AnnouncementService;
import org.zhangjiamin.util.ThreadLocalUtil;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取公告列表（公开接口）
     */
    @GetMapping("/list")
    public Result<List<Announcement>> list(@RequestParam(required = false) Integer isTop) {
        List<Announcement> list = announcementService.findAll(isTop);
        return Result.success(list);
    }

    /**
     * 公告详情（公开接口）
     */
    @GetMapping("/detail/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        try {
            Announcement announcement = announcementService.findById(id);
            return Result.success(announcement);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布公告（管理员）
     */
    @PostMapping("/create")
    public Result<Announcement> create(@RequestBody Announcement announcement) {
        try {
            Long userId = ThreadLocalUtil.getCurrentUserId();
            Integer role = ThreadLocalUtil.getCurrentRole();
            String username = ThreadLocalUtil.getCurrentUsername();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }
            if (role == null || role != 1) {
                return Result.forbidden("仅管理员可发布公告");
            }
            announcement.setPublisherId(userId);
            announcement.setPublisherName(username);
            Announcement created = announcementService.createAnnouncement(announcement);
            return Result.success(created);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/update")
    public Result<Announcement> update(@RequestBody Announcement announcement) {
        try {
            Integer role = ThreadLocalUtil.getCurrentRole();
            if (role == null || role != 1) {
                return Result.forbidden("仅管理员可修改公告");
            }
            Announcement updated = announcementService.updateAnnouncement(announcement);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            Integer role = ThreadLocalUtil.getCurrentRole();
            announcementService.deleteAnnouncement(id, role);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
