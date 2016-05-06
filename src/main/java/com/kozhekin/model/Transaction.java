package com.kozhekin.model;

import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Class used as representation of DB table and Rest Data object
 */

@Entity
@Table(name = "transaction")
public class Transaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private long id;
    @Column(name = "src_account_id")
    private long srcId;
    @Column(name = "dst_account_id")
    private long dstId;
    @Column
    private BigDecimal amount;
    @Column
    private Timestamp date;
    @Column
    private TransactionState state;
    @Column
    private String comment;


    public long getId() {
        return id;
    }

    public Transaction setId(final long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Transaction setAmount(final BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Timestamp getDate() {
        return date;
    }

    public Transaction setDate(final Timestamp date) {
        this.date = date;
        return this;
    }

    public TransactionState getState() {
        return state;
    }

    public Transaction setState(final TransactionState state) {
        this.state = state;
        return this;
    }

    public long getSrcId() {
        return srcId;
    }

    public Transaction setSrcId(final long srcId) {
        this.srcId = srcId;
        return this;
    }

    public long getDstId() {
        return dstId;
    }

    public Transaction setDstId(final long dstId) {
        this.dstId = dstId;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Transaction setComment(final String comment) {
        this.comment = comment;
        return this;
    }
}
