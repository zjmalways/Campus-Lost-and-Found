package com.zhangjiaming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "物品分页查询请求")
public class ItemPageRequest {

    @Schema(description = "页码，默认1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认12")
    private Integer pageSize = 12;

    @Schema(description = "发布类型：0-丢失,1-捡到")
    private Integer publishType;

    @Schema(description = "物品分类：1-证件,2-钥匙,3-电子设备,4-衣物,5-钱包,6-其他")
    private Integer itemType;

    @Schema(description = "状态：0-未找回,1-已找回")
    private Integer status;

    @Schema(description = "关键词搜索（标题/描述/特征/地点）")
    private String keyword;

    @Schema(description = "发布者ID")
    private Long publisherId;
}
