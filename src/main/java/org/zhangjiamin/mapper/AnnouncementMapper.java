package org.zhangjiamin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.zhangjiamin.entity.Announcement;
import java.util.List;

@Mapper
public interface AnnouncementMapper {
    Announcement findById(Long announcementId);
    int insert(Announcement announcement);
    int update(Announcement announcement);
    int deleteById(Long announcementId);

    List<Announcement> findAll(@Param("isTop") Integer isTop);
    List<Announcement> findTopAnnouncements();
}
