/**
 * 
 */
package me.vela.thrift.client.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.EvictingQueue;

/**
 * @author w.vela
 */
public class FailoverCheckingStrategy<T> {

    private static final int DEFAULT_FAIL_COUNT = 10;

    private static final long DEFAULT_FAIL_DURATION = TimeUnit.MINUTES.toMillis(1);

    private static final long DEFAULT_RECOVERY_DURATION = TimeUnit.MINUTES.toMillis(3);

    private final int failCount;

    private final long failDuration;

    private final Cache<T, Boolean> failedList;

    private final ConcurrentMap<T, EvictingQueue<Long>> failCountMap = new ConcurrentHashMap<>();

    public FailoverCheckingStrategy() {
        this(DEFAULT_FAIL_COUNT, DEFAULT_FAIL_DURATION, DEFAULT_RECOVERY_DURATION);
    }

    /**
     * @param failCount
     * @param failDuration
     * @param recoveryDuration
     */
    public FailoverCheckingStrategy(int failCount, long failDuration, long recoveryDuration) {
        this.failCount = failCount;
        this.failDuration = failDuration;
        this.failedList = CacheBuilder.newBuilder()
                .expireAfterWrite(recoveryDuration, TimeUnit.MILLISECONDS).build();
    }

    public Set<T> getFailed() {
        return failedList.asMap().keySet();
    }

    public void fail(T object) {
        EvictingQueue<Long> evictingQueue = failCountMap.computeIfAbsent(object,
                o -> EvictingQueue.create(failCount));
        boolean addToFail = false;
        synchronized (evictingQueue) {
            evictingQueue.add(System.currentTimeMillis());
            if (evictingQueue.size() == failCount
                    && evictingQueue.element() >= System.currentTimeMillis() - failDuration) {
                addToFail = true;
            }
        }
        if (addToFail) {
            failedList.put(object, Boolean.TRUE);
        }
    }
}
