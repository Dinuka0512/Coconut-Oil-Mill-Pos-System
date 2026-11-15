package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.ProductPriceDAO;
import com.dev09.kumaracoconutoil.entity.ProductPriceEntity;
import com.dev09.kumaracoconutoil.util.AuditUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductPriceDAOImpl implements ProductPriceDAO {

    @Override
    public ArrayList<ProductPriceEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<ProductPriceEntity> query = session.createQuery(
                    "FROM ProductPriceEntity ORDER BY id", ProductPriceEntity.class);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public ArrayList<ProductPriceEntity> getByProductId(String productId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<ProductPriceEntity> query = session.createQuery(
                    "FROM ProductPriceEntity p WHERE p.productId = :productId ORDER BY p.id",
                    ProductPriceEntity.class);
            query.setParameter("productId", productId);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(ProductPriceEntity entity) {
        if (entity.getId() == null || entity.getId().isBlank()) {
            entity.setId(generateNewId());
        }
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
    public boolean update(ProductPriceEntity entity) {
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
                    "SELECT COUNT(p) FROM ProductPriceEntity p WHERE p.id = :id",
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
            String sql = "UPDATE product_price SET is_deleted = true, updated_by = :updatedBy, updated_at = :updatedAt WHERE id = :id";
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
    public boolean deleteByProductId(String productId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            String sql = "UPDATE product_price SET is_deleted = true, updated_by = :updatedBy, updated_at = :updatedAt WHERE product_id = :productId";
            session.createNativeQuery(sql)
                    .setParameter("productId", productId)
                    .setParameter("updatedBy", AuditUtil.currentUser())
                    .setParameter("updatedAt", LocalDateTime.now())
                    .executeUpdate();
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
    public String generateNewId() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return String.format("PRC%03d", nextNumber(session));
        } finally {
            session.close();
        }
    }

    @Override
    public ProductPriceEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<ProductPriceEntity> query = session.createQuery(
                    "FROM ProductPriceEntity p WHERE p.id = :id", ProductPriceEntity.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    @Override
    public boolean syncPrices(String productId, ArrayList<ProductPriceEntity> prices) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction tx = session.beginTransaction();
        try {
            List<ProductPriceEntity> existing = session.createQuery(
                            "FROM ProductPriceEntity p WHERE p.productId = :productId",
                            ProductPriceEntity.class)
                    .setParameter("productId", productId)
                    .list();

            Map<String, ProductPriceEntity> remaining = new LinkedHashMap<>();
            for (ProductPriceEntity entity : existing) {
                remaining.put(entity.getId(), entity);
            }

            int nextNumber = nextNumber(session);

            for (ProductPriceEntity incoming : prices) {
                String id = incoming.getId();
                if (id == null || id.isBlank()) {
                    ProductPriceEntity entity = new ProductPriceEntity(
                            String.format("PRC%03d", nextNumber++),
                            productId,
                            incoming.getPrice());
                    session.persist(entity);
                } else {
                    ProductPriceEntity entity = remaining.remove(id);
                    if (entity != null) {
                        entity.setPrice(incoming.getPrice());
                    } else {
                        incoming.setProductId(productId);
                        session.merge(incoming);
                    }
                }
            }

            for (ProductPriceEntity removed : remaining.values()) {
                removed.setDeleted(true);
            }

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

    private int nextNumber(Session session) {
        String sql = "SELECT id FROM product_price ORDER BY id DESC LIMIT 1";
        NativeQuery<String> query = session.createNativeQuery(sql, String.class);
        String lastId = query.uniqueResult();

        if (lastId == null || lastId.isEmpty()) {
            return 1;
        }

        String numericPart = lastId.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) {
            return 1;
        }

        try {
            return Integer.parseInt(numericPart) + 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
