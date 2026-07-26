package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.DuePaymentBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.dao.custom.OrderProductDetailDAO;
import com.dev09.kumaracoconutoil.dao.custom.impl.OrderDAOImpl;
import com.dev09.kumaracoconutoil.dao.custom.impl.OrderProductDetailDAOImpl;
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderBOImpl implements OrderBO {
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final OrderProductDetailDAO orderProductDetailDAO = new OrderProductDetailDAOImpl();
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);
    private final DuePaymentBO duePaymentBO = (DuePaymentBO) BOFactory.getInstance().getBo(BOFactory.BOType.DUE_PAYMENT);

    @Override
    public boolean placeOrder(OrderDto dto) {
        try {
            OrderEntity entity = new OrderEntity();
            entity.setOrderId(dto.getOrderId());
            entity.setDate(dto.getDate());
            entity.setAmountPaid(dto.getAmountPaid());
            entity.setDiscount(dto.getDiscount());
            entity.setTax(dto.getTax());
            entity.setTaxRate(dto.getTaxRate()); // Save tax rate
            entity.setBalance(dto.getBalance());
            entity.setPaymentMethod(dto.getPaymentMethod());
            
            com.dev09.kumaracoconutoil.entity.CustomerEntity customer = new com.dev09.kumaracoconutoil.entity.CustomerEntity();
            customer.setCustId(dto.getCustomer().getCustId());
            entity.setCustomer(customer);
            
            ArrayList<com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity> details = new ArrayList<>();
            if (dto.getOrderProducts() != null) {
                for (OrderProductDetailsDto detail : dto.getOrderProducts()) {
                    com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity detailEntity = 
                        new com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity();
                    detailEntity.setOrder(entity);
                    
                    com.dev09.kumaracoconutoil.entity.ProductEntity product = 
                        new com.dev09.kumaracoconutoil.entity.ProductEntity();
                    product.setId(detail.getProduct().getId());
                    detailEntity.setProduct(product);
                    
                    detailEntity.setQuantity(detail.getQuantity());
                    detailEntity.setUnitPrice(detail.getUnitPrice());
                    details.add(detailEntity);
                    
                    productBO.updateStock(detail.getProduct().getId(), detail.getQuantity());
                }
            }
            entity.setOrderProducts(details);
            
            boolean saved = orderDAO.save(entity);

            if (saved && dto.getBalance() < 0 && !"CUST001".equals(dto.getCustomer().getCustId())) {
                try {
                    com.dev09.kumaracoconutoil.dao.custom.CustomerDAO customerDAO =
                        (com.dev09.kumaracoconutoil.dao.custom.CustomerDAO) DAOFactory.getInstance()
                            .getDAO(DAOFactory.DAOType.CUSTOMER);
                    CustomerEntity customerEntity = customerDAO.search(dto.getCustomer().getCustId());
                    if (customerEntity != null) {
                        customerEntity.setDueAmount(customerEntity.getDueAmount() + Math.abs(dto.getBalance()));
                        customerDAO.update(customerEntity);

                        DuePaymentEntity duePayment = new DuePaymentEntity();
                        duePayment.setDate(dto.getDate());
                        duePayment.setCustomer(customerEntity);
                        duePayment.setOrder(entity);
                        duePayment.setAmount(Math.abs(dto.getBalance()));
                        duePayment.setNote("Due added on order " + dto.getOrderId());
                        duePayment.setType("DUE_ADDED");
                        duePaymentBO.saveDuePayment(duePayment);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return saved;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
                CustomerDto customerDto = customerBO.searchCustomer(entity.getCustomer().getCustId());
                if (customerDto != null) {
                    dto.setCustomer(customerDto);
                } else {
                    CustomerDto deletedCustomer = new CustomerDto();
                    deletedCustomer.setCustId(entity.getCustomer().getCustId());
                    deletedCustomer.setName("Unknown");
                    deletedCustomer.setContact("N/A");
                    dto.setCustomer(deletedCustomer);
                }
            }
            
            dto.setAmountPaid(entity.getAmountPaid());
            dto.setDiscount(entity.getDiscount());
            dto.setTax(entity.getTax());
            dto.setTaxRate(entity.getTaxRate());
            dto.setBalance(entity.getBalance());
            dto.setPaymentMethod(entity.getPaymentMethod());
            
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
        try {
            OrderEntity entity = orderDAO.searchWithProducts(dto.getOrderId());
            if (entity == null) return false;

            // 1. Delete only DUE_ADDED records (keep DUE_PAID records as history)
            duePaymentBO.deleteDueAddedByOrderId(dto.getOrderId());

            // 2. If old order had due (balance < 0), subtract from customer's dueAmount
            if (entity.getBalance() < 0 && entity.getCustomer() != null) {
                com.dev09.kumaracoconutoil.dao.custom.CustomerDAO customerDAO =
                    (com.dev09.kumaracoconutoil.dao.custom.CustomerDAO) DAOFactory.getInstance()
                        .getDAO(DAOFactory.DAOType.CUSTOMER);
                CustomerEntity customerEntity = customerDAO.search(entity.getCustomer().getCustId());
                if (customerEntity != null) {
                    double oldDue = Math.abs(entity.getBalance());
                    double newDueAmount = Math.max(0, customerEntity.getDueAmount() - oldDue);
                    customerEntity.setDueAmount(newDueAmount);
                    customerDAO.update(customerEntity);
                }
            }

            // 3. Update order entity fields
            com.dev09.kumaracoconutoil.entity.CustomerEntity customer = new com.dev09.kumaracoconutoil.entity.CustomerEntity();
            customer.setCustId(dto.getCustomer().getCustId());
            entity.setCustomer(customer);

            entity.setAmountPaid(dto.getAmountPaid());
            entity.setDiscount(dto.getDiscount());
            entity.setTax(dto.getTax());
            entity.setBalance(dto.getBalance());
            entity.setPaymentMethod(dto.getPaymentMethod());

            if (entity.getOrderProducts() != null) {
                entity.getOrderProducts().clear();
            }

            if (dto.getOrderProducts() != null) {
                ArrayList<com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity> details = new ArrayList<>();
                for (OrderProductDetailsDto detail : dto.getOrderProducts()) {
                    com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity detailEntity = 
                        new com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity();
                    detailEntity.setOrder(entity);
                    
                    com.dev09.kumaracoconutoil.entity.ProductEntity product = 
                        new com.dev09.kumaracoconutoil.entity.ProductEntity();
                    product.setId(detail.getProduct().getId());
                    detailEntity.setProduct(product);
                    
                    detailEntity.setQuantity(detail.getQuantity());
                    detailEntity.setUnitPrice(detail.getUnitPrice());
                    details.add(detailEntity);
                }
                entity.setOrderProducts(details);
            }

            boolean updated = orderDAO.update(entity);

            // 4. If customer is NOT CUST001 and new balance < 0, create new DUE_ADDED record
            if (updated && dto.getBalance() < 0 && !"CUST001".equals(dto.getCustomer().getCustId())) {
                try {
                    com.dev09.kumaracoconutoil.dao.custom.CustomerDAO customerDAO =
                        (com.dev09.kumaracoconutoil.dao.custom.CustomerDAO) DAOFactory.getInstance()
                            .getDAO(DAOFactory.DAOType.CUSTOMER);
                    CustomerEntity customerEntity = customerDAO.search(dto.getCustomer().getCustId());
                    if (customerEntity != null) {
                        customerEntity.setDueAmount(customerEntity.getDueAmount() + Math.abs(dto.getBalance()));
                        customerDAO.update(customerEntity);

                        DuePaymentEntity duePayment = new DuePaymentEntity();
                        duePayment.setDate(dto.getDate());
                        duePayment.setCustomer(customerEntity);
                        duePayment.setOrder(entity);
                        duePayment.setAmount(Math.abs(dto.getBalance()));
                        duePayment.setNote("Due added on order update " + dto.getOrderId());
                        duePayment.setType("DUE_ADDED");
                        duePaymentBO.saveDuePayment(duePayment);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return updated;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteOrder(String orderId) throws SQLException, ClassNotFoundException {
        // Before deleting, reduce customer.dueAmount if this order had a due
        OrderEntity order = orderDAO.search(orderId);
        if (order != null && order.getBalance() < 0 && order.getCustomer() != null) {
            try {
                com.dev09.kumaracoconutoil.dao.custom.CustomerDAO customerDAO =
                    (com.dev09.kumaracoconutoil.dao.custom.CustomerDAO) DAOFactory.getInstance()
                        .getDAO(DAOFactory.DAOType.CUSTOMER);
                CustomerEntity customerEntity = customerDAO.search(order.getCustomer().getCustId());
                if (customerEntity != null) {
                    double dueToRemove = Math.abs(order.getBalance());
                    double newDueAmount = Math.max(0, customerEntity.getDueAmount() - dueToRemove);
                    customerEntity.setDueAmount(newDueAmount);
                    customerDAO.update(customerEntity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return orderDAO.delete(orderId);
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
    public double payCustomerDue(String customerId, double amount, String note) throws SQLException, ClassNotFoundException {
        List<OrderEntity> unpaidOrders = orderDAO.getUnpaidOrdersByCustomer(customerId);
        double remaining = amount;

        // Get customer entity once
        com.dev09.kumaracoconutoil.dao.custom.CustomerDAO customerDAO =
            (com.dev09.kumaracoconutoil.dao.custom.CustomerDAO) DAOFactory.getInstance()
                .getDAO(DAOFactory.DAOType.CUSTOMER);
        CustomerEntity customerEntity = customerDAO.search(customerId);

        // Pay off orders oldest-first, create a DUE_PAID record per order
        for (OrderEntity order : unpaidOrders) {
            if (remaining <= 0) break;
            double owed = -order.getBalance();
            double payForThisOrder = Math.min(remaining, owed);

            order.setBalance(order.getBalance() + payForThisOrder);
            orderDAO.update(order);

            // Create DUE_PAID record linked to this specific order
            if (customerEntity != null) {
                DuePaymentEntity duePayment = new DuePaymentEntity();
                duePayment.setDate(LocalDate.now());
                duePayment.setCustomer(customerEntity);
                duePayment.setOrder(order);
                duePayment.setAmount(payForThisOrder);
                duePayment.setNote(note != null && !note.trim().isEmpty() ? note.trim() : "Due payment received");
                duePayment.setType("DUE_PAID");
                duePaymentBO.saveDuePayment(duePayment);
            }

            remaining -= payForThisOrder;
        }

        // Update customer.dueAmount
        double paidAmount = amount - remaining;
        if (paidAmount > 0 && customerEntity != null) {
            double newDue = customerEntity.getDueAmount() - paidAmount;
            customerEntity.setDueAmount(Math.max(0, newDue));
            customerDAO.update(customerEntity);
        }

        return remaining;
    }
}