package io.virinchi.ekart.Model;

//Every user account carries one of these roles.
//BUYER      -> normal shopper (default on signup)
//SELLER     -> approved to list products (after becomeSeller request is APPROVED);
//              manages their own products/orders/customers/category from admin.html
//SUPERADMIN -> manages the whole platform: all users, seller approvals, system (superadmin.html)
public enum Role {
    BUYER,
    SELLER,
    SUPERADMIN
}
