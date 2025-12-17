-- ShedLock 分布式锁表
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);

COMMENT ON TABLE shedlock IS 'ShedLock分布式定时任务锁表';
COMMENT ON COLUMN shedlock.name IS '锁名称（唯一标识）';
COMMENT ON COLUMN shedlock.lock_until IS '锁持有时间上限';
COMMENT ON COLUMN shedlock.locked_at IS '锁获取时间';
COMMENT ON COLUMN shedlock.locked_by IS '锁持有者标识';
