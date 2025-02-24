package com.example.domains;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    private String id;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "price",nullable = false)
    private long price;
    @Column(name="product_data", nullable = false)
    private String productData;
    @Column(name="image_path")
    private String imagePath;
    @Column(name = "name_uz",nullable = false)
    private String nameUz;
    @Column(name = "description_uz", nullable = false)
    private String descriptionUz;
    @Column(name="name_ru", nullable = false)
    private String nameRu;
    @Column(name="description_ru", nullable = false)
    private String descriptionRu;
}