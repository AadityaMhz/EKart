package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

// One row per (user, product, color) combination in a shopper's cart.
@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserTbl user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    // Selected product color
    @Column(nullable = true)
    private String color;
}