package com.ecommerce.customer.controller;

import com.ecommerce.customer.dto.CustomerLoginDTO;
import com.ecommerce.customer.dto.CustomerRequestDTO;
import com.ecommerce.customer.dto.CustomerResponseDTO;
import com.ecommerce.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs quản lý khách hàng")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký khách hàng mới (mã hóa mật khẩu)")
    public ResponseEntity<CustomerResponseDTO> register(@Valid @RequestBody CustomerRequestDTO requestDTO) {
        CustomerResponseDTO response = customerService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin khách hàng theo ID (không bao gồm mật khẩu)")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        CustomerResponseDTO response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/login")
    @Operation(summary = "Đăng nhập khách hàng")
    public ResponseEntity<CustomerResponseDTO> login(@Valid @RequestBody CustomerLoginDTO loginDTO) {
        CustomerResponseDTO response = customerService.login(loginDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả khách hàng")
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> responses = customerService.getAllCustomers();
        return ResponseEntity.ok(responses);
    }
}
