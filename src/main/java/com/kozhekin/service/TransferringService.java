package com.kozhekin.service;

import com.kozhekin.model.Transaction;
import com.kozhekin.model.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.*;

public class TransferringService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferringService.class);
    private static ExecutorService exec;

    public static void init() {
        if (null != exec) {
            return;
        }
        LOGGER.info("Initializing TransferringService...");
        exec = Executors.newSingleThreadExecutor();
    }

    public static void destroy() {
        if (null == exec) {
            return;
        }
        LOGGER.info("Stopping TransferringService...");
        exec.shutdown();
        try {
            exec.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted", e);
            Thread.currentThread().interrupt();
        }
        exec = null;
    }

    public Transaction transfer(final long srcId, final long dstId, BigDecimal amount) {
        return handleExceptions(exec.submit(new TransferringTask(srcId, dstId, amount)));
    }

    public Transaction deposit(final long dstId, BigDecimal amount) {
        return handleExceptions(exec.submit(new DepositTask(dstId, amount)));
    }

    private Transaction handleExceptions(final Future<Transaction> f) {
        try {
            return f.get();
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted", e);
            Thread.currentThread().interrupt();
            return new Transaction().setState(TransactionState.ERROR).setComment(e.getMessage());
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return new Transaction().setState(TransactionState.ERROR).setComment(e.getMessage());
        }
    }
}
