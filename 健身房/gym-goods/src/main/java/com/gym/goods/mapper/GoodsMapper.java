package com.gym.goods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.goods.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    @Update("UPDATE t_goods SET stock = stock - #{count}, sales = sales + #{count}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND stock >= #{count}")
    int deductStock(@Param("id") Long id, @Param("count") Integer count, @Param("version") Integer version);

    @Update("UPDATE t_goods SET stock = stock + #{count} WHERE id = #{id}")
    int addStock(@Param("id") Long id, @Param("count") Integer count);
}
