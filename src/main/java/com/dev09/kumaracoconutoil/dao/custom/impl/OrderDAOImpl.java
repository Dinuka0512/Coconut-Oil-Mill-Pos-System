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
            Query<OrderEntity> query = session.createQuery("FROM OrderEntity", OrderEntity.class);
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
            session.persist(entity);
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
            OrderEntity entity = session.get(OrderEntity.class, id);
            if (entity != null) {
                session.remove(entity);
                transaction.commit();
                return true;
            }
            return false;
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
            String lastId = (String) session.createQuery("SELECT o.orderId FROM OrderEntity o ORDER BY o.orderId DESC").setMaxResults(1).uniqueResult();
            if (lastId == null) {
                return "O001";
            }
            int number = Integer.parseInt(lastId.substring(1));
            return String.format("O%03d", ++number);
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
}
