package com.kozhekin.transferring.dao;

import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface ApplicationDao {

    List<MoneyTransaction> getTransactions(int accountId);

    Account createAccount(String email);

    Account getAccount(int accountId);

    MoneyTransaction transfer(int srcId, int dstId, BigDecimal amount);

    MoneyTransaction deposit(int dstId, BigDecimal amount);

    List<Account> getAllAccounts();

    Collection<MoneyTransaction> getTransactions();
}
