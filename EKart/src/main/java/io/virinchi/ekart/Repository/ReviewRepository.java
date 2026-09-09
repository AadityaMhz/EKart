package io.virinchi.ekart.Repository;

import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.Review;
import io.virinchi.ekart.Model.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    boolean existsByUserAndProduct(UserTbl user, Product product);

}
