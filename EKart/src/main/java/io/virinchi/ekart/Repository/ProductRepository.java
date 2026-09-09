package io.virinchi.ekart.Repository;

import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByActiveTrue();

    List<Product> findBySeller(UserTbl seller);

    List<Product> findByCategory_NameAndActiveTrue(String categoryName);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);

}
