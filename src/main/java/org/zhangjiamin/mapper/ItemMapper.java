package org.zhangjiamin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.zhangjiamin.entity.Item;
import java.util.List;

@Mapper
public interface ItemMapper {
    Item findById(Long itemId);
    int insert(Item item);
    int update(Item item);
    int deleteById(Long itemId);

    List<Item> findPage(@Param("publishType") Integer publishType,
                        @Param("itemType") Integer itemType,
                        @Param("status") Integer status,
                        @Param("keyword") String keyword,
                        @Param("publisherId") Long publisherId);

    List<Item> findByPublisherId(Long publisherId);

    int incrementViewCount(Long itemId);
    int incrementCollectCount(Long itemId);
    int incrementCommentCount(Long itemId);
}
