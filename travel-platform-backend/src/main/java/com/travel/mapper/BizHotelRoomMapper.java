package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.BizHotelRoom;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface BizHotelRoomMapper extends BaseMapper<BizHotelRoom> {

    /**
     * 原子扣减库存（乐观锁），返回影响行数，0 表示库存不足
     */
    @Update("UPDATE biz_hotel_room SET remaining_count = remaining_count - #{count} " +
            "WHERE id = #{id} AND remaining_count >= #{count}")
    int deductStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 回补库存（取消订单/退款）
     */
    @Update("UPDATE biz_hotel_room SET remaining_count = remaining_count + #{count} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("count") Integer count);
}
