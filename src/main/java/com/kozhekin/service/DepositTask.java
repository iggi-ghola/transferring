package com.kozhekin.service;

import com.kozhekin.Hibernate;
import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import com.kozhekin.model.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

/**
 * Class used by TransferringService to transfer money from outside
 */
public class DepositTask extends Task implements Callable<Transaction> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepositTask.class);
    private static final long SYSTEM_DEPOSIT_ID = 0L;

    public DepositTask(final long dstAccountId, final BigDecimal amount) {
        super(SYSTEM_DEPOSIT_ID, dstAccountId, amount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Transaction call() throws Exception {
        return Hibernate.doInTransaction(s -> {
            final Account dst = s.get(Account.class, dstAccountId);
            final Transaction t = createTransaction();
            if (null == dst) {
                return t.setState(TransactionState.ERROR).setComment("Can not find Account with id: " + dstAccountId);
            }
            final Account src = s.get(Account.class, SYSTEM_DEPOSIT_ID);
            updateAccount(s, src, amount.negate());
            updateAccount(s, dst, amount);
            t.setState(TransactionState.SUCCESS);
            s.save(t);
            return t;
        });
    }

}
