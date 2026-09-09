package io.virinchi.ekart.Repository;

import io.virinchi.ekart.Model.CartItem;
import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByUser(UserTbl user);

    Optional<CartItem> findByUserAndProduct(UserTbl user, Product product);

    void deleteByUser(UserTbl user);

}
