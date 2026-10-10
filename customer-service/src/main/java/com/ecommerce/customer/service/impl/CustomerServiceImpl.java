package com.ecommerce.customer.service.impl;

import com.ecommerce.customer.dto.CustomerLoginDTO;
import com.ecommerce.customer.dto.CustomerRequestDTO;
import com.ecommerce.customer.dto.CustomerResponseDTO;
import com.ecommerce.customer.entity.Customer;
import com.ecommerce.customer.exception.DuplicateResourceException;
import com.ecommerce.customer.exception.InvalidCredentialsException;
import com.ecommerce.customer.exception.ResourceNotFoundException;
import com.ecommerce.customer.repository.CustomerRepository;
import com.ecommerce.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CustomerResponseDTO register(CustomerRequestDTO requestDTO) {
        log.info("Xử lý đăng ký tài khoản với email: {}", requestDTO.getEmail());

        if (customerRepository.existsByEmail(requestDTO.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("Email '" + requestDTO.getEmail() + "' đã tồn tại!");
        }

        String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());

        Customer customer = Customer.builder()
                .fullName(requestDTO.getFullName().trim())
                .email(requestDTO.getEmail().trim().toLowerCase())
                .password(encodedPassword)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Đăng ký thành công khách hàng với ID: {}", savedCustomer.getId());

        return mapToResponseDTO(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        log.info("Tìm kiếm thông tin khách hàng ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng với ID " + id + " không tồn tại!"));

        return mapToResponseDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDTO login(CustomerLoginDTO loginDTO) {
        log.info("Xác thực thông tin đăng nhập cho email: {}", loginDTO.getEmail());

        Customer customer = customerRepository.findByEmail(loginDTO.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("email or password incorrect"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("email or password incorrect");
        }

        log.info("Đăng nhập thành công cho khách hàng ID: {}", customer.getId());
        return mapToResponseDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .build();
    }
}
