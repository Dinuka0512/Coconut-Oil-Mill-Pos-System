package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public ArrayList<ProductEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<ProductEntity> query = session.createQuery(
                    "FROM ProductEntity", ProductEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(ProductEntity entity) {
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
    public boolean update(ProductEntity entity) {
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
                    "SELECT COUNT(p) FROM ProductEntity p WHERE p.id = :id",
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
            String sql = "UPDATE product SET is_deleted = true WHERE id = :id";
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
            String sql = "SELECT id FROM product ORDER BY id DESC LIMIT 1";
            NativeQuery<String> query = session.createNativeQuery(sql, String.class);
            String lastId = query.uniqueResult();

            if (lastId == null || lastId.isEmpty()) {
                return "PROD001";
            }

            String numericPart = lastId.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                return "PROD001";
            }

            int number = Integer.parseInt(numericPart);
            return String.format("PROD%03d", number + 1);
        } catch (NumberFormatException e) {
            return "PROD001";
        } finally {
            session.close();
        }
    }

    @Override
    public ProductEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String sql = "SELECT * FROM product WHERE id = :id AND is_deleted = false";
            NativeQuery<ProductEntity> query = session.createNativeQuery(sql, ProductEntity.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
}