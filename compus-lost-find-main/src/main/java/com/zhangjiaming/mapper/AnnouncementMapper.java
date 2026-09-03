package com.zhangjiaming.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangjiaming.annotation.Autofill;
import com.zhangjiaming.annotation.Autofill.OperationType;
import com.zhangjiaming.entity.Announcement;

/**
 * 公告 Mapper：继承 BaseMapper 获得通用 CRUD 能力
 */
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 新增公告（自动填充 createTime / updateTime）
     */
    @Override
    @Autofill(OperationType.INSERT)
    int insert(Announcement entity);

    /**
     * 更新公告（自动填充 updateTime）
     */
    @Override
    @Autofill(OperationType.UPDATE)
    int updateById(Announcement entity);
}
