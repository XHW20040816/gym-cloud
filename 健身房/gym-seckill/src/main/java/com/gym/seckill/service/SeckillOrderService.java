package com.gym.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.api.dto.SeckillOrderDTO;
import com.gym.common.result.R;
import com.gym.entity.seckill.SeckillOrder;

import java.math.BigDecimal;

public interface SeckillOrderService extends IService<SeckillOrder> {

    /**
     * 执行秒杀（多级防护：快速售罄→Lua原子扣减→Redisson锁兜底→MQ异步建单）
     *
     * @param userId          用户ID
     * @param seckillGoodsId  秒杀商品ID（t_seckill_goods主键）
     * @return R.ok(seckillGoodsId) 表示排队中，其他为失败
     */
    R<Long> doSeckill(Long userId, Long seckillGoodsId);

    /** 我的秒杀订单分页 */
    IPage<SeckillOrder> pageMy(Long userId, Page<SeckillOrder> page);

    /** 查询用户是否已秒杀某商品成功，返回订单；否则返回null */
    SeckillOrder getByUserAndGoods(Long userId, Long seckillGoodsId);

    /** 秒杀超时取消（死信调用）：状态置为取消 + 回滚Redis库存 */
    void seckillTimeoutCancel(String orderNo);

    /**
     * 异步建单：插入秒杀订单 + DB 扣减库存（同一本地事务，任一步失败整体回滚）
     *
     * @param dto    建单消息
     * @param amount 秒杀价快照
     */
    void createSeckillOrder(SeckillOrderDTO dto, BigDecimal amount);
}
