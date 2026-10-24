package com.gym.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.common.exception.BizException;
import com.gym.entity.enums.CardType;
import com.gym.entity.member.MemberCard;
import com.gym.entity.member.UserMember;
import com.gym.member.mapper.MemberCardMapper;
import com.gym.member.mapper.UserMemberMapper;
import com.gym.member.service.MemberCardService;
import com.gym.member.service.UserMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserMemberServiceImpl extends ServiceImpl<UserMemberMapper, UserMember> implements UserMemberService {

    @Resource
    private UserMemberMapper userMemberMapper;

    @Resource
    private MemberCardMapper memberCardMapper;

    @Resource
    private MemberCardService memberCardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean openCard(Long userId, Long cardId) {
        // 普通API场景：直接保存，不扣余额
        return saveUserMember(userId, cardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserMember(Long userId, Long cardId) {
        if (userId == null || cardId == null) {
            throw new BizException("参数错误：userId和cardId不能为空");
        }
        MemberCard card = memberCardMapper.selectById(cardId);
        if (card == null) {
            throw new BizException("会员卡不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime;
        Integer remainTimes = 0;

        CardType type = card.getType();
        if (type == CardType.TIMES) {
            // 次卡：有效期默认1年，剩余次数=次卡次数
            endTime = now.plusYears(1);
            remainTimes = card.getTimes() != null ? card.getTimes() : 0;
        } else {
            // 月卡/季卡/年卡：days 字段为天数
            int days = card.getDays() != null ? card.getDays() : type.getCode() * 30;
            if (days <= 0) {
                days = type.getCode() * 30;
            }
            endTime = now.plusDays(days);
            remainTimes = 0; // 非次卡不限制次数
        }

        UserMember userMember = new UserMember();
        userMember.setUserId(userId);
        userMember.setCardId(cardId);
        userMember.setStartTime(now);
        userMember.setEndTime(endTime);
        userMember.setRemainTimes(remainTimes);
        userMember.setStatus(UserMember.STATUS_NORMAL);
        return userMemberMapper.insert(userMember) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductTimes(Long userMemberId, Integer times) {
        if (times == null || times <= 0) {
            throw new BizException("扣减次数必须大于0");
        }
        int rows = userMemberMapper.deductTimes(userMemberId, times);
        if (rows == 0) {
            throw new BizException("剩余次数不足");
        }
        return true;
    }

    @Override
    public List<UserMember> listByUserId(Long userId) {
        LambdaQueryWrapper<UserMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMember::getUserId, userId);
        wrapper.orderByDesc(UserMember::getStartTime);
        return userMemberMapper.selectList(wrapper);
    }

    @Override
    public IPage<UserMember> listPage(Page<UserMember> page, Long userId, Integer status) {
        LambdaQueryWrapper<UserMember> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(UserMember::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(UserMember::getStatus, status);
        }
        wrapper.orderByDesc(UserMember::getStartTime);
        return userMemberMapper.selectPage(page, wrapper);
    }
}
