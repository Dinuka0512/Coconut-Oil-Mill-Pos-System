package com.dev09.kumaracoconutoil.bo.custom;

import com.dev09.kumaracoconutoil.bo.SuperBo;
import com.dev09.kumaracoconutoil.dto.CustomerDto;

import java.util.ArrayList;

public interface CustomerBO extends SuperBo {
    ArrayList<CustomerDto> getAllCustomers();
    boolean saveCustomer(CustomerDto dto);
    boolean updateCustomer(CustomerDto dto);
    boolean existCustomer(String id);
    boolean deleteCustomer(String id);
    String generateNewCustomerId();
    CustomerDto searchCustomer(String id);

    byte[] exportAllCustomers();
}
