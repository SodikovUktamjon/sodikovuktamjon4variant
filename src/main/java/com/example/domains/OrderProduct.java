package com.example.domains;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Embeddable
@Table(name = "order_products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderProduct {
    @Column(name = "order_product_quantity")
    private int quantity;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;
}