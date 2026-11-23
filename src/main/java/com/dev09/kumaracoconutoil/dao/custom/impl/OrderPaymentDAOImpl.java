package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.OrderPaymentDAO;
import com.dev09.kumaracoconutoil.entity.OrderPaymentEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;

public class OrderPaymentDAOImpl implements OrderPaymentDAO {

    @Override
    public boolean save(OrderPaymentEntity entity) {
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
    public ArrayList<OrderPaymentEntity> getByOrderId(String orderId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderPaymentEntity> query = session.createQuery(
                    "FROM OrderPaymentEntity p WHERE p.orderId = :orderId", OrderPaymentEntity.class);
            query.setParameter("orderId", orderId);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }
}
