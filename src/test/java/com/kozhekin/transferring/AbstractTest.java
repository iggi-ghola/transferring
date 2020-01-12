package com.kozhekin.transferring;

import com.kozhekin.transferring.model.MoneyTransaction;
import com.kozhekin.transferring.model.MoneyTransactionState;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

abstract public class AbstractTest {
    protected final int threads = Runtime.getRuntime().availableProcessors() + 2;

    protected void checkErrors(Collection<Future<?>> tasks) throws InterruptedException, ExecutionException {
        for (Future<?> task : tasks) {
            try {
                task.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof AssertionError) {
                    throw (AssertionError) e.getCause();
                }
                throw e;
            }
        }
    }

    protected void validateSuccessMoneyTransaction(com.kozhekin.transferring.modelTest.MoneyTransaction trx) {
        Assert.assertNotNull("trx must not be null", trx);
        Assert.assertNotNull("amount must not be null", trx.getAmount());
        Assert.assertNotNull("date must not be null", trx.getDate());
        Assert.assertEquals("state must be SUCCESS", MoneyTransactionState.SUCCESS, trx.getState());
        Assert.assertTrue("srcId must be non negative", trx.getSrcId() > -1);
        Assert.assertTrue("dstId must be positive", trx.getDstId() > 0);
        Assert.assertTrue("id must be non negative", trx.getId() > -1);
    }

    protected void validateSuccessMoneyTransaction(MoneyTransaction trx) {
        Assert.assertNotNull("trx must not be null", trx);
        Assert.assertNotNull("amount must not be null", trx.getAmount());
        Assert.assertNotNull("date must not be null", trx.getDate());
        Assert.assertEquals("state must be SUCCESS", MoneyTransactionState.SUCCESS, trx.getState());
        Assert.assertTrue("srcId must be non negative", trx.getSrcId() > -1);
        Assert.assertTrue("dstId must be positive", trx.getDstId() > 0);
        Assert.assertTrue("id must be non negative", trx.getId() > -1);
    }

    protected void validateErrorMoneyTransaction(MoneyTransaction trx, BigDecimal amount) {
        Assert.assertNotNull("trx must not be null", trx);
        Assert.assertEquals("amount must not be null", amount, trx.getAmount());
        Assert.assertEquals("state must be ERROR", MoneyTransactionState.ERROR, trx.getState());
        Assert.assertNotNull("comment must not be null", trx.getComment());
    }

}
