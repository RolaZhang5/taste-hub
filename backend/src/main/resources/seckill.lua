---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Rola.
--- DateTime: 28/5/2025 9:43 pm
---
-- 1.parameter list
-- 1.1 voucherId
local voucherId = ARGV[1]
--1.2 userId
local userId = ARGV[2]
-- 1.3 OrderId
local orderId = ARGV[3]

-- 2.parameter key
-- 2.1 stock key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2 order key
local orderKey = 'seckill:order:' .. voucherId

-- 3.script service
-- 3.1 check if the stock is sufficient
if(tonumber(redis.call('get', stockKey)) <= 0) then
    -- 3.2 if not, return 1
    return 1
end
-- 3.3 check if user has submit order
if(redis.call('sismember', orderKey, userId) == 1) then
    -- 3.4 if exists, means order is duplicated, return 2
    return 2
end
-- 3.4 deduct stock incrby stockKey
redis.call('incrby', stockKey, -1)
-- 3.5 submit order sadd orderKey userId
redis.call('sadd', orderKey, userId)
--3.6 Send message to the queue，XADD stream.orders * k1 v1 k2 v2 ...
redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
return 0