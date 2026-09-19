package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.CartItem;
import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.UserTbl;
import io.virinchi.ekart.Repository.CartItemRepository;
import io.virinchi.ekart.Repository.ProductRepository;
import io.virinchi.ekart.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

// cart.html - add / view / update quantity / remove.
// Colour is stored ONLY for footwear products. Everything else gets null.
@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;


    // =========================================================
    // CURRENT LOGGED-IN USER
    // =========================================================

    private UserTbl currentUser(HttpSession session) {

        String username =
                (String) session.getAttribute("username");

        if (username == null) {
            return null;
        }

        return userRepo.findByUsername(username);
    }


    // =========================================================
    // IS THIS PRODUCT FOOTWEAR?
    // =========================================================
    //
    // Single place that decides whether a product has colours.
    // If your shoe category is named something else, change it
    // here only.
    // =========================================================

    private boolean isFootwear(Product product) {

        return product != null
                && product.getCategory() != null
                && product.getCategory().getName() != null
                && "Footwear".equalsIgnoreCase(
                product.getCategory().getName().trim());
    }


    // =========================================================
    // VIEW CART
    // =========================================================

    @GetMapping("/cart")
    public String viewCart(
            HttpSession session,
            Model m) {

        UserTbl user = currentUser(session);

        if (user == null) {
            m.addAttribute(
                    "error",
                    "Login First!!!"
            );

            return "login";
        }

        List<CartItem> items =
                cartRepo.findByUser(user);

        double total =
                items.stream()
                        .mapToDouble(i ->
                                i.getProduct().getPrice()
                                        * i.getQuantity())
                        .sum();

        m.addAttribute(
                "cartItems",
                items
        );

        m.addAttribute(
                "cartTotal",
                total
        );

        return "cart";
    }


    // =========================================================
    // PRODUCT IMAGE
    // =========================================================
    //
    // Product.image is stored as a Base64 String.
    //
    // We decode it here and send the actual JPEG bytes
    // to the browser instead of making Thymeleaf create
    // a huge Base64 string.
    //
    // This fixes:
    // EL1078E: Concatenated string is too long
    // =========================================================

    @GetMapping("/product-image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProductImage(
            @PathVariable int id) {

        Product product =
                productRepo.findById(id).orElse(null);

        if (product == null
                || product.getImage() == null
                || product.getImage().isEmpty()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        try {

            byte[] imageBytes =
                    Base64.getDecoder()
                            .decode(product.getImage());

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }


    // =========================================================
    // ADD TO CART
    // =========================================================

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam("productId") int productId,

            @RequestParam(
                    value = "quantity",
                    defaultValue = "1"
            )
            int quantity,

            // No default value here.
            // A non-footwear product must never become "Black".
            @RequestParam(
                    value = "color",
                    required = false
            )
            String color,

            HttpSession session,
            Model m) {

        UserTbl user =
                currentUser(session);

        if (user == null) {

            m.addAttribute(
                    "error",
                    "Login First!!!"
            );

            return "login";
        }


        Product product =
                productRepo.findById(productId)
                        .orElse(null);

        if (product == null) {
            return "redirect:/home";
        }


        // -----------------------------------------------------
        // Colour applies to footwear only
        // -----------------------------------------------------

        boolean footwear = isFootwear(product);

        if (footwear) {

            color = (color == null || color.trim().isEmpty())
                    ? "Black"
                    : color.trim();

        } else {

            // Ignore anything the form sent.
            color = null;
        }


        // -----------------------------------------------------
        // Find the existing cart item
        // -----------------------------------------------------
        //
        // findByUserAndProductAndColor with a null colour would
        // generate "color = null" in SQL, which never matches.
        // So non-footwear uses the two-argument lookup instead.
        // -----------------------------------------------------

        CartItem item =
                footwear
                        ? cartRepo
                        .findByUserAndProductAndColor(
                                user,
                                product,
                                color
                        )
                        .orElse(null)
                        : cartRepo
                        .findByUserAndProduct(
                                user,
                                product
                        )
                        .orElse(null);


        // -----------------------------------------------------
        // New product / colour combination
        // -----------------------------------------------------

        if (item == null) {

            item = new CartItem();

            item.setUser(user);

            item.setProduct(product);

            item.setColor(color);

            item.setQuantity(
                    Math.min(
                            quantity,
                            product.getStock()
                    )
            );

        }


        // -----------------------------------------------------
        // Already in cart
        // -----------------------------------------------------

        else {

            int newQuantity =
                    item.getQuantity()
                            + quantity;

            item.setQuantity(
                    Math.min(
                            newQuantity,
                            product.getStock()
                    )
            );

            item.setColor(color);
        }


        // -----------------------------------------------------
        // Prevent invalid quantity
        // -----------------------------------------------------

        if (item.getQuantity() <= 0) {

            return "redirect:/product/"
                    + productId;
        }


        cartRepo.save(item);

        return "redirect:/cart";
    }


    // =========================================================
    // UPDATE QUANTITY
    // =========================================================

    @PostMapping("/cart/update")
    public String updateQuantity(
            @RequestParam("itemId") int itemId,

            @RequestParam("quantity") int quantity) {

        CartItem item =
                cartRepo.findById(itemId)
                        .orElse(null);

        if (item != null) {

            // Quantity 0 or below = remove
            if (quantity <= 0) {

                cartRepo.delete(item);

            } else {

                // Never exceed available stock
                int maxQuantity =
                        item.getProduct().getStock();

                item.setQuantity(
                        Math.min(
                                quantity,
                                maxQuantity
                        )
                );

                cartRepo.save(item);
            }
        }

        return "redirect:/cart";
    }


    // =========================================================
    // REMOVE ITEM
    // =========================================================

    @PostMapping("/cart/remove")
    public String removeItem(
            @RequestParam("itemId") int itemId) {

        cartRepo.deleteById(itemId);

        return "redirect:/cart";
    }
}