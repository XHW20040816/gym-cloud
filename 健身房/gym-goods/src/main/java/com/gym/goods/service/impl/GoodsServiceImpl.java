package com.gym.goods.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.api.seckill.SeckillFeignClient;
import com.gym.common.constant.MQConstants;
import com.gym.common.exception.BizException;
import com.gym.common.result.ResultCode;
import com.gym.entity.goods.Goods;
import com.gym.goods.mapper.GoodsMapper;
import com.gym.goods.message.GoodsSyncMessage;
import com.gym.goods.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private SeckillFeignClient seckillFeignClient;

    /** 乐观锁最大重试次数 */
    private static final int MAX_RETRY = 3;

    @Override
    public IPage<Goods> listPage(Page<Goods> page, Long categoryId, String nameLike, Integer status) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        if (StringUtils.isNotBlank(nameLike)) {
            wrapper.like(Goods::getName, nameLike);
        }
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        return goodsMapper.selectPage(page, wrapper);
    }

    @Override
    public Goods getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateGoods(Goods goods) {
        boolean isNew = (goods.getId() == null);
        boolean result = super.saveOrUpdate(goods);
        if (result) {
            String action = isNew ? "C" : "U";
            GoodsSyncMessage msg = new GoodsSyncMessage(goods.getId(), action);
            rabbitTemplate.convertAndSend(MQConstants.GOODS_EXCHANGE, "", JSON.toJSONString(msg));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeGoods(Long id) {
        boolean result = super.removeById(id);
        if (result) {
            GoodsSyncMessage msg = new GoodsSyncMessage(id, "D");
            rabbitTemplate.convertAndSend(MQConstants.GOODS_EXCHANGE, "", JSON.toJSONString(msg));
            // 级联清理秒杀服务中引用该商品的秒杀商品（失败不阻塞商品删除）
            try {
                seckillFeignClient.deleteGoodsByGoodsId(id);
            } catch (Exception e) {
                log.warn("级联删除秒杀商品失败 goodsId={}", id, e);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long id, Integer count) {
        if (count == null || count <= 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "扣减数量必须大于0");
        }
        int retry = 0;
        while (retry < MAX_RETRY) {
            Goods goods = goodsMapper.selectById(id);
            if (goods == null) {
                throw new BizException("商品不存在");
            }
            // 假设 version 字段存在于表中（如果实体无此字段，需反射或自定义SQL取version）
            // 此处直接通过实体取，若实体未定义version，可用自定义SQL查询
            Integer version = getVersion(goods);
            int rows = goodsMapper.deductStock(id, count, version);
            if (rows > 0) {
                return true;
            }
            retry++;
        }
        // 重试3次后仍失败
        Goods goods = goodsMapper.selectById(id);
        if (goods != null && goods.getStock() < count) {
            throw new BizException(ResultCode.STOCK_NOT_ENOUGH);
        }
        throw new BizException(ResultCode.STOCK_NOT_ENOUGH.getCode(), "扣减库存失败，请稍后重试");
    }

    /**
     * 获取版本号：实体已声明 @Version 字段，直接返回
     */
    private Integer getVersion(Goods goods) {
        return goods.getVersion();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStock(Long id, Integer count) {
        if (count == null || count <= 0) {
            throw new BizException(ResultCode.PARAM_ERROR.getCode(), "增加数量必须大于0");
        }
        int rows = goodsMapper.addStock(id, count);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setStatus(status);
        boolean result = super.updateById(goods);
        if (result) {
            GoodsSyncMessage msg = new GoodsSyncMessage(id, "U");
            rabbitTemplate.convertAndSend(MQConstants.GOODS_EXCHANGE, "", JSON.toJSONString(msg));
        }
        return result;
    }
}
