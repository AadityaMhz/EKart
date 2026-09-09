package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Named "Orders" (not "Order") because ORDER is a reserved SQL keyword.
//Created at checkout from the buyer's cart - order-confirm.html / profile order history.
@Entity
@Data
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserTbl user;

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(length = 500)
    private String shippingAddress;

    private String couponCode; //e.g. "SPIN10", won from the Spin & Win widget
    private double discountAmount; //Rs. amount taken off the subtotal

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

}
