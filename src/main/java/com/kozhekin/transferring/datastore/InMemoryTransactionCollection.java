package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.model.MoneyTransaction;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class InMemoryTransactionCollection {
    private final ReentrantReadWriteLock transactionsLock = new ReentrantReadWriteLock();
    private final Map<Integer, MoneyTransaction> transactions = new HashMap<>();
    private final Map<Integer, List<Integer>> srcAccountIndex = new HashMap<>();
    private final Map<Integer, List<Integer>> dstAccountIndex = new HashMap<>();
    private final IdGenerator trxIdGenerator = new InMemoryIdGenerator();

    List<MoneyTransaction> getTransactions(int accountId) {
        final List<MoneyTransaction> result = new ArrayList<>();
        transactionsLock.readLock().lock();
        try {
            result.addAll(findInIndex(srcAccountIndex, accountId));
            result.addAll(findInIndex(dstAccountIndex, accountId));
        } finally {
            transactionsLock.readLock().unlock();
        }
        result.sort(Comparator.comparingLong(MoneyTransaction::getId));
        return result;
    }

    List<MoneyTransaction> getAllTransactions() {
        transactionsLock.readLock().lock();
        try {
            return new ArrayList<>(transactions.values());
        } finally {
            transactionsLock.readLock().unlock();
        }
    }

    MoneyTransaction saveTransaction(MoneyTransaction.MoneyTransactionBuilder transactionBuilder) {
        MoneyTransaction trx = enrichAndBuild(transactionBuilder);

        transactionsLock.writeLock().lock();
        try {
            if (transactions.put(trx.getId(), trx) != null) {
                throw new IllegalStateException("Duplicate transaction found on id = " + trx.getId());
            }
            srcAccountIndex.computeIfAbsent(trx.getSrcId(), k -> new ArrayList<>()).add(trx.getId());
            dstAccountIndex.computeIfAbsent(trx.getDstId(), k -> new ArrayList<>()).add(trx.getId());
        } finally {
            transactionsLock.writeLock().unlock();
        }
        return trx;
    }

    private List<MoneyTransaction> findInIndex(Map<Integer, List<Integer>> index, int accountId) {
        return index.getOrDefault(accountId, Collections.emptyList())
                .stream().map(id -> transactions.get(id)).collect(Collectors.toList());
    }

    private MoneyTransaction enrichAndBuild(MoneyTransaction.MoneyTransactionBuilder transactionBuilder) {
        int id = trxIdGenerator.next();
        if (id < 0) {
            throw new IllegalStateException("Storage is full");
        }
        transactionBuilder.setId(id);
        transactionBuilder.setDate(new Timestamp(System.currentTimeMillis()));
        return transactionBuilder.build();
    }

}
