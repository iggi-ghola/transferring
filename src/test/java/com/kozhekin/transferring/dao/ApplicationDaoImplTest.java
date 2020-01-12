package com.kozhekin.transferring.dao;

import com.kozhekin.transferring.AbstractTest;
import com.kozhekin.transferring.ApplicationContext;
import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;
import com.kozhekin.transferring.model.MoneyTransactionState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApplicationDaoImplTest extends AbstractTest {
    private ApplicationContext context;
    private ApplicationDao applicationDao;
    private ExecutorService exec;

    @Before
    public void init() {
        exec = Executors.newFixedThreadPool(threads);
        context = new ApplicationContext();
        applicationDao = context.getApplicationDao();
    }

    @After
    public void destroy() {
        context.stop();
        exec.shutdown();
    }

    @Test
    public void getTransactions() {
        Account a = context.getDatastore().createAccount(Account.builder().setUsername("user"));
        context.getDatastore().saveTransaction(
                MoneyTransaction.builder().setAmount(BigDecimal.TEN).setSrcId(0).setDstId(a.getId())
                        .setState(MoneyTransactionState.SUCCESS));
        Collection<MoneyTransaction> transactions = applicationDao.getTransactions();
        Assert.assertNotNull("Must return non-null", transactions);
        Assert.assertEquals("Must return exact 1 transaction", 1, transactions.size());
        validateSuccessMoneyTransaction(transactions.iterator().next());
    }

    @Test
    public void createAccount() {
        String user = "asdf@asdf.com";
        Account a = applicationDao.createAccount(user);
        Assert.assertNotNull("Must return non-null", a);
        Assert.assertEquals("username must be equal", user, a.getUsername());
    }

    @Test
    public void getAccount() {
        Assert.assertNull("Must return null for negative", applicationDao.getAccount(-1));
        Assert.assertNull("Must return null for wrong id", applicationDao.getAccount(12345));
    }

    @Test
    public void checkSystemAccount() {
        Account system = applicationDao.getAccount(0);
        Assert.assertNotNull("Must return non null", system);
        Assert.assertEquals("SYSTEM", system.getType());
        Assert.assertEquals("system", system.getUsername());
    }

    @Test
    public void depositNegative() {
        // account not exists
        validateErrorMoneyTransaction(applicationDao.deposit(12345, BigDecimal.ONE), BigDecimal.ONE);
        // negative accountId
        validateErrorMoneyTransaction(applicationDao.deposit(-1, BigDecimal.ZERO), BigDecimal.ZERO);
        // amount is 0
        validateErrorMoneyTransaction(applicationDao.deposit(1, BigDecimal.ZERO), BigDecimal.ZERO);
        // amount is negative
        validateErrorMoneyTransaction(applicationDao.deposit(1, BigDecimal.TEN.negate()), BigDecimal.TEN.negate());
        // amount is null
        validateErrorMoneyTransaction(applicationDao.deposit(1, null), null);
        // system account id
        validateErrorMoneyTransaction(applicationDao.deposit(0, BigDecimal.TEN), BigDecimal.TEN);
    }

    @Test
    public void depositPositive() throws InterruptedException, ExecutionException {
        final Account a = context.getDatastore().createAccount(Account.builder().setUsername("2nd"));
        final int size = 1000;
        List<Future<?>> tasks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tasks.add(exec.submit(() -> validateSuccessMoneyTransaction(applicationDao.deposit(a.getId(), BigDecimal.TEN))));
        }
        checkErrors(tasks);
        Account aa = applicationDao.getAccount(a.getId());
        Assert.assertEquals(BigDecimal.valueOf(size * 10), aa.getAmount());
        Assert.assertEquals(size, applicationDao.getTransactions().size());
    }


    @Test
    public void transferNegative() {
        Account a = context.getDatastore().createAccount(Account.builder().setUsername("2nd"));
        Account a1 = context.getDatastore().createAccount(Account.builder().setUsername("3rd"));
        // src account not exists
        validateErrorMoneyTransaction(applicationDao.transfer(12345, a.getId(), BigDecimal.TEN), BigDecimal.TEN);
        // negative src accountId
        validateErrorMoneyTransaction(applicationDao.transfer(-1, a.getId(), BigDecimal.ZERO), BigDecimal.ZERO);
        // amount is 0
        validateErrorMoneyTransaction(applicationDao.transfer(a1.getId(), a.getId(), BigDecimal.ZERO), BigDecimal.ZERO);
        // amount is negative
        validateErrorMoneyTransaction(applicationDao.transfer(a1.getId(), a.getId(), BigDecimal.TEN.negate()), BigDecimal.TEN.negate());
        // amount is null
        validateErrorMoneyTransaction(applicationDao.transfer(a1.getId(), a.getId(), null), null);
        // system account id
        validateErrorMoneyTransaction(applicationDao.transfer(0, a.getId(), BigDecimal.TEN), BigDecimal.TEN);
        // same account src == dst
        validateErrorMoneyTransaction(applicationDao.transfer(a1.getId(), a1.getId(), BigDecimal.TEN), BigDecimal.TEN);
        // not enough money
        validateErrorMoneyTransaction(applicationDao.transfer(a.getId(), a1.getId(), BigDecimal.TEN), BigDecimal.TEN);
        validateErrorMoneyTransaction(applicationDao.transfer(a1.getId(), a.getId(), BigDecimal.TEN), BigDecimal.TEN);
    }

    @Test
    public void transferPositive() throws InterruptedException, ExecutionException {
        final Account a = context.getDatastore().createAccount(Account.builder().setUsername("2nd"));
        final Account a1 = context.getDatastore().createAccount(Account.builder().setUsername("3rd"));
        final int size = 1000;
        validateSuccessMoneyTransaction(applicationDao.deposit(a.getId(), BigDecimal.valueOf(size)));
        List<Future<?>> tasks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tasks.add(exec.submit(() -> validateSuccessMoneyTransaction(applicationDao.transfer(a.getId(), a1.getId(), BigDecimal.ONE))));
        }
        checkErrors(tasks);
        tasks.clear();
        for (int i = 0; i < size; i++) {
            tasks.add(exec.submit(() -> validateSuccessMoneyTransaction(applicationDao.transfer(a1.getId(), a.getId(), BigDecimal.ONE))));
        }
        checkErrors(tasks);
        Account aa = applicationDao.getAccount(a.getId());
        Account aa1 = applicationDao.getAccount(a1.getId());
        Assert.assertEquals(BigDecimal.ZERO, aa1.getAmount());
        Assert.assertEquals(BigDecimal.valueOf(size), aa.getAmount());
        Assert.assertEquals(1 + size + size, applicationDao.getTransactions().size());
    }

    @Test
    public void getAllAccounts() {
        Assert.assertEquals(1, applicationDao.getAllAccounts().size());
        Assert.assertEquals("Not system account", "system", applicationDao.getAllAccounts().iterator().next().getUsername());
    }

    @Test
    public void testGetTransactionsNegative() {
        Assert.assertEquals("Empty list for nonexistent accounts", Collections.emptyList(), applicationDao.getTransactions(12345));
        Assert.assertEquals("Empty list for negative account id", Collections.emptyList(), applicationDao.getTransactions(-1));
    }

    @Test
    public void testGetTransactions() throws InterruptedException, ExecutionException {
        final Account a = context.getDatastore().createAccount(Account.builder().setUsername("2nd"));
        final Account a1 = context.getDatastore().createAccount(Account.builder().setUsername("3rd"));
        validateSuccessMoneyTransaction(applicationDao.deposit(a.getId(), BigDecimal.valueOf(1000)));

        Assert.assertEquals("Must be exact 1 transaction", 1, applicationDao.getTransactions(a.getId()).size());
        Assert.assertEquals("Must be exact 0 transaction", 0, applicationDao.getTransactions(a1.getId()).size());

        List<Future<?>> tasks = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            tasks.add(exec.submit(() -> validateSuccessMoneyTransaction(applicationDao.transfer(a.getId(), a1.getId(), BigDecimal.ONE))));
        }
        checkErrors(tasks);

        Assert.assertEquals("Must be exact 1001 transaction", 1001, applicationDao.getTransactions(a.getId()).size());
        Assert.assertEquals("Must be exact 1000 transaction", 1000, applicationDao.getTransactions(a1.getId()).size());
        applicationDao.getTransactions().forEach(this::validateSuccessMoneyTransaction);
    }
}