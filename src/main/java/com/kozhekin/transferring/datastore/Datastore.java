package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface Datastore {

    void start();

    void stop();

    List<MoneyTransaction> getTransactions(int accountId);

    Account createAccount(Account.AccountBuilder accountBuilder);

    Account getAccount(int accountId);

    boolean accountExists(int accountId);

    List<Account> getAccounts();

    Collection<MoneyTransaction> getTransactions();

    MoneyTransaction saveTransaction(MoneyTransaction.MoneyTransactionBuilder transactionBuilder);

    MoneyTransaction.MoneyTransactionBuilder transferSync(int srcId, int dstId, BigDecimal amount);

    MoneyTransaction.MoneyTransactionBuilder depositSync(int dstId, BigDecimal amount);
}
