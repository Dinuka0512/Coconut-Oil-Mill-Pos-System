package com.dev09.kumaracoconutoil.dao.custom.impl;

import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    @Override
    public ArrayList<OrderEntity> getAll() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderEntity> query = session.createQuery("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.orderProducts op LEFT JOIN FETCH op.product", OrderEntity.class);
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
            
            session.createNativeQuery("DELETE FROM due_payment WHERE order_id = :id")
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
    public synchronized String generateNewId() {
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
    public List<LocalDate> getOrderDatesByMonth(int year, int month) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            LocalDate start = LocalDate.of(year, month, 1);
            Query<LocalDate> query = session.createQuery(
                "SELECT DISTINCT o.date FROM OrderEntity o "
                        + "WHERE o.date >= :start AND o.date < :end ORDER BY o.date",
                LocalDate.class);
            query.setParameter("start", start);
            query.setParameter("end", start.plusMonths(1));
            return query.list();
        } finally {
            session.close();
        }
    }

    @Override
    public double getTotalDueByCustomer(String customerId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<Double> query = session.createQuery(
                "SELECT COALESCE(SUM(o.balance), 0) FROM OrderEntity o WHERE o.customer.custId = :custId AND o.balance < 0",
                Double.class);
            query.setParameter("custId", customerId);
            Double result = query.uniqueResult();
            return result != null ? -result : 0.0;
        } finally {
            session.close();
        }
    }

    @Override
    public List<OrderEntity> getUnpaidOrdersByCustomer(String customerId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderEntity> query = session.createQuery(
                "SELECT o FROM OrderEntity o WHERE o.customer.custId = :custId AND o.balance < 0 ORDER BY o.date ASC, o.orderId ASC",
                OrderEntity.class);
            query.setParameter("custId", customerId);
            return query.list();
        } finally {
            session.close();
        }
    }

@Override
    public OrderEntity searchWithProducts(String orderId) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderEntity> query = session.createQuery(
                    "SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH o.orderProducts op LEFT JOIN FETCH op.product WHERE o.orderId = :orderId",
                    OrderEntity.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResult();
        } finally {
            session.close();
        }
    }

    @Override
    public List<OrderProductDetailsEntity> getOrderProductDetailsByDate(LocalDate date) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Query<OrderProductDetailsEntity> query = session.createQuery(
                    "SELECT od FROM OrderProductDetailsEntity od "
                            + "JOIN FETCH od.order o JOIN FETCH od.product p "
                            + "WHERE o.date = :date AND o.isDeleted = false AND od.isDeleted = false "
                            + "ORDER BY o.orderId ASC",
                    OrderProductDetailsEntity.class);
            query.setParameter("date", date);
            return new ArrayList<>(query.list());
        } finally {
            session.close();
        }
    }

    @Override
    public int clearFullyPaidOrders() {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            int paidOrderCount = ((Number) session.createNativeQuery(
                    "SELECT COUNT(*) FROM orders WHERE balance >= 0").uniqueResult()).intValue();
            if (paidOrderCount == 0) {
                transaction.commit();
                return 0;
            }

            // Only fully paid orders (balance >= 0) are removed. Orders with an
            // outstanding due (balance < 0) are kept so due management still works.
            session.createNativeQuery(
                    "DELETE FROM due_payment WHERE order_id IN (SELECT orderId FROM orders WHERE balance >= 0)")
                    .executeUpdate();
            session.createNativeQuery(
                    "DELETE FROM order_product_details WHERE order_id IN (SELECT orderId FROM orders WHERE balance >= 0)")
                    .executeUpdate();
            session.createNativeQuery("DELETE FROM orders WHERE balance >= 0")
                    .executeUpdate();

            transaction.commit();
            return paidOrderCount;
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            return -1;
        } finally {
            session.close();
        }
    }

    @Override
    public double getSalesBetween(LocalDate fromInclusive, LocalDate toExclusive) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Double total = session.createQuery(
                    "SELECT COALESCE(SUM(o.amountPaid - o.balance), 0) FROM OrderEntity o "
                            + "WHERE o.isDeleted = false AND o.date >= :from AND o.date < :to",
                    Double.class)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive)
                    .uniqueResult();
            return total != null ? total : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public long getActiveCustomerCount() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Long count = session.createQuery(
                    "SELECT COUNT(c) FROM CustomerEntity c WHERE c.isDeleted = false", Long.class)
                    .uniqueResult();
            return count != null ? count : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public long getActiveProductCount() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Long count = session.createQuery(
                    "SELECT COUNT(p) FROM ProductEntity p WHERE p.isDeleted = false", Long.class)
                    .uniqueResult();
            return count != null ? count : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public double getTotalCustomerDue() {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            Double total = session.createQuery(
                    "SELECT COALESCE(SUM(c.dueAmount), 0) FROM CustomerEntity c WHERE c.isDeleted = false",
                    Double.class)
                    .uniqueResult();
            return total != null ? total : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Object[]> getRecentSales(int limit) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.createQuery(
                    "SELECT o.orderId, o.date, COALESCE(c.name, ''), "
                            + "COALESCE(SUM(d.quantity), 0), (o.amountPaid - o.balance) "
                            + "FROM OrderEntity o "
                            + "LEFT JOIN o.customer c "
                            + "LEFT JOIN o.orderProducts d "
                            + "WHERE o.isDeleted = false "
                            + "GROUP BY o.orderId, o.date, c.name, o.amountPaid, o.balance "
                            + "ORDER BY o.date DESC, o.orderId DESC", Object[].class)
                    .setMaxResults(limit)
                    .list();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Object[]> getSalesByDay(LocalDate fromInclusive, LocalDate toExclusive) {
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            return session.createQuery(
                    "SELECT o.date, COALESCE(SUM(o.amountPaid - o.balance), 0) FROM OrderEntity o "
                            + "WHERE o.isDeleted = false AND o.date >= :from AND o.date < :to "
                            + "GROUP BY o.date ORDER BY o.date", Object[].class)
                    .setParameter("from", fromInclusive)
                    .setParameter("to", toExclusive)
                    .list();
        } finally {
            session.close();
        }
    }
}
