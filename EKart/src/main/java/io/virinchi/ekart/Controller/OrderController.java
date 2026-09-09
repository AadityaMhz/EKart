package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.*;
import io.virinchi.ekart.Repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//Checkout (turns the buyer's cart into an Orders + OrderItem rows) and
//order-confirm.html / order history, shown from profile.html.
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CartItemRepository cartRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final JavaMailSender mailSender;

    //Same codes/percentages as the original js/cart.js COUPON_TABLE, fed by
    //the Spin & Win widget. Kept server-side only - the client sends just
    //the code, never the discount amount, so a coupon can't be forged by
    //editing the page.
    private static final Map<String, Integer> COUPON_TABLE = Map.of(
            "SPIN5", 5,
            "SPIN10", 10,
            "SPIN15", 15,
            "SPIN20", 20
    );

    private UserTbl currentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        return userRepo.findByUsername(username);
    }

    @PostMapping("/checkout")
    @Transactional //needed so the order save + cart cleanup run as one unit; without
                    //it, deleteByUser has no active transaction to run inside of
    public String checkout(@RequestParam("shippingAddress") String shippingAddress,
                            @RequestParam(value = "couponCode", required = false) String couponCode,
                            HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        List<CartItem> items = cartRepo.findByUser(user);
        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        //Check stock BEFORE touching anything - a checkout should never
        //partially succeed and leave stock or the cart in a half-updated state.
        for (CartItem ci : items) {
            if (ci.getQuantity() > ci.getProduct().getStock()) {
                m.addAttribute("error", "Not enough stock for \"" + ci.getProduct().getName()
                        + "\" - only " + ci.getProduct().getStock() + " left. Please update your cart.");
                m.addAttribute("cartItems", items);
                m.addAttribute("cartTotal", items.stream().mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity()).sum());
                return "cart";
            }
        }

        Orders order = new Orders();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PLACED);

        double subtotal = 0;
        for (CartItem ci : items) {
            OrderItem oi = new OrderItem();
            oi.setOrders(order);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(ci.getProduct().getPrice());
            order.getItems().add(oi);
            subtotal += ci.getProduct().getPrice() * ci.getQuantity();

            //Decrement stock now that we know every item in the cart has enough.
            Product product = ci.getProduct();
            product.setStock(product.getStock() - ci.getQuantity());
            productRepo.save(product);
        }

        double discount = 0;
        if (couponCode != null && COUPON_TABLE.containsKey(couponCode.toUpperCase())) {
            int percent = COUPON_TABLE.get(couponCode.toUpperCase());
            discount = subtotal * percent / 100.0;
            order.setCouponCode(couponCode.toUpperCase());
            order.setDiscountAmount(discount);
        }
        order.setTotalAmount(subtotal - discount);

        orderRepo.save(order); //cascades and saves OrderItems too
        cartRepo.deleteByUser(user); //empty the cart after checkout

        //Order confirmation email - same JavaMailSender pattern as the
        //signup welcome email, wrapped in try/catch so a broken mail
        //config never blocks the order itself (it's already saved above).
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Your EKart order #ORD" + order.getId() + " is confirmed");
            mailMessage.setText("Hi " + user.getUsername() + ",\n\n"
                    + "Thanks for your order! Here's a quick summary:\n\n"
                    + "Order ID: ORD" + order.getId() + "\n"
                    + "Total: Rs. " + order.getTotalAmount() + "\n"
                    + "Shipping to: " + order.getShippingAddress() + "\n\n"
                    + "We'll let you know once it ships.\n\n"
                    + "- EKart");
            mailSender.send(mailMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/order/confirm/" + order.getId();
    }

    @GetMapping("/order/confirm/{id}")
    public String orderConfirm(@PathVariable("id") int id, HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        Orders order = orderRepo.findById(id).orElse(null);
        //block viewing an order that isn't yours - a bare numeric ID would
        //otherwise let anyone browse other people's confirmed orders
        if (order == null || order.getUser().getId() != user.getId()) {
            return "redirect:/home";
        }

        m.addAttribute("order", order);
        return "order-confirm";
    }

}
