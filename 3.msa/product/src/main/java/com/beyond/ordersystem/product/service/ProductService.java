package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.common.service.S3Uploader;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.*;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Uploader s3Uploader;

    public Long save(ProductCreateDto dto, String mamberEmail) {
//        String imageUrl = s3Uploader.upload(dto.getProductImage(), "product");
        Product product = productRepository.save(dto.toEntity(mamberEmail));

        return product.getId();
    }

    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto dto) {
        Specification<Product>specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                if(dto.getProductName() != null) {
                    predicateList.add(criteriaBuilder.like(root.get("name"), '%' + dto.getProductName() + '%'));
                }
                if(dto.getCategory() != null) {
                    predicateList.add(criteriaBuilder.equal(root.get("category"), dto.getCategory()));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                for(int i = 0; i < predicateList.size(); i++) {
                    predicateArr[i] = predicateList.get(i);
                }

                Predicate predicate = criteriaBuilder.and(predicateArr);

                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification, pageable);

        return productList.map(a -> ProductResDto.fromEntity(a));
    }

    public ProductResDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 제품은 없습니다."));
        return ProductResDto.fromEntity(product);
    }

    public void updateProduct(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다."));
        s3Uploader.delete(product.getImagePath());
        String imageUrl = s3Uploader.upload(dto.getProductImage(), "product");
        product.updateProduct(dto, imageUrl);
    }

    public Long updateStock(ProductUpdateStockDto dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        if(product.getStockQuantity() < dto.getProductCount()) {
            throw new IllegalArgumentException("재고부족");
        }

        product.decreaseQuantity(dto.getProductCount());
        return product.getId();
    }
}
