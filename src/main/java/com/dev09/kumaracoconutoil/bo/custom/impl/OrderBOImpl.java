package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.dao.custom.OrderProductDetailDAO;
import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.dao.custom.ProductDAO;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OrderBOImpl implements OrderBO {
    private OrderDAO orderDAO = (OrderDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.ORDER);
    private OrderProductDetailDAO orderProductDetailDAO = (OrderProductDetailDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.ORDER_PRODUCT_DETAIL);
    private ProductDAO productDAO = (ProductDAO) DAOFactory.getInstance().getDAO(DAOFactory.DAOType.PRODUCT);

    @Override
    public boolean placeOrder(OrderDto dto) throws SQLException, ClassNotFoundException {
        // Mapping DTO to Entity
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setCustId(dto.getCustomer().getCustId());
        customerEntity.setName(dto.getCustomer().getName());
        customerEntity.setContact(dto.getCustomer().getContact());

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(dto.getOrderId());
        orderEntity.setDate(dto.getDate());
        orderEntity.setCustomer(customerEntity);

        org.hibernate.Session session = com.dev09.kumaracoconutoil.config.FactoryConfiguration.getInstance().getSession();
        org.hibernate.Transaction transaction = session.beginTransaction();

        try {
            // 1. Save Order
            session.persist(orderEntity);

            // 2. Save Order Details and Update Product Qty
            for (OrderProductDetailsDto detailDto : dto.getOrderProducts()) {
                OrderProductDetailsEntity detailEntity = new OrderProductDetailsEntity();
                detailEntity.setId(detailDto.getId());
                detailEntity.setOrder(orderEntity);
                
                ProductEntity productEntity = session.get(ProductEntity.class, detailDto.getProduct().getId());
                if (productEntity == null) throw new RuntimeException("Product not found");

                detailEntity.setProduct(productEntity);
                detailEntity.setQuantity(detailDto.getQuantity());
                detailEntity.setUnitPrice(detailDto.getUnitPrice());

                session.persist(detailEntity);

                // Update Product Quantity
                productEntity.setQty(productEntity.getQty() - detailDto.getQuantity());
                session.merge(productEntity);
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
        // Implementation for retrieving all orders if needed for a history view
        return new ArrayList<>();
    }
}
