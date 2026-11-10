package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.SupplierDAO;
import com.dev09.kumaracoconutoil.entity.SupplierEntity;
import com.dev09.kumaracoconutoil.util.AuditUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class SupplierDAOImpl implements SupplierDAO {

    @Override
    public ArrayList<SupplierEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<SupplierEntity> query = session.createQuery(
                    "FROM SupplierEntity", SupplierEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(SupplierEntity entity) {
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
    public boolean update(SupplierEntity entity) {
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
                    "SELECT COUNT(s) FROM SupplierEntity s WHERE s.supplierId = :id",
                    Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            String sql = "UPDATE supplier SET is_deleted = true, updated_by = :updatedBy, updated_at = :updatedAt WHERE supplierId = :id";
            int rowsUpdated = session.createNativeQuery(sql)
                    .setParameter("id", id)
                    .setParameter("updatedBy", AuditUtil.currentUser())
                    .setParameter("updatedAt", LocalDateTime.now())
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
            String sql = "SELECT supplierId FROM supplier ORDER BY supplierId DESC LIMIT 1";
            NativeQuery<String> query = session.createNativeQuery(sql, String.class);
            String lastId = query.uniqueResult();

            if (lastId == null || lastId.isEmpty()) {
                return "SUP001";
            }

            String numericPart = lastId.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                return "SUP001";
            }

            int number = Integer.parseInt(numericPart);
            return String.format("SUP%03d", number + 1);
        } catch (NumberFormatException e) {
            return "SUP001";
        } finally {
            session.close();
        }
    }

    @Override
    public SupplierEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String sql = "SELECT * FROM supplier WHERE supplierId = :id AND is_deleted = false";
            NativeQuery<SupplierEntity> query = session.createNativeQuery(sql, SupplierEntity.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
}
