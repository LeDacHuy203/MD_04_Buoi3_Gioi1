package com.ecommerce.customer.service;

import com.ecommerce.customer.dto.CustomerLoginDTO;
import com.ecommerce.customer.dto.CustomerRequestDTO;
import com.ecommerce.customer.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerResponseDTO register(CustomerRequestDTO requestDTO);

    CustomerResponseDTO getCustomerById(Long id);

    CustomerResponseDTO login(CustomerLoginDTO loginDTO);

    List<CustomerResponseDTO> getAllCustomers();
}
