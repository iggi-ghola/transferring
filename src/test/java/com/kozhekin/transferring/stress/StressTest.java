package com.kozhekin.transferring.stress;

import com.kozhekin.transferring.AbstractTest;
import com.kozhekin.transferring.ApplicationClient;
import com.kozhekin.transferring.ApplicationClientFactory;
import com.kozhekin.transferring.ApplicationContext;
import com.kozhekin.transferring.model.MoneyTransactionState;
import com.kozhekin.transferring.modelTest.Account;
import com.kozhekin.transferring.modelTest.MoneyTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StressTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StressTest.class);
    private ApplicationContext applicationContext;
    private ThreadLocal<ApplicationClient> applicationClient;
    private BlockingQueue<Runnable> taskQueue;
    private ExecutorService exec;

    @Test
    @Category(com.kozhekin.transferring.category.StressTest.class)
    public void testStress() throws InterruptedException, ExecutionException {
        final int minutes = Integer.parseInt(System.getProperty("stress.time.minutes", "1"));
        final long stopTime = System.nanoTime() + TimeUnit.MINUTES.toNanos(minutes);

        double amount = 100;
        final int size = 100;
        createAccounts(size, amount);
        List<Account> accountsBefore = checkAccountsBefore(amount);

        final Random r = new Random(0);
        long startTime = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
        Collection<Future<?>> tasks = new ArrayList<>();
        while (System.nanoTime() < stopTime) {
            if (taskQueue.size() > threads * 3) {
                checkErrors(tasks);
                tasks.clear();
                continue;
            }
            final int src = r.nextInt(accountsBefore.size());
            final int dst = r.nextInt(accountsBefore.size());
            tasks.add(exec.submit(() -> {
                MoneyTransaction t = getClient().transfer(accountsBefore.get(src).getId(), accountsBefore.get(dst).getId(), BigDecimal.ONE);
                if (t.getState() == MoneyTransactionState.SUCCESS) {
                    count.incrementAndGet();
                }
                Assert.assertTrue("Amount must be positive", getClient().getAccount(src).getAmount().signum() >= 0);
                Assert.assertTrue("Amount must be positive", getClient().getAccount(dst).getAmount().signum() >= 0);
            }));
        }
        checkErrors(tasks);

        LOGGER.info("Took {} millis, {} success transactions", System.currentTimeMillis() - startTime, count.get());

        checkTransactionsAfter(size + count.get());
        checkAccountsAfter(amount);
    }

    @Before
    public void init() {
        applicationContext = new ApplicationContext();
        applicationContext.start();
        applicationClient = ThreadLocal.withInitial(ApplicationClientFactory::create);
        int nThreads = threads * 2;
        taskQueue = new LinkedBlockingQueue<>();
        exec = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, taskQueue);
    }

    @After
    public void destroy() {
        exec.shutdown();
        applicationContext.stop();
    }

    public ApplicationClient getClient() {
        return applicationClient.get();
    }

    private void checkTransactionsAfter(final int expectedCount) {
        final Collection<MoneyTransaction> trs = getClient().getTransactions();
        Assert.assertEquals("Wrong amount of transactions", expectedCount, trs.size());
        trs.forEach(this::validateSuccessMoneyTransaction);
    }

    private void createAccounts(final int size, final double amount) throws ExecutionException, InterruptedException {

        AtomicInteger suffix = new AtomicInteger(0);
        List<Future<?>> tasks = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            tasks.add(exec.submit(() -> {
                final Account a = getClient().createAccount("aaa" + suffix.incrementAndGet() + "@bbb.com");
                getClient().deposit(a.getId(), BigDecimal.valueOf(amount));
            }));
        }
        checkErrors(tasks);

    }

    private List<Account> checkAccountsBefore(double amount) {
        // skip system account
        final List<Account> accountsBefore = getClient().getAccounts().stream().skip(1).collect(Collectors.toList());
        double total = accountsBefore.stream().map(Account::getAmount).map(BigDecimal::doubleValue).reduce(Double::sum).orElse(-1.);
        Assert.assertEquals("Sum amount is not correct", accountsBefore.size() * amount, total, 0.001);
        return accountsBefore;
    }

    private void checkAccountsAfter(final double amount) {
        final List<Account> accountsAfter = getClient().getAccounts().stream().skip(1).collect(Collectors.toList());
        accountsAfter.forEach(a -> Assert.assertTrue("Amount must be not negative", a.getAmount().compareTo(BigDecimal.ZERO) >= 0));
        double totalAfter = accountsAfter.stream().map(Account::getAmount).map(BigDecimal::doubleValue).reduce(Double::sum).orElse(-1.);
        Assert.assertEquals("Sum amount is not correct", accountsAfter.size() * amount, totalAfter, 0.001);
    }

}
