package com.gym.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.member.MemberCard;
import com.gym.member.mapper.MemberCardMapper;
import com.gym.member.service.MemberCardService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MemberCardServiceImpl extends ServiceImpl<MemberCardMapper, MemberCard> implements MemberCardService {

    @Resource
    private MemberCardMapper memberCardMapper;

    @Override
    public IPage<MemberCard> listPage(Page<MemberCard> page, String nameLike) {
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(nameLike)) {
            wrapper.like(MemberCard::getName, nameLike);
        }
        wrapper.orderByDesc(MemberCard::getPrice);
        return memberCardMapper.selectPage(page, wrapper);
    }
}
