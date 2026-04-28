package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.BOFactory;
import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.bo.custom.OrderBO;
import com.dev09.kumaracoconutoil.bo.custom.ProductBO;
import com.dev09.kumaracoconutoil.dao.custom.OrderDAO;
import com.dev09.kumaracoconutoil.dao.custom.OrderProductDetailDAO;
import com.dev09.kumaracoconutoil.dao.custom.impl.OrderDAOImpl;
import com.dev09.kumaracoconutoil.dao.custom.impl.OrderProductDetailDAOImpl;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.dto.OrderDto;
import com.dev09.kumaracoconutoil.dto.OrderProductDetailsDto;
import com.dev09.kumaracoconutoil.dto.ProductDto;
import com.dev09.kumaracoconutoil.entity.OrderEntity;
import com.dev09.kumaracoconutoil.entity.OrderProductDetailsEntity;
import com.dev09.kumaracoconutoil.entity.ProductEntity;
import com.dev09.kumaracoconutoil.util.PdfGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderBOImpl implements OrderBO {
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final OrderProductDetailDAO orderProductDetailDAO = new OrderProductDetailDAOImpl();
    private final ProductBO productBO = (ProductBO) BOFactory.getInstance().getBo(BOFactory.BOType.PRODUCT);
    private final CustomerBO customerBO = (CustomerBO) BOFactory.getInstance().getBo(BOFactory.BOType.CUSTOMER);

    @Override
    public boolean placeOrder(OrderDto dto) {
        try {
            OrderEntity entity = new OrderEntity();
            entity.setOrderId(dto.getOrderId());
            entity.setDate(dto.getDate());
            entity.setAmountPaid(dto.getAmountPaid());
            entity.setDiscount(dto.getDiscount());
            entity.setTax(dto.getTax());
            entity.setBalance(dto.getBalance());
            
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
            
            return orderDAO.save(entity);
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
            dto.setBalance(entity.getBalance());
            
            if (entity.getOrderProducts() != null) {
                dto.setOrderProducts(new ArrayList<>());
                for (OrderProductDetailsEntity op : entity.getOrderProducts()) {
                    OrderProductDetailsDto detailDto = new OrderProductDetailsDto();
                    detailDto.setQuantity(op.getQuantity());
                    detailDto.setUnitPrice(op.getUnitPrice());
                    
                    if (op.getProduct() != null) {
                        ProductDto productDto = new ProductDto();
                        productDto.setId(op.getProduct().getId());
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
            
            com.dev09.kumaracoconutoil.entity.CustomerEntity customer = new com.dev09.kumaracoconutoil.entity.CustomerEntity();
            customer.setCustId(dto.getCustomer().getCustId());
            entity.setCustomer(customer);
            
            entity.setAmountPaid(dto.getAmountPaid());
            entity.setDiscount(dto.getDiscount());
            entity.setTax(dto.getTax());
            entity.setBalance(dto.getBalance());
            
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
            
            return orderDAO.update(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteOrder(String orderId) throws SQLException, ClassNotFoundException {
        return orderDAO.delete(orderId);
    }

    @Override
    public byte[] generateOrderBill(String orderId) throws SQLException, ClassNotFoundException {
        OrderEntity order = orderDAO.search(orderId);
        if (order == null) return null;

        List<OrderProductDetailsEntity> details = order.getOrderProducts();
        if (details == null) details = new ArrayList<>();

        return PdfGenerator.generateOrderBill(order, details);
    }

    @Override
    public OrderEntity getOrderById(String orderId) throws SQLException, ClassNotFoundException {
        return orderDAO.search(orderId);
    }
}