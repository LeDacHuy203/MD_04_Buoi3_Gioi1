package com.ecommerce.product;

import com.ecommerce.product.dto.ProductRequestDTO;
import com.ecommerce.product.dto.ProductResponseDTO;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/v1/products - Với price = -500, hệ thống phải trả về 400 Bad Request với định dạng ApiResponseError")
    void testCreateProduct_NegativePrice_Returns400() throws Exception {
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("Tai nghe Bluetooth")
                .price(BigDecimal.valueOf(-500))
                .stockQuantity(10)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Giá sản phẩm phải lớn hơn 0")))
                .andExpect(jsonPath("$.timestamp").exists());

        // Đảm bảo logic lưu DB không bao giờ được gọi
        verify(productService, never()).createProduct(any(ProductRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/products - Tên sản phẩm để trống (@NotBlank), trả về 400 Bad Request")
    void testCreateProduct_BlankName_Returns400() throws Exception {
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("")
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(5)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Tên sản phẩm không được để trống")));
    }

    @Test
    @DisplayName("POST /api/v1/products - Số lượng tồn kho âm (@Min(0)), trả về 400 Bad Request")
    void testCreateProduct_NegativeStock_Returns400() throws Exception {
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("Bàn phím cơ")
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(-1)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Số lượng tồn kho không được âm")));
    }

    @Test
    @DisplayName("POST /api/v1/products - Dữ liệu hợp lệ trả về 201 Created và ProductResponseDTO")
    void testCreateProduct_Success() throws Exception {
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .name("Bàn phím cơ DareU EK87")
                .price(BigDecimal.valueOf(890000))
                .stockQuantity(50)
                .build();

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .id(1L)
                .name("Bàn phím cơ DareU EK87")
                .price(BigDecimal.valueOf(890000))
                .stockQuantity(50)
                .build();

        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bàn phím cơ DareU EK87"))
                .andExpect(jsonPath("$.price").value(890000))
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    @DisplayName("GET /api/v1/products/99 - ID không tồn tại trả về 404 Not Found")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Sản phẩm với ID 99 không tồn tại!"));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Sản phẩm với ID 99 không tồn tại!"));
    }

    @Test
    @DisplayName("GET /api/v1/products - Lấy danh sách toàn bộ sản phẩm")
    void testGetAllProducts() throws Exception {
        ProductResponseDTO p1 = ProductResponseDTO.builder()
                .id(1L)
                .name("Sản phẩm 1")
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(10)
                .build();

        when(productService.getAllProducts()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Sản phẩm 1"));
    }
}
