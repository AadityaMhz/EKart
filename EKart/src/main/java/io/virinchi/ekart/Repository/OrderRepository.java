package io.virinchi.ekart.Repository;

import io.virinchi.ekart.Model.Orders;
import io.virinchi.ekart.Model.UserTbl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {

    List<Orders> findByUserOrderByOrderDateDesc(UserTbl user);

    List<Orders> findAllByOrderByOrderDateDesc();

}
