package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.DuePaymentDAO;
import com.dev09.kumaracoconutoil.entity.DuePaymentEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;

public class DuePaymentDAOImpl implements DuePaymentDAO {

    @Override
    public ArrayList<DuePaymentEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<DuePaymentEntity> query = session.createQuery(
                "SELECT d FROM DuePaymentEntity d LEFT JOIN FETCH d.customer LEFT JOIN FETCH d.order ORDER BY d.date DESC",
                DuePaymentEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(DuePaymentEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.persist(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean update(DuePaymentEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.merge(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean exist(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d.id) FROM DuePaymentEntity d WHERE d.id = :id", Long.class);
            query.setParameter("id", id);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createNativeQuery("DELETE FROM due_payment WHERE id = :id")
                   .setParameter("id", id)
                   .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public String generateNewId() {
        return null;
    }

    @Override
    public DuePaymentEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(DuePaymentEntity.class, id);
        } finally {
            session.close();
        }
    }

    @Override
    public ArrayList<DuePaymentEntity> getDuePaymentsByCustomerId(String customerId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<DuePaymentEntity> query = session.createQuery(
                "SELECT d FROM DuePaymentEntity d LEFT JOIN FETCH d.customer LEFT JOIN FETCH d.order WHERE d.customer.custId = :customerId ORDER BY d.date DESC",
                DuePaymentEntity.class);
            query.setParameter("customerId", customerId);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean deleteByOrderId(String orderId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createNativeQuery("DELETE FROM due_payment WHERE order_id = :orderId")
                   .setParameter("orderId", orderId)
                   .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean deleteDueAddedByOrderId(String orderId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.createNativeQuery("DELETE FROM due_payment WHERE order_id = :orderId AND type = 'DUE_ADDED'")
                   .setParameter("orderId", orderId)
                   .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }
}
