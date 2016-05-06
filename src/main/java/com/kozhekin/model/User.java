package com.kozhekin.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used as representation of DB table and Rest Data object
 */

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private int id;
    @Column
    private String email;
    @Column
    private String phone;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Account> accounts = new ArrayList<>();

    public User(final String email, final String phone) {
        this.email = email;
        this.phone = phone;
    }

    public User() {
    }

    public int getId() {
        return id;
    }

    public User setId(final int id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(final String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public User setPhone(final String phone) {
        this.phone = phone;
        return this;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

}
