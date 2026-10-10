package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.ProductRequestDTO;
import com.ecommerce.product.dto.ProductResponseDTO;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        log.info("Tạo mới sản phẩm: {}", requestDTO.getName());

        Product product = Product.builder()
                .name(requestDTO.getName().trim())
                .price(requestDTO.getPrice())
                .stockQuantity(requestDTO.getStockQuantity())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Đã lưu sản phẩm thành công với ID: {}", savedProduct.getId());

        return mapToResponseDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        log.info("Lấy chi tiết sản phẩm ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm với ID " + id + " không tồn tại!"));

        return mapToResponseDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        log.info("Lấy danh sách tất cả sản phẩm");

        return productRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
