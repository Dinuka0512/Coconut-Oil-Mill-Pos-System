package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    @Override
    public ArrayList<OrderEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderEntity> query = session.createQuery("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.orderProducts op LEFT JOIN FETCH op.product", OrderEntity.class);
            List<OrderEntity> list = query.list();
            return new ArrayList<>(list);
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(OrderEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.merge(entity);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean update(OrderEntity entity) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.merge(entity);
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
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
            Query<Long> query = session.createQuery("SELECT COUNT(o.orderId) FROM OrderEntity o WHERE o.orderId = :id", Long.class);
            query.setParameter("id", id);
            return query.uniqueResult() > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            session.createNativeQuery("DELETE FROM order_product_details WHERE order_id = :id")
                  .setParameter("id", id)
                  .executeUpdate();
            
            session.createNativeQuery("DELETE FROM orders WHERE orderId = :id")
                  .setParameter("id", id)
                  .executeUpdate();
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            transaction.rollback();
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
            String sql = "SELECT orderId FROM orders ORDER BY orderId DESC LIMIT 1";
            String lastId = (String) session.createNativeQuery(sql).uniqueResult();
            
            if (lastId == null) {
                return "O001";
            }
            
            lastId = lastId.replaceAll("[^0-9]", "");
            int number = Integer.parseInt(lastId);
            return String.format("O%03d", number + 1);
        } catch (Exception e) {
            return "O001";
        } finally {
            session.close();
        }
    }

    @Override
    public OrderEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(OrderEntity.class, id);
        } finally {
            session.close();
        }
    }
    
    @Override
    public OrderEntity searchWithProducts(String orderId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderEntity> query = session.createQuery(
                "SELECT o FROM OrderEntity o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH o.orderProducts op LEFT JOIN FETCH op.product WHERE o.orderId = :orderId",
                OrderEntity.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }
}
