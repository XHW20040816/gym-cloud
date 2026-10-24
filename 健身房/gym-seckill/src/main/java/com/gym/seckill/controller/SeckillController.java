package com.gym.seckill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.common.constant.RedisConstants;
import com.gym.common.result.PageResult;
import com.gym.common.result.R;
import com.gym.common.result.ResultCode;
import com.gym.entity.seckill.SeckillActivity;
import com.gym.entity.seckill.SeckillGoods;
import com.gym.entity.seckill.SeckillOrder;
import com.gym.seckill.service.SeckillActivityService;
import com.gym.seckill.service.SeckillGoodsService;
import com.gym.seckill.service.SeckillOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "秒杀前台-活动/商品/下单/查询")
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource
    private SeckillActivityService seckillActivityService;

    @Resource
    private SeckillGoodsService seckillGoodsService;

    @Resource
    private SeckillOrderService seckillOrderService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ================== 公开接口 ==================

    @ApiOperation("[公开] 进行中的秒杀活动列表")
    @GetMapping("/public/activities")
    public R<List<SeckillActivity>> publicActivities() {
        return R.ok(seckillActivityService.listOngoing());
    }

    @ApiOperation("[公开] 某活动下秒杀商品列表")
    @GetMapping("/public/goods/{activityId}")
    public R<List<SeckillGoods>> publicGoods(@PathVariable Long activityId) {
        return R.ok(seckillGoodsService.listByActivityId(activityId));
    }

    // ================== 用户接口（需登录） ==================

    @ApiOperation("秒杀下单：排队中请稍后调用 /order/result 查询结果")
    @PostMapping("/order")
    public R<Long> order(
            @ApiParam("秒杀商品ID（t_seckill_goods.id）") @RequestParam Long seckillGoodsId,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        return seckillOrderService.doSeckill(userId, seckillGoodsId);
    }

    @ApiOperation("查询秒杀结果：有=成功，无=未命中或售罄")
    @GetMapping("/order/result/{seckillGoodsId}")
    public R<Map<String, Object>> result(
            @PathVariable Long seckillGoodsId,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        SeckillOrder order = seckillOrderService.getByUserAndGoods(userId, seckillGoodsId);
        Map<String, Object> data = new HashMap<>();
        if (order != null) {
            data.put("success", true);
            data.put("order", order);
            return R.ok(data);
        }
        // 无订单：看售罄标记是否存在来返回更准确的信息
        Boolean soldout = stringRedisTemplate.hasKey(RedisConstants.SECKILL_SOLDOUT_KEY + seckillGoodsId);
        data.put("success", false);
        if (Boolean.TRUE.equals(soldout)) {
            data.put("reason", "秒杀已售罄/未命中");
        } else {
            data.put("reason", "排队中/秒杀失败/未命中");
        }
        return R.ok(data);
    }

    @ApiOperation("我的秒杀订单列表（分页）")
    @GetMapping("/order/my")
    public R<PageResult<SeckillOrder>> my(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            return R.fail(ResultCode.UNAUTHORIZED, "未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        Page<SeckillOrder> page = new Page<>(pageNum, pageSize);
        IPage<SeckillOrder> result = seckillOrderService.pageMy(userId, page);
        return R.ok(PageResult.of(result.getRecords(), result.getTotal()));
    }
}
