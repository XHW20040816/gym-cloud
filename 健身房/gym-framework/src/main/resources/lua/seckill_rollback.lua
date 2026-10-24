-- 秒杀回滚库存脚本
-- KEYS[1] stockKey     库存key
-- KEYS[2] userHashKey  用户购买次数hash key
-- KEYS[3] soldoutKey   售罄key
-- ARGV[1] userId      用户ID
-- 返回：1 成功

-- 1) 库存 +1
redis.call('INCR', KEYS[1])

-- 2) 移除售罄标记（有就删，没有也不报错）
redis.call('DEL', KEYS[3])

-- 3) 用户购买次数 -1，若减到0则删除该字段
local remain = redis.call('HINCRBY', KEYS[2], ARGV[1], -1)
if remain <= 0 then
    redis.call('HDEL', KEYS[2], ARGV[1])
end
return 1
