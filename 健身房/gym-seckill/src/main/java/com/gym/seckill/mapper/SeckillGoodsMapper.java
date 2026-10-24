package com.gym.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.seckill.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    @Update("UPDATE t_seckill_goods SET sold = sold + #{addSold}, stock = stock - #{deductStock} WHERE id = #{id} AND stock >= #{deductStock}")
    int addSoldAndDeductStock(@Param("id") Long id,
                              @Param("addSold") Integer addSold,
                              @Param("deductStock") Integer deductStock);

    default int addSold(Long id, Integer count) {
        return addSoldAndDeductStock(id, count, count);
    }
}
