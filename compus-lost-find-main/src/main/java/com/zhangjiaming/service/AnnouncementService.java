package com.zhangjiaming.service;

import com.zhangjiaming.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    /**
     * 查询公告列表（可选择性按是否置顶筛选，置顶优先 + 时间倒序）
     *
     * @param isTop 是否置顶（可为 null）
     * @return 公告列表
     */
    List<Announcement> findAll(Integer isTop);

    /**
     * 查询公告详情
     *
     * @param announcementId 公告ID
     * @return 公告详情
     */
    Announcement findById(Long announcementId);

    /**
     * 发布公告
     *
     * @param announcement 公告信息
     * @return 创建后的公告
     */
    Announcement createAnnouncement(Announcement announcement);

    /**
     * 编辑公告
     *
     * @param announcement 待更新的公告信息
     * @return 更新后的公告
     */
    Announcement updateAnnouncement(Announcement announcement);

    /**
     * 删除公告
     *
     * @param announcementId 公告ID
     * @return 是否删除成功
     */
    boolean deleteAnnouncement(Long announcementId);
}
