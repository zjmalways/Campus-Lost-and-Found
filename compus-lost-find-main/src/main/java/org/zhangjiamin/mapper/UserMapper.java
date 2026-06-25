package org.zhangjiamin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.zhangjiamin.entity.User;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    User findById(Long userId);
    int insert(User user);
    int update(User user);
    int updatePassword(User user);
}
