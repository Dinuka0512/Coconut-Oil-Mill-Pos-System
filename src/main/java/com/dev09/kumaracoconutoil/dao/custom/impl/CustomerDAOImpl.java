package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.CustomerDAO;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public ArrayList<CustomerEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<CustomerEntity> query = session.createQuery(
                    "FROM CustomerEntity", CustomerEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(CustomerEntity entity) {
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
    public boolean update(CustomerEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.merge(entity);
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
    public boolean exist(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(c) FROM CustomerEntity c WHERE c.custId = :id",
                    Long.class);
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
            String sql = "UPDATE customer SET is_deleted = true WHERE custId = :id";
            int rowsUpdated = session.createNativeQuery(sql)
                    .setParameter("id", id)
                    .executeUpdate();
            tx.commit();
            return rowsUpdated > 0;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public String generateNewId() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            // Fetch all IDs (including soft-deleted) to get the highest numeric suffix
            String sql = "SELECT custId FROM customer ORDER BY custId DESC LIMIT 1";
            NativeQuery<String> query = session.createNativeQuery(sql, String.class);
            String lastId = query.uniqueResult();

            if (lastId == null || lastId.isEmpty()) {
                return "C001";
            }

            // Extract numeric part safely
            String numericPart = lastId.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                return "C001";
            }

            int number = Integer.parseInt(numericPart);
            return String.format("C%03d", number + 1);
        } catch (NumberFormatException e) {
            return "C001";
        } finally {
            session.close();
        }
    }

    @Override
    public CustomerEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String sql = "SELECT * FROM customer WHERE custId = :id";
            NativeQuery<CustomerEntity> query = session.createNativeQuery(sql, CustomerEntity.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
}