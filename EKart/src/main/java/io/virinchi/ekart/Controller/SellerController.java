package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.*;
import io.virinchi.ekart.Repository.*;
import io.virinchi.ekart.Util.ImageUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//Seller's own dashboard - this is the Spring/Thymeleaf backing for admin.html:
//their products, the orders/customers that touch those products, and category management.
//Only a logged-in user with role == SELLER may use these routes.
@Controller
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerController {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final OrderItemRepository orderItemRepo;

    private UserTbl currentSeller(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        UserTbl user = userRepo.findByUsername(username);
        if (user == null || user.getRole() != Role.SELLER) return null;
        return user;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model m) {
        UserTbl seller = currentSeller(session);
        if (seller == null) {
            m.addAttribute("error", "Sellers only. Login with a seller account.");
            return "login";
        }

        List<Product> myProducts = productRepo.findBySeller(seller);
        List<io.virinchi.ekart.Model.OrderItem> myOrderItems = orderItemRepo.findByProduct_Seller(seller);
        Set<UserTbl> myCustomers = myOrderItems.stream()
                .map(oi -> oi.getOrders().getUser())
                .collect(Collectors.toSet());

        m.addAttribute("seller", seller);
        m.addAttribute("products", myProducts);
        m.addAttribute("orderItems", myOrderItems);
        m.addAttribute("customers", myCustomers);
        m.addAttribute("categories", categoryRepo.findAll());
        return "seller-dashboard";
    }

    @PostMapping("/product/add")
    public String addProduct(@RequestParam("name") String name,
                              @RequestParam("description") String description,
                              @RequestParam("price") double price,
                              @RequestParam(value = "oldPrice", required = false) Double oldPrice,
                              @RequestParam("stock") int stock,
                              @RequestParam("categoryId") int categoryId,
                              @RequestParam(value = "badge", required = false) String badge,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              HttpSession session) {
        UserTbl seller = currentSeller(session);
        if (seller == null) return "redirect:/login";

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOldPrice(oldPrice);
        product.setStock(stock);
        product.setBadge(badge);
        product.setSeller(seller);
        product.setActive(true);
        categoryRepo.findById(categoryId).ifPresent(product::setCategory);

        if (image != null && !image.isEmpty()) {
            applyImage(product, image);
        }

        productRepo.save(product);
        return "redirect:/seller/dashboard";
    }

    @PostMapping("/product/edit")
    public String editProduct(@RequestParam("id") int id,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam("price") double price,
                               @RequestParam(value = "oldPrice", required = false) Double oldPrice,
                               @RequestParam("stock") int stock,
                               @RequestParam("categoryId") int categoryId,
                               @RequestParam(value = "badge", required = false) String badge,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               HttpSession session) {
        UserTbl seller = currentSeller(session);
        if (seller == null) return "redirect:/login";

        Product product = productRepo.findById(id).orElse(null);
        //only the owning seller may edit their own product
        if (product == null || product.getSeller() == null || product.getSeller().getId() != seller.getId()) {
            return "redirect:/seller/dashboard";
        }

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOldPrice(oldPrice);
        product.setStock(stock);
        product.setBadge(badge);
        categoryRepo.findById(categoryId).ifPresent(product::setCategory);

        if (image != null && !image.isEmpty()) {
            applyImage(product, image);
        }

        productRepo.save(product);
        return "redirect:/seller/dashboard";
    }

    @PostMapping("/product/delete")
    public String deleteProduct(@RequestParam("id") int id, HttpSession session) {
        UserTbl seller = currentSeller(session);
        if (seller == null) return "redirect:/login";

        Product product = productRepo.findById(id).orElse(null);
        if (product != null && product.getSeller() != null && product.getSeller().getId() == seller.getId()) {
            //soft delete, same idea as "active" flag rather than a hard delete
            product.setActive(false);
            productRepo.save(product);
        }
        return "redirect:/seller/dashboard";
    }

    //Resizes/recompresses an uploaded photo before storing it, so a seller
    //picking a large phone photo can't blow past TiDB's 6MB row limit
    //(same technique used for the seeded demo products).
    private void applyImage(Product product, MultipartFile image) {
        try {
            String base64 = ImageUtil.toCompressedBase64(image.getInputStream());
            if (base64 != null) {
                product.setImage(base64);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/category/add")
    public String addCategory(@RequestParam("name") String name,
                               @RequestParam(value = "icon", required = false) String icon,
                               HttpSession session) {
        UserTbl seller = currentSeller(session);
        if (seller == null) return "redirect:/login";

        if (!categoryRepo.existsByName(name)) {
            Category category = new Category();
            category.setName(name);
            category.setIcon(icon);
            categoryRepo.save(category);
        }
        return "redirect:/seller/dashboard";
    }

}
