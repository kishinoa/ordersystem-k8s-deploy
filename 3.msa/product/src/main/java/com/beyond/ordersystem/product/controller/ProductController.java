package com.beyond.ordersystem.product.controller;

import com.beyond.ordersystem.common.dto.CommonDto;
import com.beyond.ordersystem.product.dto.*;
import com.beyond.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@ModelAttribute ProductCreateDto dto, @RequestHeader("X-User-Email")String email) {
        Long id = productService.save(dto, email);
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
    public ResponseEntity<?> getProduct(@PathVariable(name = "id") Long id) throws InterruptedException {
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

    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDto dto) throws InterruptedException {
        Long id = productService.updateStock(dto);
        return new ResponseEntity<>(
                CommonDto.builder()
                        .result("OK")
                        .status_code(HttpStatus.OK.value())
                        .status_message("상품 재고수량 변경완료")
                        .build(),
                HttpStatus.OK
        );
    }
}
