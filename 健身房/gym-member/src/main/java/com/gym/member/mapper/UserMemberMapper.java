package com.gym.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gym.entity.member.UserMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMemberMapper extends BaseMapper<UserMember> {

    @Update("UPDATE t_user_member SET remain_times = remain_times - #{times} WHERE id = #{id} AND remain_times >= #{times}")
    int deductTimes(@Param("id") Long id, @Param("times") Integer times);
}
