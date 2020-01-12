package com.kozhekin.transferring.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class MoneyTransaction {
    private final int id;
    private final int srcId;
    private final int dstId;
    private final BigDecimal amount;
    private final Timestamp date;
    private final MoneyTransactionState state;
    private final String comment;

    private MoneyTransaction(int id, int srcId, int dstId, BigDecimal amount, Timestamp date, MoneyTransactionState state, String comment) {
        this.id = id;
        this.srcId = srcId;
        this.dstId = dstId;
        this.amount = amount;
        this.date = date;
        this.state = state;
        this.comment = comment;
    }

    public static MoneyTransactionBuilder builder() {
        return new MoneyTransactionBuilder();
    }

    public static MoneyTransaction.MoneyTransactionBuilder prepareErrorTransactionBuilder(int srcId, int dstId, BigDecimal amount) {
        return prepareTransactionBuilder(srcId, dstId, amount).setState(MoneyTransactionState.ERROR);
    }

    public static MoneyTransaction.MoneyTransactionBuilder prepareTransactionBuilder(int srcId, int dstId, BigDecimal amount) {
        return MoneyTransaction.builder().setAmount(amount).setSrcId(srcId).setDstId(dstId);
    }

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public MoneyTransactionState getState() {
        return state;
    }

    public int getSrcId() {
        return srcId;
    }

    public int getDstId() {
        return dstId;
    }

    public String getComment() {
        return comment;
    }

    public static class MoneyTransactionBuilder {
        private int id;
        private int srcId;
        private int dstId;
        private BigDecimal amount;
        private Timestamp date;
        private MoneyTransactionState state;
        private String comment;

        public int getId() {
            return id;
        }

        public MoneyTransactionBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public int getSrcId() {
            return srcId;
        }

        public MoneyTransactionBuilder setSrcId(int srcId) {
            this.srcId = srcId;
            return this;
        }

        public int getDstId() {
            return dstId;
        }

        public MoneyTransactionBuilder setDstId(int dstId) {
            this.dstId = dstId;
            return this;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public MoneyTransactionBuilder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Date getDate() {
            return date;
        }

        public MoneyTransactionBuilder setDate(Timestamp date) {
            this.date = date;
            return this;
        }

        public MoneyTransactionState getState() {
            return state;
        }

        public MoneyTransactionBuilder setState(MoneyTransactionState state) {
            this.state = state;
            return this;
        }

        public String getComment() {
            return comment;
        }

        public MoneyTransactionBuilder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public MoneyTransaction build() {
            return new MoneyTransaction(id, srcId, dstId, amount, date, state, comment);
        }
    }
}
