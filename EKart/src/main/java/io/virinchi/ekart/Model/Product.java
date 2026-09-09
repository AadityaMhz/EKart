package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

//Fields mirror the PRODUCTS array in js/products.js: id, name, price, oldPrice,
//category, image, badge, rating, reviews, desc - plus stock and the seller who owns it.
@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(length = 2000)
    private String description;

    private double price;
    private Double oldPrice; //nullable - shown as strikethrough when on sale

    private int stock;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String image; //Base64 encoded, same technique as VirImgTable in SpringWebVir

    private String badge; //"SALE" / "NEW" / null

    //Font-awesome class used as a placeholder tile when a product has no
    //uploaded photo yet - same idea as the icon-only entries in the
    //original js/products.js catalog (e.g. "fa-headphones").
    private String icon;

    private double rating;
    private int reviews;

    //The seller (ADMIN role user) who listed this product
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private UserTbl seller;

    private boolean active = true; //soft-delete flag used by admin/superadmin panels

}
