package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private int  price;
    private int stockQuantity;
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void updateProduct(ProductUpdateDto dto, String imageUrl) {
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.price = dto.getPrice();
        this.stockQuantity = dto.getStockQuantity();
        this.imagePath = imageUrl;
    }

    public void decreaseQuantity(int newQuantity) {
        this.stockQuantity -= newQuantity;
    }

    public void increaseQuantity(int newQuantity) {
        this.stockQuantity += newQuantity;
    }
}
