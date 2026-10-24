package com.gym.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.member.UserMember;

import java.util.List;

public interface UserMemberService extends IService<UserMember> {

    /**
     * 普通API开通会员（不跨服务扣余额，直接保存）
     */
    boolean openCard(Long userId, Long cardId);

    /**
     * 内部Feign调用：保存用户会员记录（不扣余额，由支付服务通过Seata全局事务控制）
     */
    boolean saveUserMember(Long userId, Long cardId);

    /**
     * 扣减次卡次数（原子SQL）
     */
    boolean deductTimes(Long userMemberId, Integer times);

    /**
     * 查询用户持有的所有会员卡
     */
    List<UserMember> listByUserId(Long userId);

    IPage<UserMember> listPage(Page<UserMember> page, Long userId, Integer status);
}
