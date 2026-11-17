package com.wdd.cat.common.utils;

import org.springframework.stereotype.Component;

/**
 * &#064program: cat
 * <p>
 * &#064description: 雪花 ID 生成器；跨进程传输一律用 nextString()
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-16 15:41
 **/
@Component
public class SnowflakeId {

    /** 起始纪元 2024-01-01T00:00:00Z。 */
    private static final long EPOCH = 1704067200000L;

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /** 允许的时钟回拨上限，超过就拒绝发号。 */
    private static final long MAX_BACKWARD_MS = 3_000L;

    private final long workerId;
    private final long datacenterId;

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    /** 单进程部署，机器号与机房号都取 0。 */
    public SnowflakeId() {
        this(0L, 0L);
    }

    public SnowflakeId(long workerId, long datacenterId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId 必须在 0~" + MAX_WORKER_ID + " 之间");
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException("datacenterId 必须在 0~" + MAX_DATACENTER_ID + " 之间");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /** 生成一个 id。 */
    public synchronized long next() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset > MAX_BACKWARD_MS) {
                throw new IllegalStateException(
                        "时钟回拨 " + offset + "ms，超过上限 " + MAX_BACKWARD_MS + "ms，拒绝发号");
            }
            timestamp = waitUntil(lastTimestamp);
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitUntil(lastTimestamp + 1);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /** 生成 id 并转字符串；跨进程传输用这个，避免 JS 丢精度。 */
    public String nextString() {
        return Long.toString(next());
    }

    private static long waitUntil(long target) {
        long current = System.currentTimeMillis();
        while (current < target) {
            Thread.onSpinWait();
            current = System.currentTimeMillis();
        }
        return current;
    }
}
