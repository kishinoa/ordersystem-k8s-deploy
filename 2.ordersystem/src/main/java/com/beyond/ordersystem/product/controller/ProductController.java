package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.product.dto.ProductCreateDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.dto.ProductUpdateDto;
import com.beyond.ordersystem.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@ModelAttribute ProductCreateDto dto) {
        Long id = productService.save(dto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(id+"번 상품")
                        .status_code(HttpStatus.CREATED.value())
                        .status_message("상품등록 완료")
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getProducts(@PageableDefault(size = 10)Pageable pageable, ProductSearchDto dto) {
        Page<ProductResDto> products = productService.findAll(pageable, dto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(products)
                        .status_code(HttpStatus.OK.value())
                        .status_message("상품 목록 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getProduct(@PathVariable(name = "id") Long id) {
        ProductResDto product = productService.findById(id);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result(product)
                        .status_code(HttpStatus.OK.value())
                        .status_message("상품 조회")
                        .build(),
                HttpStatus.OK
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @ModelAttribute ProductUpdateDto dto) {
        productService.updateProduct(id, dto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("수정 성공")
                        .build(),
                HttpStatus.OK
        );
    }
}
