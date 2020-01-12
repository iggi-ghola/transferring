package com.kozhekin.transferring;

import com.kozhekin.transferring.model.MoneyTransactionState;
import com.kozhekin.transferring.modelTest.Account;
import com.kozhekin.transferring.modelTest.MoneyTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class ApplicationTest extends AbstractTest {

    private ApplicationContext applicationContext;
    private ApplicationClient applicationClient;

    @Test
    public void testCreateAccount() {
        final Account a = getClient().createAccount("aaa@bbb.com");
        final Account a1 = getClient().getAccount(a.getId());

        Assert.assertNotNull("Can not find created user", a1);
        Assert.assertEquals("Id is not the same", a.getId(), a1.getId());
        Assert.assertEquals("Email is not the same", a.getUsername(), a1.getUsername());
        Assert.assertEquals("Amount must be 0", BigDecimal.ZERO, a1.getAmount());
    }

    @Test
    public void testGetSystemAccount() {
        final Account a = getClient().getAccount(0);

        Assert.assertNotNull("Can not find System user", a);
        Assert.assertEquals("Id is not 0", 0, a.getId());
        Assert.assertEquals("Email is not 'system'", "system", a.getUsername());
    }


    @Test
    public void testDeposit() {
        final Account a = getClient().createAccount("aaa@bbb.com");
        validateSuccessMoneyTransaction(getClient().deposit(a.getId(), BigDecimal.valueOf(100.)));
        final Account a1 = getClient().getAccount(a.getId());
        Assert.assertEquals("Amount must be 100.00",
                BigDecimal.valueOf(1000, 1),
                a1.getAmount());
        final List<MoneyTransaction> trs = getClient().getTransactions(a1.getId());
        Assert.assertEquals("The must be exact 1 transaction", 1, trs.size());
        final MoneyTransaction t = trs.get(0);
        Assert.assertEquals("Amount must be 100.00", BigDecimal.valueOf(1000, 1), t.getAmount());
        Assert.assertEquals("Source Account Id must be 0", 0L, t.getSrcId());
        Assert.assertEquals("Destination Account Id must be " + a1.getId(), a1.getId(), t.getDstId());
        Assert.assertEquals("State must be SUCCESS", MoneyTransactionState.SUCCESS, t.getState());
    }

    @Test
    public void testTransfer() {
        final Account a1 = getClient().createAccount("aaa@bbb.com");
        final Account a2 = getClient().createAccount("bbb@ccc.net");
        validateSuccessMoneyTransaction(getClient().deposit(a1.getId(), BigDecimal.valueOf(100.)));
        validateSuccessMoneyTransaction(getClient().transfer(a1.getId(), a2.getId(), BigDecimal.valueOf(5000, 2)));

        final List<MoneyTransaction> trs = getClient().getTransactions(a2.getId());
        Assert.assertEquals("The must be exact 1 transaction", 1, trs.size());
        final MoneyTransaction t = trs.get(0);
        Assert.assertEquals("Amount must be 50.00", BigDecimal.valueOf(5000, 2), t.getAmount());
        Assert.assertEquals("Source Account Id must be " + a1.getId(), a1.getId(), t.getSrcId());
        Assert.assertEquals("Destination Account Id must be " + a2.getId(), a2.getId(), t.getDstId());
        Assert.assertEquals("State must be SUCCESS", MoneyTransactionState.SUCCESS, t.getState());

        Assert.assertEquals("Amount must be 50.00",
                BigDecimal.valueOf(5000, 2),
                getClient().getAccount(a1.getId()).getAmount());
        Assert.assertEquals("Amount must be 50.00",
                BigDecimal.valueOf(5000, 2),
                getClient().getAccount(a2.getId()).getAmount());
    }

    @Before
    public void init() {
        applicationContext = new ApplicationContext();
        applicationContext.start();
        applicationClient = ApplicationClientFactory.create();

    }

    @After
    public void destroy() {
        applicationContext.stop();
    }

    public ApplicationClient getClient() {
        return applicationClient;
    }
}