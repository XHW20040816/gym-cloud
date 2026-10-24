package com.gym.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.goods.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    @Select("SELECT * FROM t_goods WHERE id = #{id}")
    Goods findById(Long id);
}
