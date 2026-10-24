package com.gym.api.seckill;

import com.gym.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 秒杀服务内部 Feign 客户端。
 */
@FeignClient("gym-seckill")
public interface SeckillFeignClient {

    /** 按商品ID删除其关联的秒杀商品（商品被删除时级联清理） */
    @DeleteMapping("/seckill/internal/goods/{goodsId}")
    R<Void> deleteGoodsByGoodsId(@PathVariable("goodsId") Long goodsId);
}
