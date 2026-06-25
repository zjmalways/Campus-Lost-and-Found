package org.zhangjiamin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.zhangjiamin.entity.Comment;
import java.util.List;

@Mapper
public interface CommentMapper {
    Comment findById(Long commentId);
    int insert(Comment comment);
    int deleteById(Long commentId);
    int deleteByItemId(Long itemId);
    List<Comment> findByItemId(Long itemId);
    List<Comment> findByUserId(Long userId);
    int markAsRead(Long commentId);
    int countByItemId(Long itemId);
}
