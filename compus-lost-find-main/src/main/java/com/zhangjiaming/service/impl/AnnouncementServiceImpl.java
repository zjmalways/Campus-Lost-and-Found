package com.zhangjiaming.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangjiaming.context.ErrorContext;
import com.zhangjiaming.entity.Announcement;
import com.zhangjiaming.mapper.AnnouncementMapper;
import com.zhangjiaming.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 查询公告列表（置顶优先 + 时间倒序）
     *
     * @param isTop 是否置顶（可为 null）
     * @return 公告列表
     */
    @Override
    public List<Announcement> findAll(Integer isTop) {
        return announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>()
                        .eq(isTop != null, Announcement::getIsTop, isTop)
                        .orderByDesc(Announcement::getIsTop)
                        .orderByDesc(Announcement::getCreateTime));
    }

    /**
     * 查询公告详情
     *
     * @param announcementId 公告ID
     * @return 公告详情
     */
    @Override
    public Announcement findById(Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new RuntimeException(ErrorContext.ANNOUNCEMENT_NOT_EXISTS);
        }
        return announcement;
    }

    /**
     * 发布公告
     *
     * @param announcement 公告信息
     * @return 创建后的公告
     */
    @Override
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    /**
     * 编辑公告（仅更新标题/内容/置顶）
     *
     * @param announcement 待更新的公告信息
     * @return 更新后的公告
     */
    @Override
    @Transactional
    public Announcement updateAnnouncement(Announcement announcement) {
        Announcement existing = announcementMapper.selectById(announcement.getAnnouncementId());
        if (existing == null) {
            throw new RuntimeException(ErrorContext.ANNOUNCEMENT_NOT_EXISTS);
        }

        Announcement update = new Announcement();
        update.setAnnouncementId(announcement.getAnnouncementId());
        update.setTitle(announcement.getTitle());
        update.setContent(announcement.getContent());
        update.setIsTop(announcement.getIsTop());
        announcementMapper.updateById(update);

        return announcementMapper.selectById(announcement.getAnnouncementId());
    }

    /**
     * 删除公告
     *
     * @param announcementId 公告ID
     * @return 是否删除成功
     */
    @Override
    @Transactional
    public boolean deleteAnnouncement(Long announcementId) {
        Announcement existing = announcementMapper.selectById(announcementId);
        if (existing == null) {
            throw new RuntimeException(ErrorContext.ANNOUNCEMENT_NOT_EXISTS);
        }
        announcementMapper.deleteById(announcementId);
        return true;
    }
}
