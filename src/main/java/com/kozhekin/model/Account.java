package com.kozhekin.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Class used as representation of DB table and Rest Data object
 */
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private long id;
    @Column
    private BigDecimal amount = BigDecimal.ZERO;
    @Column
    private String type = "CURRENT";
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    @Column
    private long version;

    public Account(final User u, final String type) {
        user = u;
        if (null != type) {
            this.type = type;
        }
    }

    public Account() {
    }

    public long getId() {
        return id;
    }

    public Account setId(final long id) {
        this.id = id;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account setAmount(final BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getType() {
        return type;
    }

    public Account setType(final String type) {
        this.type = type;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Account setUser(final User user) {
        this.user = user;
        return this;
    }

    public long getVersion() {
        return version;
    }

    public Account setVersion(final long version) {
        this.version = version;
        return this;
    }

}
