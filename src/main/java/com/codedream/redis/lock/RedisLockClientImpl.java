
package com.codedream.redis.lock;


import com.codedream.redis.function.CheckedCallBack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * 锁客户端
 *
 * @author yxz
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLockClientImpl implements RedisLockClient {
    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = getLock(lockName, lockType);
        return lock.tryLock(waitTime, leaseTime, timeUnit);
    }

    @Override
    public void unLock(String lockName, LockType lockType) {
        RLock lock = getLock(lockName, lockType);
        lock.unlock();
    }

    private RLock getLock(String lockName, LockType lockType) {
        RLock lock;
        if (LockType.REENTRANT == lockType) {
            lock = redissonClient.getLock(lockName);
        } else {
            lock = redissonClient.getFairLock(lockName);
        }
        return lock;
    }

    @Override
    public <T> T lock(String lockName, LockType lockType, long waitTime, long leaseTime, TimeUnit timeUnit, CheckedCallBack<T> supplier) {
        try {
            boolean result = this.tryLock(lockName, lockType, waitTime, leaseTime, timeUnit);
            if (result) {
                return supplier.get();
            }
        } catch (Throwable e) {
            if (e instanceof Error) {
                throw (Error) e;
            } else if (e instanceof IllegalAccessException ||
                    e instanceof IllegalArgumentException ||
                    e instanceof NoSuchMethodException) {
                throw new IllegalArgumentException(e);
            } else if (e instanceof InvocationTargetException) {
                throw new RuntimeException(((InvocationTargetException) e).getTargetException());
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException(e);
        } finally {
            this.unLock(lockName, lockType);
        }
        return null;
    }

}
