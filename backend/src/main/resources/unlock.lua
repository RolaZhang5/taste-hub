-- Get the thread's ID get key
local id = redis.call('get', KEYS[1])
-- Compare the thread ID with the lock ID
if(id == ARGV[1]) then
    -- Release the lock, del key
    return redis.call('del', id)
end
return 0