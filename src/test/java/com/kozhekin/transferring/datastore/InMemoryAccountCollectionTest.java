package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.AbstractTest;
import com.kozhekin.transferring.model.Account;
import com.kozhekin.transferring.model.MoneyTransaction;
import com.kozhekin.transferring.model.MoneyTransactionState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAccountCollectionTest extends AbstractTest {

    private InMemoryAccountCollection accountCollection;
    private Account.AccountBuilder accountBuilder;

    @Before
    public void init() {
        accountCollection = new InMemoryAccountCollection();
        accountBuilder = Account.builder().setUsername("aaa@bbb.com")
                .setAmount(BigDecimal.valueOf(123.45)).setType("SUPER_TYPE");
    }

    @Test
    public void createAccount() {
        Account a = accountCollection.createAccount(accountBuilder);
        Assert.assertNotNull("Account must not be null", a);
        Assert.assertTrue("Account Id must exist", accountCollection.accountExists(a.getId()));
        Assert.assertEquals("Username is wrong", accountBuilder.getUsername(), a.getUsername());
        Assert.assertEquals("Amount is wrong", accountBuilder.getAmount(), a.getAmount());
        Assert.assertEquals("Type is wrong", accountBuilder.getType(), a.getType());
    }

    @Test
    public void getAccountCopy() {
        Account a = accountCollection.createAccount(accountBuilder);
        Account aa = accountCollection.getAccountCopy(a.getId());
        ensureEquals(a, aa);
    }

    @Test
    public void accountExists() {
        Account a = accountCollection.createAccount(accountBuilder);
        Assert.assertNotNull("Account must not be null", a);
        Assert.assertTrue("Account Id must exist", accountCollection.accountExists(a.getId()));
    }

    @Test
    public void getAccountCopies() {
        int size = 10;
        List<Account> created = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            created.add(accountCollection.createAccount(accountBuilder));
        }
        List<Account> copies = accountCollection.getAccountCopies();
        Assert.assertEquals("Wrong amount of created accounts", size, created.size());
        Assert.assertEquals("Wrong amount of account copies", size, copies.size());
        for (int i = 0; i < size; ++i) {
            ensureEquals(created.get(i), copies.get(i));
        }
    }

    @Test
    public void transferSyncPositiveAmountPositive() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.valueOf(100)));
        Account b = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.valueOf(100)));
        // positive amount
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.transferSync(a.getId(), b.getId(), BigDecimal.ONE);
        Assert.assertEquals("Must be success", MoneyTransactionState.SUCCESS, trxBuilder.getState());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Account bb = accountCollection.getAccountCopy(b.getId());
        Assert.assertEquals("Sum is wrong", BigDecimal.valueOf(99), aa.getAmount());
        Assert.assertEquals("Sum is wrong", BigDecimal.valueOf(101), bb.getAmount());
    }

    @Test
    public void transferSyncPositiveAmountNegative() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.valueOf(100)));
        Account b = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.valueOf(100)));
        // negative amount
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.transferSync(a.getId(), b.getId(), BigDecimal.ONE.negate());
        Assert.assertEquals("Must be success", MoneyTransactionState.SUCCESS, trxBuilder.getState());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Account bb = accountCollection.getAccountCopy(b.getId());
        Assert.assertEquals("Sum is wrong", BigDecimal.valueOf(101), aa.getAmount());
        Assert.assertEquals("Sum is wrong", BigDecimal.valueOf(99), bb.getAmount());
    }

    @Test
    public void transferSyncNegativeAmountPositive() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.ZERO));
        Account b = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.ZERO));
        // positive amount
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.transferSync(a.getId(), b.getId(), BigDecimal.ONE);
        Assert.assertEquals("Must be error", MoneyTransactionState.ERROR, trxBuilder.getState());
        Assert.assertNotNull("Comment must be presented in case of error", trxBuilder.getComment());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Account bb = accountCollection.getAccountCopy(b.getId());
        Assert.assertEquals("Sum is wrong", BigDecimal.ZERO, aa.getAmount());
        Assert.assertEquals("Sum is wrong", BigDecimal.ZERO, bb.getAmount());
    }

    @Test
    public void transferSyncNegativeAmountNegative() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.ZERO));
        Account b = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.ZERO));
        // negative amount
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.transferSync(a.getId(), b.getId(), BigDecimal.ONE.negate());
        Assert.assertEquals("Must be error", MoneyTransactionState.ERROR, trxBuilder.getState());
        Assert.assertNotNull("Comment must be presented in case of error", trxBuilder.getComment());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Account bb = accountCollection.getAccountCopy(b.getId());
        Assert.assertEquals("Sum is wrong", BigDecimal.ZERO, aa.getAmount());
        Assert.assertEquals("Sum is wrong", BigDecimal.ZERO, bb.getAmount());
    }

    @Test
    public void depositSyncPositiveAmountPositive() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.ZERO));
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.depositSync(a.getId(), BigDecimal.TEN);
        Assert.assertEquals("Must be success", MoneyTransactionState.SUCCESS, trxBuilder.getState());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Assert.assertEquals("Amount is wrong", BigDecimal.TEN, aa.getAmount());
    }

    @Test
    public void depositSyncPositiveAmountNegative() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.TEN));
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.depositSync(a.getId(), BigDecimal.TEN.negate());
        Assert.assertEquals("Must be success", MoneyTransactionState.SUCCESS, trxBuilder.getState());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Assert.assertEquals("Amount is wrong", BigDecimal.ZERO, aa.getAmount());
    }

    @Test
    public void depositSyncNegativeAmountNegative() {
        Account a = accountCollection.createAccount(accountBuilder.setAmount(BigDecimal.ZERO));
        MoneyTransaction.MoneyTransactionBuilder trxBuilder = accountCollection.depositSync(a.getId(), BigDecimal.TEN.negate());
        Assert.assertEquals("Must be error", MoneyTransactionState.ERROR, trxBuilder.getState());
        Assert.assertNotNull("Comment must be presented in case of error", trxBuilder.getComment());
        Account aa = accountCollection.getAccountCopy(a.getId());
        Assert.assertEquals("Amount is wrong", BigDecimal.ZERO, aa.getAmount());
    }

    private void ensureEquals(final Account a, final Account aa) {
        Assert.assertNotNull("Account must not be null", a);
        Assert.assertNotNull("Account must not be null", aa);
        Assert.assertEquals("Username is wrong", a.getUsername(), aa.getUsername());
        Assert.assertEquals("Amount is wrong", a.getAmount(), aa.getAmount());
        Assert.assertEquals("Type is wrong", a.getType(), aa.getType());
    }
}