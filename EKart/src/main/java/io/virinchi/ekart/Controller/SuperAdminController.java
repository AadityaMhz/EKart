package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.*;
import io.virinchi.ekart.Repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Platform-wide control panel - superadmin.html: all users, seller-application
//approvals, and a bird's-eye view of every product/order in the system.
//Only a logged-in user with role == SUPERADMIN may use these routes.
@Controller
@RequiredArgsConstructor
@RequestMapping("/superadmin")
public class SuperAdminController {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    private UserTbl currentSuperAdmin(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        UserTbl user = userRepo.findByUsername(username);
        if (user == null || user.getRole() != Role.SUPERADMIN) return null;
        return user;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model m) {
        UserTbl admin = currentSuperAdmin(session);
        if (admin == null) {
            m.addAttribute("error", "Superadmins only. Login with a superadmin account.");
            return "login";
        }

        m.addAttribute("admin", admin);
        m.addAttribute("allUsers", userRepo.findAll());
        m.addAttribute("sellers", userRepo.findByRole(Role.SELLER));
        m.addAttribute("pendingSellers", userRepo.findBySellerStatus(SellerStatus.PENDING));
        m.addAttribute("allProducts", productRepo.findAll());
        m.addAttribute("allOrders", orderRepo.findAllByOrderByOrderDateDesc());
        return "superadmin";
    }

    @PostMapping("/seller/approve")
    public String approveSeller(@RequestParam("userId") int userId, HttpSession session) {
        if (currentSuperAdmin(session) == null) return "redirect:/login";

        userRepo.findById(userId).ifPresent(user -> {
            user.setSellerStatus(SellerStatus.APPROVED);
            user.setRole(Role.SELLER);
            userRepo.save(user);
        });
        return "redirect:/superadmin";
    }

    @PostMapping("/seller/reject")
    public String rejectSeller(@RequestParam("userId") int userId, HttpSession session) {
        if (currentSuperAdmin(session) == null) return "redirect:/login";

        userRepo.findById(userId).ifPresent(user -> {
            user.setSellerStatus(SellerStatus.REJECTED);
            userRepo.save(user);
        });
        return "redirect:/superadmin";
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam("userId") int userId, HttpSession session) {
        UserTbl admin = currentSuperAdmin(session);
        if (admin == null) return "redirect:/login";
        if (admin.getId() == userId) return "redirect:/superadmin"; //can't delete self

        userRepo.deleteById(userId);
        return "redirect:/superadmin";
    }

    @PostMapping("/product/delete")
    public String deleteProduct(@RequestParam("productId") int productId, HttpSession session) {
        if (currentSuperAdmin(session) == null) return "redirect:/login";

        productRepo.findById(productId).ifPresent(product -> {
            product.setActive(false);
            productRepo.save(product);
        });
        return "redirect:/superadmin";
    }

    @PostMapping("/order/status")
    public String updateOrderStatus(@RequestParam("orderId") int orderId,
                                     @RequestParam("status") OrderStatus status,
                                     HttpSession session) {
        if (currentSuperAdmin(session) == null) return "redirect:/login";

        orderRepo.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepo.save(order);
        });
        return "redirect:/superadmin";
    }

}
