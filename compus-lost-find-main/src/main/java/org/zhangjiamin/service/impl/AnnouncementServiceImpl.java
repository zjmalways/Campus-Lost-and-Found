package org.zhangjiamin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhangjiamin.entity.Announcement;
import org.zhangjiamin.mapper.AnnouncementMapper;
import org.zhangjiamin.service.AnnouncementService;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> findAll(Integer isTop) {
        return announcementMapper.findAll(isTop);
    }

    @Override
    public Announcement findById(Long announcementId) {
        Announcement announcement = announcementMapper.findById(announcementId);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        return announcement;
    }

    @Override
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    @Transactional
    public Announcement updateAnnouncement(Announcement announcement) {
        Announcement existing = announcementMapper.findById(announcement.getAnnouncementId());
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        announcementMapper.update(announcement);
        return announcementMapper.findById(announcement.getAnnouncementId());
    }

    @Override
    @Transactional
    public boolean deleteAnnouncement(Long announcementId, Integer userRole) {
        // 只有管理员可以删除公告
        if (userRole == null || userRole != 1) {
            throw new RuntimeException("无权删除公告");
        }
        Announcement existing = announcementMapper.findById(announcementId);
        if (existing == null) {
            throw new RuntimeException("公告不存在");
        }
        announcementMapper.deleteById(announcementId);
        return true;
    }
}
