package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

//A line item inside an Orders - keeps a price "snapshot" so later price
//changes on the Product don't rewrite historical orders.
@Entity
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Orders orders;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private double priceAtPurchase;

}
