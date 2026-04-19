package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.OrderProductDetailDAO;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class OrderProductDetailDAOImpl implements OrderProductDetailDAO {
    @Override
    public ArrayList<OrderProductDetailsEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderProductDetailsEntity> query = session.createQuery("FROM OrderProductDetailsEntity", OrderProductDetailsEntity.class);
            List<OrderProductDetailsEntity> list = query.list();
            return new ArrayList<>(list);
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(OrderProductDetailsEntity entity) {
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
    public boolean update(OrderProductDetailsEntity entity) {
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
            Query<Long> query = session.createQuery("SELECT COUNT(od.id) FROM OrderProductDetailsEntity od WHERE od.id = :id", Long.class);
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
            OrderProductDetailsEntity entity = session.get(OrderProductDetailsEntity.class, id);
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
            String lastId = (String) session.createQuery("SELECT od.id FROM OrderProductDetailsEntity od ORDER BY od.id DESC").setMaxResults(1).uniqueResult();
            if (lastId == null) {
                return "OD001";
            }
            int number = Integer.parseInt(lastId.substring(2));
            return String.format("OD%03d", ++number);
        } finally {
            session.close();
        }
    }

    @Override
    public OrderProductDetailsEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(OrderProductDetailsEntity.class, id);
        } finally {
            session.close();
        }
    }
}
