package com.zhangjiaming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码（盐值+哈希，不返回给前端）")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "角色：0-普通用户，1-管理员")
    private Integer role;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "状态：0-正常，1-禁用")
    private Integer status;
}
