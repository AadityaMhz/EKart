package io.virinchi.ekart.Repository;

import io.virinchi.ekart.Model.Role;
import io.virinchi.ekart.Model.SellerStatus;
import io.virinchi.ekart.Model.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserTbl, Integer> {

    boolean existsByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    UserTbl findByUsername(String username);

    List<UserTbl> findByRole(Role role);

    List<UserTbl> findBySellerStatus(SellerStatus sellerStatus);

}
