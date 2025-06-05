package com.sun.wineshop.model.entity;

import com.sun.wineshop.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "orderItems")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;
    Double totalAmount;
    String recipientName;
    String address;
    String phoneNumber;
    String rejectReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    OrderStatus status;

    @CreationTimestamp
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    List<OrderItem> orderItems = new ArrayList<>();
}
