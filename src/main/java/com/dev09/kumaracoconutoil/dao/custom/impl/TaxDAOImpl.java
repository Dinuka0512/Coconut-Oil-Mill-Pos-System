package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.TaxDAO;
import com.dev09.kumaracoconutoil.entity.TaxEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class TaxDAOImpl implements TaxDAO {
    @Override
    public ArrayList<TaxEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<TaxEntity> query = session.createQuery("FROM TaxEntity", TaxEntity.class);
            List<TaxEntity> list = query.list();
            return new ArrayList<>(list);
        } finally {
            session.close();
        }
    }

    @Override
    public boolean save(TaxEntity entity) {
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
    public boolean update(TaxEntity entity) {
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
            Query<Long> query = session.createQuery("SELECT COUNT(t.id) FROM TaxEntity t WHERE t.id = :id", Long.class);
            query.setParameter("id", Integer.parseInt(id));
            return query.uniqueResult() > 0;
        } catch (NumberFormatException e) {
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public boolean delete(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            TaxEntity entity = session.get(TaxEntity.class, Integer.parseInt(id));
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
            Integer lastId = (Integer) session.createQuery("SELECT t.id FROM TaxEntity t ORDER BY t.id DESC").setMaxResults(1).uniqueResult();
            if (lastId == null) {
                return "1";
            }
            return String.valueOf(++lastId);
        } finally {
            session.close();
        }
    }

    @Override
    public TaxEntity search(String id) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.get(TaxEntity.class, Integer.parseInt(id));
        } catch (NumberFormatException e) {
            return null;
        } finally {
            session.close();
        }
    }
}
