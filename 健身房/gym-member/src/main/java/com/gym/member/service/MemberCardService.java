package com.gym.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.member.MemberCard;

public interface MemberCardService extends IService<MemberCard> {

    IPage<MemberCard> listPage(Page<MemberCard> page, String nameLike);
}
