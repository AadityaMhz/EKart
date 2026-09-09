package io.virinchi.ekart.Model;

import jakarta.persistence.*;
import lombok.Data;

//@Entity creates the table with provided name in database
//UserTbl -> user_tbl
@Entity
@Data
public class UserTbl {

    @Id //primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //Generated Value- Auto Increment
    private int id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password; //stored as MD5 hash, same pattern as SpringWebVir template

    private String fullName;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role = Role.BUYER; //default role on signup

    @Enumerated(EnumType.STRING)
    private SellerStatus sellerStatus = SellerStatus.NONE; //becomeSeller() workflow

    private String businessName; //filled in when applying to become a seller

}
