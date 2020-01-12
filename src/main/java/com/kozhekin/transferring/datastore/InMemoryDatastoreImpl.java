package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public class InMemoryDatastoreImpl implements Datastore {
    private InMemoryTransactionCollection trxCollection;
    private InMemoryAccountCollection accountCollection;

    @Override
    public void start() {
        trxCollection = new InMemoryTransactionCollection();
        accountCollection = new InMemoryAccountCollection();
    }

    @Override
    public void stop() {
        trxCollection = null;
        accountCollection = null;
    }

    @Override
    public List<MoneyTransaction> getTransactions(int accountId) {
        return trxCollection.getTransactions(accountId);
    }

    @Override
    public Collection<MoneyTransaction> getTransactions() {
        return trxCollection.getAllTransactions();
    }

    @Override
    public MoneyTransaction saveTransaction(MoneyTransaction.MoneyTransactionBuilder transactionBuilder) {
        return trxCollection.saveTransaction(transactionBuilder);
    }

    @Override
    public Account createAccount(Account.AccountBuilder accountBuilder) {
        return accountCollection.createAccount(accountBuilder);
    }

    @Override
    public Account getAccount(int accountId) {
        return accountCollection.getAccountCopy(accountId);
    }

    @Override
    public boolean accountExists(int accountId) {
        return accountCollection.accountExists(accountId);
    }

    @Override
    public List<Account> getAccounts() {
        return accountCollection.getAccountCopies();
    }

    @Override
    public MoneyTransaction.MoneyTransactionBuilder transferSync(int srcId, int dstId, BigDecimal amount) {
        return accountCollection.transferSync(srcId, dstId, amount);
    }

    @Override
    public MoneyTransaction.MoneyTransactionBuilder depositSync(int dstId, BigDecimal amount) {
        return accountCollection.depositSync(dstId, amount);

    }


}
