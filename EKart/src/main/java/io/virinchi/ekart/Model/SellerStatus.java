package io.virinchi.ekart.Model;

//Tracks a buyer's "Become a Seller" request (becomesellerrr.html)
public enum SellerStatus {
    NONE,       //never applied
    PENDING,    //applied, waiting for approval
    APPROVED,   //approved -> role upgraded to SELLER
    REJECTED
}
