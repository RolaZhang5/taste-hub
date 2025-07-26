package com.taste.utils;

public interface ILock {
    /**
     * Try to fetch the lock
     * @param timeoutSec The lock's expiration time in seconds, the lock will be automatically released after expiration
     * @return true: means the lock was acquired sucessfully; false: means the lock was acquired failed
     */
    boolean tryLock(long timeoutSec);

    /**
     * Release the lock
     */
    void unLock();
}
