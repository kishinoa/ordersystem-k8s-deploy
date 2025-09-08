package com.beyond.ordersystem.product.dto;

import com.beyond.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateDto {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
//    private MultipartFile productImage;

    public Product toEntity(String memberEmail) {
        return Product.builder()
                .name(this.name)
                .category(this.category)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .memberEmail(memberEmail)
//                .imagePath(imageUrl)
                .build();
    }
}
