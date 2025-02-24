    package com.example.domains;

    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.GenericGenerator;

    import java.time.LocalDateTime;
    import java.util.List;

    @Entity
    @Table(name = "orders")
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public class Order {
        @Id
        @GenericGenerator(name = "uuid2", strategy = "uuid2")
        @GeneratedValue(generator = "uuid2")
        private String id;
        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(
                name = "order_products",
                joinColumns = @JoinColumn(name = "order_id")
        )
        private List<OrderProduct> productsList;
        @CreationTimestamp
        @Column(updatable = false, name = "order_date")
        private LocalDateTime orderDate;
        @Column(name = "address")
        private String address;
        @Column(name = "total_price", nullable = false)
        private long totalPrice;
    }

