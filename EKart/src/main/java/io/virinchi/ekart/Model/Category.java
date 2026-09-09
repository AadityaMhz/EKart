package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

//Matches CATEGORIES in js/products.js (Men, Women, Kids, Footwear, Sports, Electronics, Accessories, Home)
//and the "Add Category" form in admin.html
@Entity
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String name;

    private String icon; //font-awesome class, e.g. "fa-shirt"

}
