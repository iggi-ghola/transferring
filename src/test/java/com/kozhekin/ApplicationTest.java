package com.kozhekin;

import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import com.kozhekin.model.TransactionState;
import com.kozhekin.model.User;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ApplicationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationTest.class);
//TODO Should add negative scenarios

    private Launcher launcher;
    private ApplicationClient applicationClient;


    @Test
    public void testCreateUser() {
        final User u = applicationClient.createUser("aaa@bbb.com", "123456789");
        final List<User> ul = applicationClient.getUsers(u.getId());

        Assert.assertEquals("Can not find created user", 1, ul.size());
        Assert.assertEquals("UserId is not the same", u.getId(), ul.get(0).getId());
        Assert.assertEquals("Email is not the same", u.getEmail(), ul.get(0).getEmail());
        Assert.assertEquals("Phone is not the same", u.getPhone(), ul.get(0).getPhone());
        Assert.assertTrue("New User has no accounts", ul.get(0).getAccounts().isEmpty());
        Assert.assertTrue("New User has no accounts", u.getAccounts().isEmpty());
    }

    @Test
    public void testCreateAccount() {
        final User u = applicationClient.createUser("aaa@bbb.com", "123456789");
        final Account a = applicationClient.createAccount(u.getId(), null);
        final User u1 = applicationClient.getUsers(u.getId()).get(0);
        Assert.assertTrue("User must have exact 1 Account", u1.getAccounts().size() == 1);
        final Account a1 = u1.getAccounts().get(0);
        Assert.assertEquals("AccountId is not the same", a.getId(), a1.getId());
        Assert.assertEquals("Amount must be 0", BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_UP), a1.getAmount());
    }

    @Test
    public void testGetSystemUser() {
        final List<User> ul = applicationClient.getUsers(0);

        Assert.assertEquals("Can not find System user", 1, ul.size());
        Assert.assertEquals("UserId is not 0", 0, ul.get(0).getId());
        Assert.assertEquals("Email is not 'system'", "system", ul.get(0).getEmail());
        Assert.assertEquals("Phone is not 'system'", "system", ul.get(0).getPhone());
        Assert.assertTrue("New User has one account", ul.get(0).getAccounts().size() == 1);

        final Account sys = ul.get(0).getAccounts().get(0);
        Assert.assertEquals("AccountId is not 0", 0, sys.getId());
    }


    @Test
    public void testDeposit() {
        final User u = applicationClient.createUser("aaa@bbb.com", "123456789");
        final Account a = applicationClient.createAccount(u.getId(), null);
        applicationClient.deposit(a.getId(), BigDecimal.valueOf(100.));
        final List<User> ul = applicationClient.getUsers(u.getId());
        final Account a1 = ul.get(0).getAccounts().get(0);
        Assert.assertEquals("Amount must be 100.00",
                BigDecimal.valueOf(10000, 2),
                a1.getAmount());
        final List<Transaction> trs = applicationClient.getTransactions(a1.getId());
        Assert.assertEquals("The must be exact 1 transaction", 1, trs.size());
        final Transaction t = trs.get(0);
        Assert.assertEquals("Amount must be 100.00", BigDecimal.valueOf(10000, 2), t.getAmount());
        Assert.assertEquals("Source Account Id must be 0", 0L, t.getSrcId());
        Assert.assertEquals("Destination Account Id must be " + a1.getId(), a1.getId(), t.getDstId());
        Assert.assertEquals("State must be SUCCESS", TransactionState.SUCCESS, t.getState());
    }

    @Test
    public void testTransfer() {
        final User u = applicationClient.createUser("aaa@bbb.com", "123456789");
        final Account a1 = applicationClient.createAccount(u.getId(), null);
        final Account a2 = applicationClient.createAccount(u.getId(), null);
        applicationClient.deposit(a1.getId(), BigDecimal.valueOf(100.));
        applicationClient.transfer(a1.getId(), a2.getId(), BigDecimal.valueOf(5000, 2));

        final List<Transaction> trs = applicationClient.getTransactions(a2.getId());
        Assert.assertEquals("The must be exact 1 transaction", 1, trs.size());
        final Transaction t = trs.get(0);
        Assert.assertEquals("Amount must be 50.00", BigDecimal.valueOf(5000, 2), t.getAmount());
        Assert.assertEquals("Source Account Id must be " + a1.getId(), a1.getId(), t.getSrcId());
        Assert.assertEquals("Destination Account Id must be " + a2.getId(), a2.getId(), t.getDstId());
        Assert.assertEquals("State must be SUCCESS", TransactionState.SUCCESS, t.getState());

        final User u1 = applicationClient.getUsers(u.getId()).get(0);
        Assert.assertEquals("Amount must be 50.00", BigDecimal.valueOf(5000, 2), u1.getAccounts().get(0).getAmount());
        Assert.assertEquals("Amount must be 50.00", BigDecimal.valueOf(5000, 2), u1.getAccounts().get(1).getAmount());
    }

    @Test
    public void testStress() throws InterruptedException {
        final int tries = 10000;
        double amount = tries + 10.;
        final int size = 100;
        final User u = applicationClient.createUser("aaa@bbb.com", "123456789");
        for (int i = 0; i < size; ++i) {
            final Account a = applicationClient.createAccount(u.getId(), null);
            applicationClient.deposit(a.getId(), BigDecimal.valueOf(amount));
        }
        final List<Account> accountsBefore = applicationClient.getUsers(u.getId()).get(0).getAccounts();
        double total = accountsBefore.stream().map(Account::getAmount).map(BigDecimal::doubleValue).reduce(Double::sum).orElse(-1.);
        Assert.assertEquals("Sum amount is not correct", accountsBefore.size() * amount, total, 0.001);
        final Random r = new Random(0);
        final ExecutorService exec = Executors.newFixedThreadPool(32);
        final ThreadLocal<ApplicationClient> client = ThreadLocal.withInitial(() -> {
            final ResteasyClient client1 = new ResteasyClientBuilder().build();
            final ResteasyWebTarget target = client1.target("http://localhost:8081");
            return target.proxy(ApplicationClient.class);
        });
        long startTime = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < tries; ++i) {
            final int src = r.nextInt(accountsBefore.size());
            final int dst = r.nextInt(accountsBefore.size());
            if (src == dst) {
                continue;
            }
            count++;
            exec.submit(() -> client.get().transfer(accountsBefore.get(src).getId(), accountsBefore.get(dst).getId(), BigDecimal.ONE));
        }

        exec.shutdown();
        exec.awaitTermination(15L, TimeUnit.MINUTES);
        LOGGER.info("Took {} millis", System.currentTimeMillis() - startTime);
        final List<Transaction> trs = applicationClient.getTransactions();
        Assert.assertEquals("Wrong amount of transactions", size + count, trs.size());
        final List<Account> accountsAfter = applicationClient.getUsers(u.getId()).get(0).getAccounts();
        double totalAfter = accountsAfter.stream().map(Account::getAmount).map(BigDecimal::doubleValue).reduce(Double::sum).orElse(-1.);
        Assert.assertEquals("Sum amount is not correct", accountsAfter.size() * amount, totalAfter, 0.001);
    }


    @Before
    public void init() {
        launcher = new Launcher();
        launcher.launch();
        final ResteasyClient client = new ResteasyClientBuilder().build();
        final ResteasyWebTarget target = client.target("http://localhost:8081");

        applicationClient = target.proxy(ApplicationClient.class);

    }

    @After
    public void destroy() {
        launcher.shutdown();
    }
}