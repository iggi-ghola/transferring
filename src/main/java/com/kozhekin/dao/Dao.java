package com.kozhekin.dao;

import com.kozhekin.Hibernate;
import com.kozhekin.model.Account;
import com.kozhekin.model.Transaction;
import com.kozhekin.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class for working with DB
 */
public class Dao {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

    public User createUser(final String email, final String phone) throws Exception {
        return Hibernate.doInTransaction(s -> {
            final User u = new User(email, phone);
            s.save(u);
            return u;
        });
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsers(final Object... ids) {
        try {
            return Hibernate.doInTransaction(s -> {
                if (null == ids || 0 == ids.length) {
                    return (List<User>) s.createQuery("from User u where u.id != 0").list();
                }
                return (List<User>) s.createQuery("from User u where u.id in (:ids)").setParameterList("ids", ids).list();
            });
        } catch (Exception e) {
            LOGGER.error("Exception during getting users ids: " + Arrays.toString(ids), e);
        }
        return Collections.emptyList();
    }

    public Account createAccount(final int userId, final String type) throws Exception {
        return Hibernate.doInTransaction(s -> {
            final User u = s.get(User.class, userId);
            if (null == u) {
                throw new IllegalArgumentException("User not found, id = " + userId);
            }
            final Account a = new Account(u, type);
            s.save(a);
            return a;
        });
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> getTransactions(final Object... ids) {
        try {
            return Hibernate.doInTransaction(s -> {
                if (null == ids || 0 == ids.length) {
                    return (List<Transaction>) s.createQuery("from Transaction t").list();
                }
                return (List<Transaction>) s.createQuery("from Transaction t where t.srcId in (:ids) or t.dstId in (:ids)").setParameterList("ids", ids).list();
            });
        } catch (Exception e) {
            LOGGER.error("Exception during getting users ids: " + Arrays.toString(ids), e);
        }
        return Collections.emptyList();
    }
}
