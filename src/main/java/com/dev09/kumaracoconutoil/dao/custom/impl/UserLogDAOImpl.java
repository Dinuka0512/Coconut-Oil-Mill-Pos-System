package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.UserLogDAO;
import com.dev09.kumaracoconutoil.entity.UserLogEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserLogDAOImpl implements UserLogDAO {

    @Override
    public ArrayList<UserLogEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<UserLogEntity> query = session.createQuery(
                    "SELECT u FROM UserLogEntity u ORDER BY u.loggedAt DESC",
                    UserLogEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(UserLogEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.persist(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean update(UserLogEntity entity) {
        return false;
    }

    @Override
    public boolean exist(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u.id) FROM UserLogEntity u WHERE u.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String id) {
        return false;
    }

    @Override
    public String generateNewId() {
        return null;
    }

    @Override
    public UserLogEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(UserLogEntity.class, id);
        } finally {
            session.close();
        }
    }

    @Override
    public int deleteOlderThan(LocalDateTime cutoff) {
        if (cutoff == null) return 0;
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            int deleted = session.createMutationQuery("DELETE FROM UserLogEntity u WHERE u.loggedAt < :cutoff")
                    .setParameter("cutoff", cutoff)
                    .executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return 0;
        } finally {
            session.close();
        }
    }
}
