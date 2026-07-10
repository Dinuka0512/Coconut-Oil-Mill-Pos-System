package com.dev09.kumaracoconutoil.bo.custom.impl;

import com.dev09.kumaracoconutoil.bo.custom.CustomerBO;
import com.dev09.kumaracoconutoil.dao.DAOFactory;
import com.dev09.kumaracoconutoil.dao.custom.CustomerDAO;
import com.dev09.kumaracoconutoil.dto.CustomerDto;
import com.dev09.kumaracoconutoil.entity.CustomerEntity;

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
                    customer.getContact(),
                    customer.getDueAmount()
            ));
        }
        return allCustomers;
    }

    @Override
    public boolean saveCustomer(CustomerDto dto) {
        CustomerEntity entity = new CustomerEntity(
                dto.getCustId(),
                dto.getName(),
                dto.getContact(),
                false,
                0
        );
        return customerDAO.save(entity);
    }

    @Override
    public boolean updateCustomer(CustomerDto dto) {
        CustomerEntity existing = customerDAO.search(dto.getCustId());
        double currentDue = 0;
        if (existing != null) {
            currentDue = existing.getDueAmount();
        }
        CustomerEntity entity = new CustomerEntity(
                dto.getCustId(),
                dto.getName(),
                dto.getContact(),
                false,
                currentDue
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
                    customer.getContact(),
                    customer.getDueAmount()
            );
        }
        return null;
    }

    @Override
    public ArrayList<CustomerEntity> getAllCustomersEntity() {
        return customerDAO.getAll();
    }
}