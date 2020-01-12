package com.kozhekin.transferring.modelTest;

import com.kozhekin.transferring.model.MoneyTransactionState;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class MoneyTransaction {
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

    public MoneyTransaction setId(int id) {
        this.id = id;
        return this;
    }

    public int getSrcId() {
        return srcId;
    }

    public MoneyTransaction setSrcId(int srcId) {
        this.srcId = srcId;
        return this;
    }

    public int getDstId() {
        return dstId;
    }

    public MoneyTransaction setDstId(int dstId) {
        this.dstId = dstId;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public MoneyTransaction setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public MoneyTransaction setDate(Timestamp date) {
        this.date = date;
        return this;
    }

    public MoneyTransactionState getState() {
        return state;
    }

    public MoneyTransaction setState(MoneyTransactionState state) {
        this.state = state;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public MoneyTransaction setComment(String comment) {
        this.comment = comment;
        return this;
    }
}
