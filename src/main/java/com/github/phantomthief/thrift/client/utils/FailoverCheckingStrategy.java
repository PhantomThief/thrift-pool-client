/**
 * 
 */
package com.github.phantomthief.thrift.client.utils;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.EvictingQueue.create;
import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.EvictingQueue;

/**
 * <p>
 * FailoverCheckingStrategy class.
 * </p>
 *
 * @author w.vela
 * @version $Id: $Id
 */
public class FailoverCheckingStrategy<T> {

    private static final int DEFAULT_FAIL_COUNT = 10;
    private static final long DEFAULT_FAIL_DURATION = MINUTES.toMillis(1);
    private static final long DEFAULT_RECOVERY_DURATION = MINUTES.toMillis(3);
    private final Logger logger = getLogger(getClass());
    private final long failDuration;

    private final Cache<T, Boolean> failedList;

    private final LoadingCache<T, EvictingQueue<Long>> failCountMap;

    /**
     * <p>
     * Constructor for FailoverCheckingStrategy.
     * </p>
     */
    public FailoverCheckingStrategy() {
        this(DEFAULT_FAIL_COUNT, DEFAULT_FAIL_DURATION, DEFAULT_RECOVERY_DURATION);
    }

    /**
     * <p>
     * Constructor for FailoverCheckingStrategy.
     * </p>
     *
     * @param failDuration a long.
     * @param recoveryDuration a long.
     */
    public FailoverCheckingStrategy(int failCount, long failDuration, long recoveryDuration) {
        this.failDuration = failDuration;
        this.failedList = newBuilder().weakKeys().expireAfterWrite(recoveryDuration, MILLISECONDS)
                .build();
        this.failCountMap = newBuilder().weakKeys().build(
                new CacheLoader<T, EvictingQueue<Long>>() {

                    @Override
                    public EvictingQueue<Long> load(T key) {
                        return create(failCount);
                    }
                });
    }

    /**
     * <p>
     * getFailed.
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<T> getFailed() {
        return failedList.asMap().keySet();
    }

    /**
     * <p>
     * fail.
     * </p>
     *
     * @param object a T object.
     */
    public void fail(T object) {
        logger.trace("server {} failed.", object);
        boolean addToFail = false;
        try {
            EvictingQueue<Long> evictingQueue = failCountMap.get(object);
            synchronized (evictingQueue) {
                evictingQueue.add(System.currentTimeMillis());
                if (evictingQueue.remainingCapacity() == 0
                        && evictingQueue.element() >= System.currentTimeMillis() - failDuration) {
                    addToFail = true;
                }
            }
        } catch (ExecutionException e) {
            logger.error("Ops.", e);
        }
        if (addToFail) {
            failedList.put(object, TRUE);
            logger.trace("server {} failed. add to fail list.", object);
        }
    }
}
