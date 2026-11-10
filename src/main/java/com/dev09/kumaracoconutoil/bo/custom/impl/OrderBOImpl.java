package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.util.AppConstants;
import com.dev09.kumaracoconutoil.config.FactoryConfiguration;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.dao.custom.impl.OrderDAOImpl;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.DuePaymentEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.util.PdfGenerator;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderBOImpl implements OrderBO {
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);

    @Override
    public boolean placeOrder(OrderDto dto) {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            OrderEntity entity = new OrderEntity();
            entity.setOrderId(dto.getOrderId());
            entity.setDate(dto.getDate());
            entity.setAmountPaid(dto.getAmountPaid());
            entity.setDiscount(dto.getDiscount());
            entity.setTax(dto.getTax());
            entity.setTaxRate(dto.getTaxRate());
            entity.setBalance(dto.getBalance());
            entity.setPaymentMethod(dto.getPaymentMethod());

            CustomerEntity customerRef = session.getReference(CustomerEntity.class, dto.getCustomer().getCustId());
            entity.setCustomer(customerRef);

            ArrayList<OrderProductDetailsEntity> details = new ArrayList<>();
            if (dto.getOrderProducts() != null) {
                for (OrderProductDetailsDto detail : dto.getOrderProducts()) {
                    OrderProductDetailsEntity detailEntity = new OrderProductDetailsEntity();
                    detailEntity.setOrder(entity);

                    ProductEntity productRef = session.getReference(ProductEntity.class, detail.getProduct().getId());
                    detailEntity.setProduct(productRef);

                    detailEntity.setQuantity(detail.getQuantity());
                    detailEntity.setUnitPrice(detail.getUnitPrice());
                    details.add(detailEntity);

                    ProductEntity product = session.get(ProductEntity.class, detail.getProduct().getId());
                    if (product != null) {
                        product.setQty(product.getQty() - detail.getQuantity());
                        session.merge(product);
                    }
                }
            }
            entity.setOrderProducts(details);

            session.persist(entity);

            if (dto.getBalance() < 0 && !AppConstants.DEMO_CUSTOMER_ID.equals(dto.getCustomer().getCustId())) {
                CustomerEntity customerEntity = session.get(CustomerEntity.class, dto.getCustomer().getCustId());
                if (customerEntity != null) {
                    customerEntity.setDueAmount(customerEntity.getDueAmount() + Math.abs(dto.getBalance()));
                    session.merge(customerEntity);

                    DuePaymentEntity duePayment = new DuePaymentEntity();
                    duePayment.setDate(dto.getDate());
                    duePayment.setCustomer(customerEntity);
                    duePayment.setOrder(entity);
                    duePayment.setAmount(Math.abs(dto.getBalance()));
                    duePayment.setNote("Due added on order " + dto.getOrderId());
                    duePayment.setType("DUE_ADDED");
                    session.persist(duePayment);
                }
            }

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
    public String generateNextOrderId() throws SQLException, ClassNotFoundException {
        return orderDAO.generateNewId();
    }

    @Override
    public ArrayList<OrderDto> getAllOrders() throws SQLException, ClassNotFoundException {
        ArrayList<OrderEntity> entities = orderDAO.getAll();
        ArrayList<OrderDto> dtos = new ArrayList<>();
        
        for (OrderEntity entity : entities) {
            OrderDto dto = new OrderDto();
            dto.setOrderId(entity.getOrderId());
            dto.setDate(entity.getDate());
            
            if (entity.getCustomer() != null) {
                CustomerEntity c = entity.getCustomer();
                CustomerDto customerDto = new CustomerDto(
                        c.getCustId(),
                        c.getName(),
                        c.getContact(),
                        c.getDueAmount()
                );
                customerDto.setCreatedBy(c.getCreatedBy());
                customerDto.setCreatedAt(c.getCreatedAt());
                customerDto.setUpdatedBy(c.getUpdatedBy());
                customerDto.setUpdatedAt(c.getUpdatedAt());
                dto.setCustomer(customerDto);
            }
            
            dto.setAmountPaid(entity.getAmountPaid());
            dto.setDiscount(entity.getDiscount());
            dto.setTax(entity.getTax());
            dto.setTaxRate(entity.getTaxRate());
            dto.setBalance(entity.getBalance());
            dto.setPaymentMethod(entity.getPaymentMethod());
            dto.setCreatedBy(entity.getCreatedBy());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setUpdatedBy(entity.getUpdatedBy());
            dto.setUpdatedAt(entity.getUpdatedAt());
            
            if (entity.getOrderProducts() != null) {
                dto.setOrderProducts(new ArrayList<>());
                for (OrderProductDetailsEntity op : entity.getOrderProducts()) {
                    OrderProductDetailsDto detailDto = new OrderProductDetailsDto();
                    detailDto.setQuantity(op.getQuantity());
                    detailDto.setUnitPrice(op.getUnitPrice());
                    
                    if (op.getProduct() != null) {
                        ProductDto productDto = new ProductDto();
                        productDto.setId(op.getProduct().getId());
                        productDto.setItemcode(op.getProduct().getItemcode());
                        productDto.setName(op.getProduct().getName());
                        productDto.setQty(op.getProduct().getQty());
                        productDto.setPrice(op.getProduct().getPrice());
                        detailDto.setProduct(productDto);
                    }
                    
                    dto.getOrderProducts().add(detailDto);
                }
            } else {
                dto.setOrderProducts(new ArrayList<>());
            }
            
            dtos.add(dto);
        }
        
        return dtos;
    }

    @Override
    public boolean updateOrder(OrderDto dto) throws SQLException, ClassNotFoundException {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            OrderEntity entity = session.createQuery(
                "SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.orderProducts op LEFT JOIN FETCH op.product WHERE o.orderId = :orderId",
                OrderEntity.class)
                .setParameter("orderId", dto.getOrderId())
                .uniqueResult();
            if (entity == null) {
                transaction.rollback();
                return false;
            }

            session.createNativeQuery("DELETE FROM due_payment WHERE order_id = :orderId AND type = 'DUE_ADDED'")
                   .setParameter("orderId", dto.getOrderId())
                   .executeUpdate();

            if (entity.getBalance() < 0 && entity.getCustomer() != null) {
                CustomerEntity customerEntity = session.get(CustomerEntity.class, entity.getCustomer().getCustId());
                if (customerEntity != null) {
                    double oldDue = Math.abs(entity.getBalance());
                    customerEntity.setDueAmount(Math.max(0, customerEntity.getDueAmount() - oldDue));
                    session.merge(customerEntity);
                }
            }

            CustomerEntity customerRef = session.getReference(CustomerEntity.class, dto.getCustomer().getCustId());
            entity.setCustomer(customerRef);

            entity.setAmountPaid(dto.getAmountPaid());
            entity.setDiscount(dto.getDiscount());
            entity.setTax(dto.getTax());
            entity.setBalance(dto.getBalance());
            entity.setPaymentMethod(dto.getPaymentMethod());

            List<OrderProductDetailsEntity> orderProducts = entity.getOrderProducts();
            if (orderProducts == null) {
                orderProducts = new ArrayList<>();
                entity.setOrderProducts(orderProducts);
            } else {
                orderProducts.clear();
            }

            if (dto.getOrderProducts() != null) {
                for (OrderProductDetailsDto detail : dto.getOrderProducts()) {
                    OrderProductDetailsEntity detailEntity = new OrderProductDetailsEntity();
                    detailEntity.setOrder(entity);

                    ProductEntity productRef = session.getReference(ProductEntity.class, detail.getProduct().getId());
                    detailEntity.setProduct(productRef);

                    detailEntity.setQuantity(detail.getQuantity());
                    detailEntity.setUnitPrice(detail.getUnitPrice());
                    orderProducts.add(detailEntity);
                }
            }

            session.merge(entity);

            if (dto.getBalance() < 0 && !AppConstants.DEMO_CUSTOMER_ID.equals(dto.getCustomer().getCustId())) {
                CustomerEntity customerEntity = session.get(CustomerEntity.class, dto.getCustomer().getCustId());
                if (customerEntity != null) {
                    customerEntity.setDueAmount(customerEntity.getDueAmount() + Math.abs(dto.getBalance()));
                    session.merge(customerEntity);

                    DuePaymentEntity duePayment = new DuePaymentEntity();
                    duePayment.setDate(dto.getDate());
                    duePayment.setCustomer(customerEntity);
                    duePayment.setOrder(entity);
                    duePayment.setAmount(Math.abs(dto.getBalance()));
                    duePayment.setNote("Due added on order update " + dto.getOrderId());
                    duePayment.setType("DUE_ADDED");
                    session.persist(duePayment);
                }
            }

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
    public boolean deleteOrder(String orderId) throws SQLException, ClassNotFoundException {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            OrderEntity order = session.get(OrderEntity.class, orderId);
            if (order != null && order.getBalance() < 0 && order.getCustomer() != null) {
                CustomerEntity customerEntity = session.get(CustomerEntity.class, order.getCustomer().getCustId());
                if (customerEntity != null) {
                    double dueToRemove = Math.abs(order.getBalance());
                    customerEntity.setDueAmount(Math.max(0, customerEntity.getDueAmount() - dueToRemove));
                    session.merge(customerEntity);
                }
            }

            List<Object[]> productQtys = session.createNativeQuery("SELECT product_id, qty FROM order_product_details WHERE order_id = :orderId")
                    .setParameter("orderId", orderId)
                    .list();
            for (Object[] row : productQtys) {
                String productId = (String) row[0];
                if (productId == null) continue;
                double qty = ((Number) row[1]).doubleValue();
                session.createNativeQuery("UPDATE product SET qty = qty + :qty WHERE id = :productId")
                        .setParameter("qty", qty)
                        .setParameter("productId", productId)
                        .executeUpdate();
            }

            session.createNativeQuery("DELETE FROM due_payment WHERE order_id = :orderId")
                   .setParameter("orderId", orderId)
                   .executeUpdate();
            session.createNativeQuery("DELETE FROM order_product_details WHERE order_id = :orderId")
                   .setParameter("orderId", orderId)
                   .executeUpdate();
            session.createNativeQuery("DELETE FROM orders WHERE orderId = :orderId")
                   .setParameter("orderId", orderId)
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
    public byte[] generateOrderBill80mm(String orderId) throws SQLException, ClassNotFoundException {
        OrderEntity order = orderDAO.searchWithProducts(orderId);
        if (order == null) return null;

        List<OrderProductDetailsEntity> details = order.getOrderProducts();
        if (details == null) details = new ArrayList<>();

        return PdfGenerator.generateOrderBill(order, details);
    }

    @Override
    public byte[] generateOrderBillA4(String orderId) throws SQLException, ClassNotFoundException {
        OrderEntity order = orderDAO.searchWithProducts(orderId);
        if (order == null) return null;

        List<OrderProductDetailsEntity> details = order.getOrderProducts();
        if (details == null) details = new ArrayList<>();

        return PdfGenerator.generateOrderBillA4Pro(order, details);
    }

    @Override
    public OrderEntity getOrderById(String orderId) throws SQLException, ClassNotFoundException {
        return orderDAO.search(orderId);
    }

    @Override
    public List<LocalDate> getOrderDatesByMonth(int year, int month) throws SQLException, ClassNotFoundException {
        return orderDAO.getOrderDatesByMonth(year, month);
    }

    @Override
    public OrderEntity getOrderWithProducts(String orderId) throws SQLException, ClassNotFoundException {
        return orderDAO.searchWithProducts(orderId);
    }

    @Override
    public double getTotalDueByCustomer(String customerId) throws SQLException, ClassNotFoundException {
        return orderDAO.getTotalDueByCustomer(customerId);
    }

    @Override
    public List<OrderEntity> getUnpaidOrdersByCustomer(String customerId) throws SQLException, ClassNotFoundException {
        return orderDAO.getUnpaidOrdersByCustomer(customerId);
    }

    @Override
    public List<OrderProductDetailsEntity> getProductDetailsByDate(LocalDate date) throws SQLException, ClassNotFoundException {
        if (date == null) return new ArrayList<>();
        return orderDAO.getOrderProductDetailsByDate(date);
    }

    @Override
    public double payCustomerDue(String customerId, double amount, String note) throws SQLException, ClassNotFoundException {
        Session session = FactoryConfiguration.getInstance().getSession();
        Transaction transaction = session.beginTransaction();
        try {
            List<OrderEntity> unpaidOrders = session.createQuery(
                "SELECT o FROM OrderEntity o WHERE o.customer.custId = :custId AND o.balance < 0 ORDER BY o.date ASC, o.orderId ASC",
                OrderEntity.class)
                .setParameter("custId", customerId)
                .list();

            CustomerEntity customerEntity = session.get(CustomerEntity.class, customerId);
            double remaining = amount;

            for (OrderEntity order : unpaidOrders) {
                if (remaining <= 0) break;
                double owed = -order.getBalance();
                double payForThisOrder = Math.min(remaining, owed);

                order.setBalance(order.getBalance() + payForThisOrder);
                order.setAmountPaid(order.getAmountPaid() + payForThisOrder);
                session.merge(order);

                if (customerEntity != null) {
                    DuePaymentEntity duePayment = new DuePaymentEntity();
                    duePayment.setDate(LocalDate.now());
                    duePayment.setCustomer(customerEntity);
                    duePayment.setOrder(order);
                    duePayment.setAmount(payForThisOrder);
                    duePayment.setNote(note != null && !note.trim().isEmpty() ? note.trim() : "Due payment received");
                    duePayment.setType("DUE_PAID");
                    session.persist(duePayment);
                }

                remaining -= payForThisOrder;
            }

            double paidAmount = amount - remaining;
            if (paidAmount > 0 && customerEntity != null) {
                customerEntity.setDueAmount(Math.max(0, customerEntity.getDueAmount() - paidAmount));
                session.merge(customerEntity);
            }

            transaction.commit();
            return remaining;
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            throw e;
        } finally {
            session.close();
        }
    }
}