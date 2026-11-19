package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.util.AuditUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
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
            String sql = "UPDATE product SET is_deleted = true, updated_by = :updatedBy, updated_at = :updatedAt WHERE id = :id";
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
    public ArrayList<ProductEntity> getBySupplier(String supplierId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<ProductEntity> query = session.createQuery(
                    "FROM ProductEntity p WHERE p.supplierId = :supplierId", ProductEntity.class);
            query.setParameter("supplierId", supplierId);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean existByItemCode(String itemCode) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String sql = "SELECT COUNT(*) FROM product WHERE itemcode = :itemcode";
            NativeQuery<Long> query = session.createNativeQuery(sql, Long.class);
            query.setParameter("itemcode", itemCode);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean existActiveByItemCode(String itemCode) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            String sql = "SELECT COUNT(*) FROM product WHERE itemcode = :itemcode AND is_deleted = false";
            NativeQuery<Long> query = session.createNativeQuery(sql, Long.class);
            query.setParameter("itemcode", itemCode);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public ProductEntity findAnyByItemCode(String itemCode) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            // Native query so it also sees soft-deleted rows (bypasses @SQLRestriction).
            String sql = "SELECT * FROM product WHERE itemcode = :itemcode";
            NativeQuery<ProductEntity> query = session.createNativeQuery(sql, ProductEntity.class);
            query.setParameter("itemcode", itemCode);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    @Override
    public boolean revive(ProductEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            // A native UPDATE is used because @SQLRestriction hides soft-deleted rows
            // from Hibernate's merge/load, which would otherwise try to re-insert.
            String sql = "UPDATE product SET is_deleted = false, itemcode = :itemcode, name = :name, "
                    + "price_qty = :priceQty, price = :price, qty = :qty, supplier_id = :supplierId, "
                    + "updated_by = :updatedBy, updated_at = :updatedAt WHERE id = :id";
            int rowsUpdated = session.createNativeQuery(sql)
                    .setParameter("id", entity.getId())
                    .setParameter("itemcode", entity.getItemcode())
                    .setParameter("name", entity.getName())
                    .setParameter("priceQty", entity.getPriceQty())
                    .setParameter("price", entity.getPrice())
                    .setParameter("qty", entity.getQty())
                    // Explicit type so a null supplier id can still be bound.
                    .setParameter("supplierId", entity.getSupplierId(), String.class)
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