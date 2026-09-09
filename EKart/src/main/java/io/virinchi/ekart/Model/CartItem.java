package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

//One row per (user, product) pair in a shopper's cart - cart.html
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

}
