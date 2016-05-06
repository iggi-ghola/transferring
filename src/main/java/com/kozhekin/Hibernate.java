package com.kozhekin;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hibernate {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hibernate.class);
    private static SessionFactory sessionFactory;

    public static void init() {
        if (null != sessionFactory) {
            return;
        }
        LOGGER.info("Initializing Hibernate...");
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
        final Session s = sessionFactory.getCurrentSession();
        s.beginTransaction();
        s.createSQLQuery("RUNSCRIPT FROM 'classpath:h2-init.sql'").executeUpdate();
        s.getTransaction().commit();
    }

    public static void destroy() {
        LOGGER.info("Destroing Hibernate...");
        sessionFactory.close();
        sessionFactory = null;
    }

    public static SessionFactory factory() {
        return sessionFactory;
    }

    public static <T> T doInTransaction(Func<Session, T> f) throws Exception {
        final Session s = sessionFactory.getCurrentSession();
        final Transaction trx = s.beginTransaction();
        try {
            final T t = f.execute(s);
            trx.commit();
            return t;
        } catch (Exception e) {
            LOGGER.error("Exception during transaction, rolling back...");
            trx.rollback();
            throw e;
        }
    }

    public interface Func<K, T> {
        T execute(K k) throws Exception;
    }

}
