package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.BizSpotTicket;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface BizSpotTicketMapper extends BaseMapper<BizSpotTicket> {

    /**
     * 原子扣减库存并累加销量，返回影响行数，0 表示库存不足
     */
    @Update("UPDATE biz_spot_ticket SET stock = stock - #{count}, sales = sales + #{count} " +
            "WHERE id = #{id} AND stock >= #{count}")
    int deductStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 回补库存并回减销量（取消订单/退款）
     */
    @Update("UPDATE biz_spot_ticket SET stock = stock + #{count}, sales = sales - #{count} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("count") Integer count);
}
