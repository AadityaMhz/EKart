package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.CartItem;
import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.UserTbl;
import io.virinchi.ekart.Repository.CartItemRepository;
import io.virinchi.ekart.Repository.ProductRepository;
import io.virinchi.ekart.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//cart.html - add / view / update quantity / remove.
//Same session-attribute login check used across the app ("username" must be set).
@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    private UserTbl currentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        return userRepo.findByUsername(username);
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        List<CartItem> items = cartRepo.findByUser(user);
        double total = items.stream().mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum();

        m.addAttribute("cartItems", items);
        m.addAttribute("cartTotal", total);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") int productId,
                             @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                             HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/home";
        }

        CartItem item = cartRepo.findByUserAndProduct(user, product).orElse(new CartItem());
        int newQuantity = item.getId() == 0 ? quantity : item.getQuantity() + quantity;

        //Cap at available stock rather than rejecting outright - keeps the
        //flow simple, and checkout still double-checks stock before charging.
        if (newQuantity > product.getStock()) {
            newQuantity = product.getStock();
        }
        if (newQuantity <= 0) {
            return "redirect:/product/" + productId;
        }

        if (item.getId() == 0) {
            item.setUser(user);
            item.setProduct(product);
        }
        item.setQuantity(newQuantity);
        cartRepo.save(item);

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam("itemId") int itemId,
                                  @RequestParam("quantity") int quantity) {
        CartItem item = cartRepo.findById(itemId).orElse(null);
        if (item != null) {
            if (quantity <= 0) {
                cartRepo.delete(item);
            } else {
                //never let the cart hold more than what's actually in stock
                item.setQuantity(Math.min(quantity, item.getProduct().getStock()));
                cartRepo.save(item);
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeItem(@RequestParam("itemId") int itemId) {
        cartRepo.deleteById(itemId);
        return "redirect:/cart";
    }

}
