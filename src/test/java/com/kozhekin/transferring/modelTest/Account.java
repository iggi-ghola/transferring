package com.kozhekin.transferring.modelTest;

import java.math.BigDecimal;

public class Account {

    private int id;
    private BigDecimal amount;
    private String type;
    private String username;

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

}
