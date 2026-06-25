package org.zhangjiamin.service;

import org.zhangjiamin.entity.Announcement;

import java.util.List;

public interface AnnouncementService {
    List<Announcement> findAll(Integer isTop);
    Announcement findById(Long announcementId);
    Announcement createAnnouncement(Announcement announcement);
    Announcement updateAnnouncement(Announcement announcement);
    boolean deleteAnnouncement(Long announcementId, Integer userRole);
}
