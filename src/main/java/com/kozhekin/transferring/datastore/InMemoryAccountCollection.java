package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;
import com.kozhekin.transferring.model.MoneyTransactionState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryAccountCollection {

    private final Map<Integer, AccountHolder> accounts = new ConcurrentHashMap<>();
    private final IdGenerator accountIdGenerator = new InMemoryIdGenerator();

    public InMemoryAccountCollection() {
    }

    public Account createAccount(Account.AccountBuilder accountBuilder) {
        AccountHolder accountHolder = new AccountHolder(enrichAndBuild(accountBuilder));
        if (accounts.put(accountHolder.getId(), accountHolder) != null) {
            throw new IllegalStateException("Duplicate account found for id = " + accountHolder.getId());
        }
        return accountHolder.getCopySync();
    }

    public Account getAccountCopy(int accountId) {
        AccountHolder holder = accounts.get(accountId);
        if (holder == null) {
            return null;
        }
        return holder.getCopySync();
    }

    public boolean accountExists(int accountId) {
        return accounts.containsKey(accountId);
    }

    public List<Account> getAccountCopies() {
        Collection<AccountHolder> originals = accounts.values();
        List<Account> copies = new ArrayList<>(originals.size());
        originals.forEach(h -> copies.add(h.getCopySync()));
        return copies;
    }

    public MoneyTransaction.MoneyTransactionBuilder transferSync(int firstId, int secondId, BigDecimal amount) {
        AccountHolder firstHolder = accounts.get(firstId);
        AccountHolder secondHolder = accounts.get(secondId);
        firstHolder.lockForWrite();
        try {
            Account first = firstHolder.getAccount();
            if (amount.signum() >= 0 && first.getAmount().compareTo(amount) < 0) {
                return MoneyTransaction.builder().setState(MoneyTransactionState.ERROR)
                        .setComment("Not enough money on account id = " + firstId);
            }
            secondHolder.lockForWrite();
            try {
                Account second = secondHolder.getAccount();
                if (amount.signum() < 0 && second.getAmount().compareTo(amount.negate()) < 0) {
                    return MoneyTransaction.builder().setState(MoneyTransactionState.ERROR)
                            .setComment("Not enough money on account id = " + secondId);
                }
                first.setAmount(first.getAmount().subtract(amount));
                second.setAmount(second.getAmount().add(amount));
            } finally {
                secondHolder.unlockForWrite();
            }
        } finally {
            firstHolder.unlockForWrite();
        }
        return MoneyTransaction.builder().setState(MoneyTransactionState.SUCCESS);
    }

    public MoneyTransaction.MoneyTransactionBuilder depositSync(int dstId, BigDecimal amount) {
        AccountHolder dstHolder = accounts.get(dstId);
        dstHolder.lockForWrite();
        try {
            Account dst = dstHolder.getAccount();
            if (amount.signum() < 0 && dst.getAmount().compareTo(amount.negate()) < 0) {
                return MoneyTransaction.builder().setState(MoneyTransactionState.ERROR)
                        .setComment("Not enough money on account id = " + dst);
            }
            dst.setAmount(dst.getAmount().add(amount));
        } finally {
            dstHolder.unlockForWrite();
        }
        return MoneyTransaction.builder().setState(MoneyTransactionState.SUCCESS);
    }

    private Account enrichAndBuild(Account.AccountBuilder accountBuilder) {
        int id = accountIdGenerator.next();
        if (id < 0) {
            throw new IllegalStateException("Storage is full");
        }
        accountBuilder.setId(id);
        return accountBuilder.build();
    }

}
