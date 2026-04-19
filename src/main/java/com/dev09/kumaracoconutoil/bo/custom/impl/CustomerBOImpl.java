package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.CustomerDAO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;
import com.dev09.kumaracoconutoil.util.PdfGenerator;

import java.util.ArrayList;

public class CustomerBOImpl implements CustomerBO {
    private CustomerDAO customerDAO = (CustomerDAO) DAOFactory.getInstance()
            .getDAO(DAOFactory.DAOType.CUSTOMER);

    @Override
    public ArrayList<CustomerDto> getAllCustomers() {
        ArrayList<CustomerEntity> all = customerDAO.getAll();
        ArrayList<CustomerDto> allCustomers = new ArrayList<>();
        for (CustomerEntity customer : all) {
            allCustomers.add(new CustomerDto(
                    customer.getCustId(),
                    customer.getName(),
                    customer.getContact()
            ));
        }
        return allCustomers;
    }

    @Override
    public boolean saveCustomer(CustomerDto dto) {
        // Pass all 5 arguments: custId, name, contact, isDeleted=false, orders=null
        CustomerEntity entity = new CustomerEntity(
                dto.getCustId(),
                dto.getName(),
                dto.getContact(),
                false,   // isDeleted default false
                null     // orders list can be null or empty
        );
        return customerDAO.save(entity);
    }

    @Override
    public boolean updateCustomer(CustomerDto dto) {
        // Same as save – all fields must be provided
        CustomerEntity entity = new CustomerEntity(
                dto.getCustId(),
                dto.getName(),
                dto.getContact(),
                false,
                null
        );
        return customerDAO.update(entity);
    }

    @Override
    public boolean existCustomer(String id) {
        return customerDAO.exist(id);
    }

    @Override
    public boolean deleteCustomer(String id) {
        return customerDAO.delete(id);
    }

    @Override
    public String generateNewCustomerId() {
        return customerDAO.generateNewId();
    }

    @Override
    public CustomerDto searchCustomer(String id) {
        CustomerEntity customer = customerDAO.search(id);
        if (customer != null) {
            return new CustomerDto(
                    customer.getCustId(),
                    customer.getName(),
                    customer.getContact()
            );
        }
        return null;
    }

    @Override
    public byte[] exportAllCustomers() {
        ArrayList<CustomerEntity> all = customerDAO.getAll();
        return PdfGenerator.exportAllCustomers(all);
    }
}