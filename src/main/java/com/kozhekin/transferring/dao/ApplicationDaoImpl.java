package com.kozhekin.transferring.dao;

import com.kozhekin.transferring.datastore.Datastore;
import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;
import com.kozhekin.transferring.model.MoneyTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ApplicationDaoImpl implements ApplicationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDaoImpl.class);
    private final Datastore datastore;
    private final Account systemAccount;

    public ApplicationDaoImpl(Datastore datastore) {
        this.datastore = datastore;
        systemAccount = datastore.createAccount(Account.builder().setType("SYSTEM").setUsername("system"));
    }

    @Override
    public List<MoneyTransaction> getTransactions(int accountId) {
        if (accountId < 0) {
            return Collections.emptyList();
        }
        return datastore.getTransactions(accountId);
    }

    @Override
    public Account createAccount(String email) {
        return datastore.createAccount(Account.builder().setUsername(email));
    }

    @Override
    public Account getAccount(int accountId) {
        if (accountId < 0) {
            return null;
        }
        return datastore.getAccount(accountId);
    }

    @Override
    public MoneyTransaction transfer(int srcId, int dstId, BigDecimal amount) {
        return validateTransferParams(srcId, dstId, amount)
                .orElseGet(() -> doTransfer(srcId, dstId, amount));
    }

    @Override
    public MoneyTransaction deposit(int dstId, BigDecimal amount) {
        return validateDepositParams(systemAccount.getId(), dstId, amount)
                .orElseGet(() -> doDeposit(dstId, amount));
    }

    @Override
    public List<Account> getAllAccounts() {
        return datastore.getAccounts();
    }

    @Override
    public Collection<MoneyTransaction> getTransactions() {
        return datastore.getTransactions();
    }

    private MoneyTransaction doTransfer(int srcId, int dstId, BigDecimal amount) {
        MoneyTransaction.MoneyTransactionBuilder transactionBuilder;

        if (srcId < dstId) {
            transactionBuilder = datastore.transferSync(srcId, dstId, amount) ;
        } else {
            transactionBuilder = datastore.transferSync(dstId, srcId, amount.negate()) ;
        }
        transactionBuilder.setSrcId(srcId).setDstId(dstId).setAmount(amount);
        if (transactionBuilder.getState() == MoneyTransactionState.ERROR) {
            return transactionBuilder.build();
        }

        LOGGER.debug("Transferring amount {}, from {} to {}", amount, srcId, dstId);
        return datastore.saveTransaction(transactionBuilder.setState(MoneyTransactionState.SUCCESS));
    }

    private MoneyTransaction doDeposit(int dstId, BigDecimal amount) {

        MoneyTransaction.MoneyTransactionBuilder transactionBuilder = datastore.depositSync(dstId, amount);
        transactionBuilder.setDstId(dstId).setSrcId(systemAccount.getId()).setAmount(amount);

        LOGGER.debug("Deposit amount {}, to {}", amount, dstId);
        return datastore.saveTransaction(transactionBuilder.setState(MoneyTransactionState.SUCCESS));
    }

    private Optional<MoneyTransaction> validateTransferParams(int srcId, int dstId, BigDecimal amount) {
        Optional<MoneyTransaction> result = validateDepositParams(srcId, dstId, amount);
        if (result.isPresent()) {
            return result;
        }
        if (srcId == dstId) {
            return Optional.of(MoneyTransaction.prepareErrorTransactionBuilder(srcId, dstId, amount)
                    .setComment("source id equals destination id").build());
        }
        if (srcId < 0 || srcId == systemAccount.getId()) {
            return Optional.of(MoneyTransaction.prepareErrorTransactionBuilder(srcId, dstId, amount)
                    .setComment("source id does not exist").build());
        }

        if (!datastore.accountExists(srcId)) {
            return Optional.of(MoneyTransaction.prepareErrorTransactionBuilder(srcId, dstId, amount)
                    .setComment("Can not find source Account with id = " + srcId).build());
        }
        return Optional.empty();
    }

    private Optional<MoneyTransaction> validateDepositParams(int srcId, int dstId, BigDecimal amount) {
        if (dstId < 0 || dstId == systemAccount.getId()) {
            return Optional.of(MoneyTransaction.prepareErrorTransactionBuilder(srcId, dstId, amount)
                    .setComment("destination id does not exist").build());
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.of(MoneyTransaction.prepareErrorTransactionBuilder(srcId, dstId, amount)
                    .setComment("amount must be positive").build());
        }
        if (!datastore.accountExists(dstId)) {
            return Optional.of(MoneyTransaction.prepareErrorTransactionBuilder(srcId, dstId, amount)
                    .setComment("Can not find destination Account with id = " + dstId).build());
        }
        return Optional.empty();
    }


}
