-- 秒杀扣减库存脚本
-- KEYS[1] stockKey     库存key  seckill:stock:{seckillGoodsId}
-- KEYS[2] userHashKey  用户购买次数hash key  seckill:user:{seckillGoodsId}
-- KEYS[3] soldoutKey   售罄key  seckill:soldout:{seckillGoodsId}
-- ARGV[1] userId      用户ID
-- ARGV[2] limitCount  每人限购数量
-- 返回：
--    1：扣减成功
--   -1：已售罄
--   -2：超出每人限购数量

-- 1) 检查售罄标记
if redis.call('EXISTS', KEYS[3]) == 1 then
    return -1
end

-- 2) 检查用户购买次数是否已达限购上限
local limitCount = tonumber(ARGV[2])
local bought = tonumber(redis.call('HGET', KEYS[2], ARGV[1]) or 0)
if bought >= limitCount then
    return -2
end

-- 3) 读取当前库存
local stock = tonumber(redis.call('GET', KEYS[1]) or 0)

-- 4) 库存不足则设置售罄标记并返回
if stock <= 0 then
    redis.call('SET', KEYS[3], '1')
    return -1
end

-- 5) 如果扣减后库存为0，写入售罄标记
if stock - 1 == 0 then
    redis.call('SET', KEYS[3], '1')
end

-- 6) 扣减库存并累计用户购买次数
redis.call('DECR', KEYS[1])
redis.call('HINCRBY', KEYS[2], ARGV[1], 1)
return 1
