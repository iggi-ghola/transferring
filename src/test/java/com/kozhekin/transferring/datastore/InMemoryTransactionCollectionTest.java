package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.AbstractTest;
import com.kozhekin.transferring.model.MoneyTransaction;
import com.kozhekin.transferring.model.MoneyTransactionState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class InMemoryTransactionCollectionTest extends AbstractTest {

    private InMemoryTransactionCollection transactionCollection;
    private MoneyTransaction.MoneyTransactionBuilder builder;

    @Before
    public void init() {
        transactionCollection = new InMemoryTransactionCollection();
        builder = MoneyTransaction.builder().setAmount(BigDecimal.valueOf(123.45))
                .setSrcId(1).setDstId(2).setState(MoneyTransactionState.SUCCESS);
    }

    @Test
    public void getAllTransactions() {
        Assert.assertNotNull("Must return empty collection", transactionCollection.getAllTransactions());
        Assert.assertEquals("Must return empty collection", 0, transactionCollection.getAllTransactions().size());
    }

    @Test
    public void saveTransaction() {
        MoneyTransaction t = transactionCollection.saveTransaction(builder);
        validateSuccessMoneyTransaction(t);
        t = transactionCollection.saveTransaction(builder.setState(MoneyTransactionState.ERROR).setComment("test"));
        validateErrorMoneyTransaction(t, builder.getAmount());
        List<MoneyTransaction> transactions = transactionCollection.getTransactions(1);
        Assert.assertNotNull("Transaction list must not be null", transactions);
        Assert.assertEquals("Wrong number of transactions for account id=1", 2, transactions.size());
        transactions = transactionCollection.getTransactions(2);
        Assert.assertNotNull("Transaction list must not be null", transactions);
        Assert.assertEquals("Wrong number of transactions for account id=2", 2, transactions.size());
    }
}