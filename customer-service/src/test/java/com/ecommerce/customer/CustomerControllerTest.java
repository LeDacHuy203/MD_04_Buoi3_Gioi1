package com.ecommerce.customer;

import com.ecommerce.customer.dto.CustomerLoginDTO;
import com.ecommerce.customer.dto.CustomerRequestDTO;
import com.ecommerce.customer.dto.CustomerResponseDTO;
import com.ecommerce.customer.exception.InvalidCredentialsException;
import com.ecommerce.customer.exception.ResourceNotFoundException;
import com.ecommerce.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @DisplayName("GET /api/v1/customers/99 - ID không tồn tại phải trả về 404 và ApiResponseError theo đúng yêu cầu đề bài")
    void testGetCustomerById_NotFound() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new ResourceNotFoundException("Khách hàng với ID 99 không tồn tại!"));

        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Khách hàng với ID 99 không tồn tại!"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers/register - Đăng ký thành công trả về 201 Created và không có mật khẩu")
    void testRegisterCustomer_Success() throws Exception {
        CustomerRequestDTO requestDTO = CustomerRequestDTO.builder()
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@gmail.com")
                .password("123456")
                .build();

        CustomerResponseDTO responseDTO = CustomerResponseDTO.builder()
                .id(1L)
                .fullName("Nguyễn Văn A")
                .email("nguyenvana@gmail.com")
                .build();

        when(customerService.register(any(CustomerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/customers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.email").value("nguyenvana@gmail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/v1/customers/login - Sai mật khẩu phải trả về lỗi 'email or password incorrect'")
    void testLogin_IncorrectPassword() throws Exception {
        CustomerLoginDTO loginDTO = CustomerLoginDTO.builder()
                .email("nguyenvana@gmail.com")
                .password("wrongpassword")
                .build();

        when(customerService.login(any(CustomerLoginDTO.class)))
                .thenThrow(new InvalidCredentialsException("email or password incorrect"));

        mockMvc.perform(put("/api/v1/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("email or password incorrect"));
    }
}
