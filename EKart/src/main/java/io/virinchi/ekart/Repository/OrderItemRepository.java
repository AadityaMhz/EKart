package io.virinchi.ekart.Repository;

import io.virinchi.ekart.Model.OrderItem;
import io.virinchi.ekart.Model.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    //Used by the seller dashboard (admin.html) to show only orders/customers
    //that involve this seller's own products.
    List<OrderItem> findByProduct_Seller(UserTbl seller);

}
