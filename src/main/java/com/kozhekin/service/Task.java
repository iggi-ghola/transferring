package com.kozhekin.service;

import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Base class for TransferringService tasks
 */
public class Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);
    protected final long srcAccountId;
    protected final long dstAccountId;
    protected final BigDecimal amount;

    public Task(final long srcAccountId, final long dstAccountId, final BigDecimal amount) {
        this.srcAccountId = srcAccountId;
        this.dstAccountId = dstAccountId;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive number: " + amount);
        }
        this.amount = amount;
    }

    protected void updateAccount(final Session s, final Account a, final BigDecimal amount) {
        a.setAmount(a.getAmount().add(amount));
        a.setVersion(a.getVersion() + 1);
        s.update(a);
    }

    protected Transaction createTransaction() {
        return new Transaction()
                .setAmount(amount)
                .setSrcId(srcAccountId)
                .setDstId(dstAccountId)
                .setDate(new Timestamp(System.currentTimeMillis()));
    }

}
