package com.kozhekin.transferring.model;

import java.math.BigDecimal;

public class Account {

    private final int id;
    private BigDecimal amount;
    private final String type;
    private final String username;

    private Account(int id, BigDecimal amount, String type, String username) {
        this.id = id;
        this.username = username;
        this.type = type == null ? "CURRENT" : type;
        this.amount = amount == null ? BigDecimal.ZERO : amount;
    }

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

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public Account copy() {
        return new Account(id, amount, type, username);
    }

    public static class AccountBuilder {
        private int id;
        private BigDecimal amount;
        private String type;
        private String username;

        public AccountBuilder() {

        }

        public Account build() {
            return new Account(id, amount, type, username);
        }

        public int getId() {
            return id;
        }

        public AccountBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public AccountBuilder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public String getType() {
            return type;
        }

        public AccountBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public String getUsername() {
            return username;
        }

        public AccountBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

    }
}
