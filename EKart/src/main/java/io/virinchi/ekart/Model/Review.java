package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

//A buyer's written review + star rating for a Product - product-details.html.
//Product.rating / Product.reviews are recalculated from real rows here
//every time a new review is added, instead of staying at their seeded values.
@Entity
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserTbl user;

    private int rating; //1-5

    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

}
