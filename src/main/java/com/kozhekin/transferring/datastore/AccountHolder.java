package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.model.Account;

import java.util.concurrent.locks.ReentrantReadWriteLock;

class AccountHolder {
    private final Account account;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    AccountHolder(Account account) {
        this.account = account;
    }

    Account getAccount() {
        return account;
    }

    void lockForWrite() {
        lock.writeLock().lock();
    }

    void unlockForWrite() {
        lock.writeLock().unlock();
    }

    int getId() {
        return account.getId();
    }

    Account getCopy() {
        return account.copy();
    }

    Account getCopySync() {
        lock.readLock().lock();
        try {
            return getCopy();
        } finally {
            lock.readLock().unlock();
        }
    }


}
