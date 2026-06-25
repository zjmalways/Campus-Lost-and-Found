package org.zhangjiamin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String contact;
    private String avatar;
    private Integer role;        // 0-普通用户，1-管理员
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;      // 0-正常，1-禁用
}
