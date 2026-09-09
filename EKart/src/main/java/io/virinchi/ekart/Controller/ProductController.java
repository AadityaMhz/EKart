package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.Review;
import io.virinchi.ekart.Model.UserTbl;
import io.virinchi.ekart.Repository.ProductRepository;
import io.virinchi.ekart.Repository.ReviewRepository;
import io.virinchi.ekart.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//Public product-details page - product-details.html - plus the review
//system: any logged-in user can leave one written review + star rating
//per product, and the product's overall rating/review count is
//recalculated from those real rows every time.
@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepo;
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    private UserTbl currentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        return userRepo.findByUsername(username);
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable("id") int id, HttpSession session, Model m) {
        Product product = productRepo.findById(id).orElse(null);

        if (product == null) {
            m.addAttribute("error", "Product not found");
            return "redirect:/home";
        }

        UserTbl user = currentUser(session);
        boolean alreadyReviewed = user != null && reviewRepo.existsByUserAndProduct(user, product);

        m.addAttribute("product", product);
        m.addAttribute("reviews", reviewRepo.findByProductOrderByCreatedAtDesc(product));
        m.addAttribute("alreadyReviewed", alreadyReviewed);
        m.addAttribute("loggedIn", user != null);
        return "product-details";
    }

    @PostMapping("/product/{id}/review")
    public String addReview(@PathVariable("id") int id,
                             @RequestParam("rating") int rating,
                             @RequestParam("comment") String comment,
                             HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/home";
        }

        //one review per user per product
        if (reviewRepo.existsByUserAndProduct(user, product)) {
            return "redirect:/product/" + id;
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(Math.max(1, Math.min(5, rating))); //clamp to 1-5
        review.setComment(comment);
        reviewRepo.save(review);

        //Recalculate the product's overall rating/review count from real reviews.
        List<Review> allReviews = reviewRepo.findByProductOrderByCreatedAtDesc(product);
        double average = allReviews.stream().mapToInt(Review::getRating).average().orElse(0);
        product.setRating(Math.round(average * 10.0) / 10.0); //one decimal place
        product.setReviews(allReviews.size());
        productRepo.save(product);

        return "redirect:/product/" + id;
    }

}
