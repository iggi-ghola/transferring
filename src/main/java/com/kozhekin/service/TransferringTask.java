package com.kozhekin.service;

import com.kozhekin.Hibernate;
import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import com.kozhekin.model.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class transfers money between inner accounts
 */
public class TransferringTask extends Task implements Callable<Transaction> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferringTask.class);

    public TransferringTask(final long srcAccountId, final long dstAccountId, final BigDecimal amount) {
        super(srcAccountId, dstAccountId, amount);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Transaction call() throws Exception {
        return Hibernate.doInTransaction(s -> {
            List<Account> l = s.createQuery("from Account a where a.id = :srcId or a.id = :dstId")
                    .setParameter("srcId", srcAccountId).setParameter("dstId", dstAccountId).list();
            final Transaction t = createTransaction();
            if (l.isEmpty()) {
                return t.setState(TransactionState.ERROR).setComment(String.format("Can not find Account with id in (%d, %d)", srcAccountId, dstAccountId));
            }
            if (l.size() == 1) {
                return t.setState(TransactionState.ERROR).setComment(String.format("Can not find Account with id: %d",
                        srcAccountId == l.get(0).getId() ? dstAccountId : srcAccountId));
            }
            if (l.get(0).getId() == l.get(1).getId()) {
                return t.setState(TransactionState.ERROR).setComment("srcId equals dstId");
            }
            // make srs the first and dst the second element
            if (l.get(0).getId() == dstAccountId) {
                Collections.swap(l, 0, 1);
            }
            if (l.get(0).getAmount().compareTo(amount) < 0) {
                t.setState(TransactionState.ERROR)
                        .setComment("Not enough money on account id: " + srcAccountId);
                s.save(t);
                return t;
            }
            LOGGER.debug("Transferring amount {}, from {} to {}", amount, srcAccountId, dstAccountId);
            updateAccount(s, l.get(0), amount.negate());
            updateAccount(s, l.get(1), amount);
            t.setState(TransactionState.SUCCESS);
            s.save(t);
            return t;
        });
    }

}
