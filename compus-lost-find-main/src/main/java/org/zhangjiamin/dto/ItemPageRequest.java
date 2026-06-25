package org.zhangjiamin.dto;

import lombok.Data;

@Data
public class ItemPageRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Integer publishType;    // 0-丢失, 1-捡到
    private Integer itemType;       // 物品分类
    private Integer status;         // 物品状态
    private String keyword;         // 关键词搜索
    private Long publisherId;       // 发布者ID
}
