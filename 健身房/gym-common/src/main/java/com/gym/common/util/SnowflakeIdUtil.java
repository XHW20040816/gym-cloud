package com.gym.common.util;

import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdUtil {

    /** 开始时间截 (2024-01-01) */
    private static final long START_TIMESTAMP = 1704067200000L;

    /** 机器id所占的位数 */
    private static final long WORKER_ID_BITS = 5L;

    /** 数据标识id所占的位数 */
    private static final long DATACENTER_ID_BITS = 5L;

    /** 序列在id中占的位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器ID向左移12位 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 数据标识id向左移17位(12+5) */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 时间截向左移22位(5+5+12) */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /** 生成序列的掩码 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 工作机器ID(0~31) */
    private final long workerId;

    /** 数据中心ID(0~31) */
    private final long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    public SnowflakeIdUtil() {
        this(1L, 1L);
    }

    public SnowflakeIdUtil(long workerId, long datacenterId) {
        long maxWorkerId = ~(-1L << WORKER_ID_BITS);
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("workerId 不能大于 " + maxWorkerId + " 或小于 0");
        }
        long maxDatacenterId = ~(-1L << DATACENTER_ID_BITS);
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 不能大于 " + maxDatacenterId + " 或小于 0");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获取下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID。回拨毫秒数：" + (lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }
}
