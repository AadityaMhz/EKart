package io.virinchi.ekart.Config;

import io.virinchi.ekart.Model.Category;
import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Model.Role;
import io.virinchi.ekart.Model.SellerStatus;
import io.virinchi.ekart.Model.UserTbl;
import io.virinchi.ekart.Repository.CategoryRepository;
import io.virinchi.ekart.Repository.ProductRepository;
import io.virinchi.ekart.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import io.virinchi.ekart.Util.ImageUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//Runs once at startup. Seeds a default SUPERADMIN account, the starter
//categories, and the same 28-product demo catalog that used to live in
//js/products.js - ONLY if the database is empty, so this is safe to leave
//in for every run.
//
//Default superadmin login: username "superadmin" / password "admin123"
//Change this password after your first login in a real deployment.
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    //TiDB Cloud Serverless rejects any single row entry over 6MB - a hard,
    //non-configurable limit. Demo photos are resized/recompressed to a
    //JPEG well under that before being Base64-encoded (see ImageUtil), so
    //this never bites you even with a large original file.

    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;

    @Override
    public void run(String... args) {
        if (!userRepo.existsByUsername("superadmin")) {
            UserTbl superAdmin = new UserTbl();
            superAdmin.setUsername("superadmin");
            superAdmin.setPassword(DigestUtils.md5DigestAsHex("admin123".getBytes()));
            superAdmin.setEmail("superadmin@ekart.com");
            superAdmin.setFullName("Super Admin");
            superAdmin.setRole(Role.SUPERADMIN);
            superAdmin.setSellerStatus(SellerStatus.NONE);
            userRepo.save(superAdmin);
        }

        //A ready-to-use demo shopper, so you can log in and test the buy
        //flow immediately without signing up first.
        if (!userRepo.existsByUsername("buyer1")) {
            UserTbl buyer = new UserTbl();
            buyer.setUsername("buyer1");
            buyer.setPassword(DigestUtils.md5DigestAsHex("buyer123".getBytes()));
            buyer.setEmail("buyer1@ekart.com");
            buyer.setFullName("Demo Buyer");
            buyer.setPhone("9800000000");
            buyer.setAddress("Kathmandu, Nepal");
            buyer.setRole(Role.BUYER);
            buyer.setSellerStatus(SellerStatus.NONE);
            userRepo.save(buyer);
        }

        Map<String, Category> categories = new HashMap<>();
        if (categoryRepo.count() == 0) {
            categories.put("Men", seedCategory("Men", "fa-shirt"));
            categories.put("Women", seedCategory("Women", "fa-vest"));
            categories.put("Kids", seedCategory("Kids", "fa-baby"));
            categories.put("Footwear", seedCategory("Footwear", "fa-shoe-prints"));
            categories.put("Sports", seedCategory("Sports", "fa-dumbbell"));
            categories.put("Electronics", seedCategory("Electronics", "fa-headphones"));
            categories.put("Accessories", seedCategory("Accessories", "fa-glasses"));
            categories.put("Home", seedCategory("Home", "fa-couch"));
        } else {
            categoryRepo.findAll().forEach(c -> categories.put(c.getName(), c));
        }

        if (productRepo.count() == 0) {
            //name, price, oldPrice, category, image-file-in-static/image (or null), icon (or null), badge, rating, reviews, desc
            seed("Professional Hair Trimmer", 2500, 3200, "Electronics", "trimmer.jpg", null, "SALE", 4.3, 128, "Cordless rechargeable trimmer with a stainless-steel blade and 60-minute battery life for precise, long-lasting grooming.", categories);
            seed("Classic Digital Watch", 2500, null, "Accessories", "watch.jpg", null, "NEW", 4.6, 89, "A durable everyday digital watch with a backlit display, stopwatch, and water resistance up to 30m.", categories);
            seed("Air Jordan Sneakers", 5000, 6500, "Sports", "shoes.jpg", null, "SALE", 4.8, 312, "Iconic high-top sneakers with responsive cushioning, built for the court and the street alike.", categories);
            seed("Pro Football Boots", 10000, null, "Sports", "boot.jpg", null, null, 4.5, 76, "Firm-ground football boots with a molded stud pattern for explosive acceleration and sharp turns.", categories);
            seed("Aviator Sunglasses", 4000, 4800, "Accessories", "glass.jpg", null, "SALE", 4.2, 54, "Timeless aviator frames with UV400-protected polarized lenses and a lightweight metal build.", categories);
            seed("Men's Windbreaker Jacket", 2000, null, "Men", "jacket.jpg", null, "NEW", 4.4, 41, "A packable, water-resistant windbreaker built for unpredictable weather without weighing you down.", categories);
            seed("Comfort Slide Sandals", 1200, 1600, "Footwear", "slide sandles.jpg", null, "SALE", 4.1, 63, "Soft cushioned footbed sandals designed for all-day comfort, indoors or out.", categories);
            seed("Summer Breeze Tee", 900, null, "Women", "tee.jpg", null, null, 4.0, 22, "A breathable cotton tee cut for warm-weather layering, available in soft summer tones.", categories);
            seed("Summer Vibes Tee — Coral", 950, null, "Women", "summer 1.jpg", null, "NEW", 4.3, 18, "Lightweight relaxed-fit tee with a sun-faded coral wash, perfect for beach days.", categories);
            seed("Summer Vibes Tee — Ocean", 950, null, "Men", "summer 2.jpg", null, null, 4.2, 15, "Breathable everyday tee in an ocean-blue wash, made from soft combed cotton.", categories);
            seed("New Arrivals Hoodie", 2800, null, "Men", "hoodie.jpg", null, "NEW", 4.5, 33, "Heavyweight fleece hoodie with a relaxed fit - this season's most-requested layer.", categories);
            seed("Street Style Hoodie", 2600, null, "Women", "hoodie 2.jpg", null, "NEW", 4.4, 27, "Oversized streetwear hoodie with a soft brushed interior for everyday comfort.", categories);

            seed("Wireless Earbuds Pro", 3500, 4200, "Electronics", "airpod.jpg", null, "SALE", 4.6, 204, "True wireless earbuds with active noise cancellation and 28-hour total playtime.", categories);
            seed("Fast Charge Power Bank 20K", 1800, null, "Electronics", "power.jpg", null, null, 4.3, 97, "20,000mAh power bank with dual fast-charge ports to keep every device topped up on the go.", categories);
            seed("Kids Cotton T-Shirt Set", 850, null, "Kids", null, "fa-shirt", "NEW", 4.5, 31, "A 3-pack of soft, breathable cotton tees sized for active kids.", categories);
            seed("White Socks", 650, null, "Kids", "scoks.jpg", null, null, 4.7, 58, "High Quality Comfortable Socks.", categories);
            seed("Toddler Ride-On Tricycle", 4200, 5000, "Kids", null, "fa-bicycle", "SALE", 4.4, 46, "A sturdy, adjustable tricycle with a parent push-handle for early rides.", categories);
            seed("Fitness Dumbbell Set 10kg", 3200, null, "Sports", null, "fa-dumbbell", null, 4.6, 71, "Adjustable rubber-coated dumbbell pair built for home strength training.", categories);
            seed("Yoga & Gym Duffel Bag", 1400, null, "Sports", null, "fa-bag-shopping", "NEW", 4.2, 19, "Water-resistant duffel with a dedicated shoe compartment for gym or travel.", categories);
            seed("Wireless Gaming Controller", 4500, 5200, "Electronics", null, "fa-gamepad", "SALE", 4.7, 152, "Low-latency wireless controller with textured grips and remappable buttons.", categories);
            seed("Statement Gemstone Ring", 2200, null, "Accessories", null, "fa-ring", "NEW", 4.3, 24, "Hand-set gemstone ring in a brushed gold-tone band.", categories);
            seed("Classic Reading Glasses", 1100, null, "Accessories", null, "fa-glasses", null, 4.1, 37, "Lightweight anti-glare reading glasses with a spring-hinge frame.", categories);
            seed("Men's Formal Shirt", 1600, null, "Men", null, "fa-shirt", null, 4.4, 29, "Wrinkle-resistant tailored shirt suitable for office or occasion wear.", categories);
            seed("Cozy Winter Mittens", 700, null, "Accessories", null, "fa-mitten", null, 4.0, 12, "Insulated knit mittens with a soft fleece lining for cold-weather days.", categories);
            seed("Leather Laptop Briefcase", 3800, 4500, "Accessories", null, "fa-briefcase", "SALE", 4.5, 66, "Full-grain leather briefcase with a padded 15-inch laptop sleeve.", categories);
            seed("Slim Business Laptop Bag", 3300, null, "Men", null, "fa-laptop", null, 4.3, 40, "Minimalist slim laptop bag with anti-theft back pocket and USB pass-through.", categories);
            seed("Vintage Polaroid Camera", 6200, null, "Electronics", null, "fa-camera", "NEW", 4.6, 21, "Instant-print camera with a retro build and a modern auto-exposure sensor.", categories);
            seed("Throw Pillow Set — Home", 1500, null, "Home", null, "fa-couch", null, 4.2, 17, "A set of two soft woven throw pillows to freshen up any sofa or bed.", categories);
        }
    }

    private Category seedCategory(String name, String icon) {
        Category c = new Category();
        c.setName(name);
        c.setIcon(icon);
        return categoryRepo.save(c);
    }

    private void seed(String name, double price, Integer oldPrice, String categoryName, String imageFile,
                       String icon, String badge, double rating, int reviews, String desc,
                       Map<String, Category> categories) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setOldPrice(oldPrice == null ? null : oldPrice.doubleValue());
        p.setStock(50);
        p.setCategory(categories.get(categoryName));
        p.setBadge(badge);
        p.setRating(rating);
        p.setReviews(reviews);
        p.setActive(true);

        if (imageFile != null) {
            p.setImage(loadImageAsBase64(imageFile));
        } else {
            p.setIcon(icon);
        }

        productRepo.save(p);
    }

    //Reads a demo photo from src/main/resources/static/image/, resizes/
    //recompresses it to stay well under TiDB's 6MB row limit, and
    //Base64-encodes it the same way the seller's "Add Product" upload does.
    private String loadImageAsBase64(String fileName) {
        try (InputStream in = new ClassPathResource("static/image/" + fileName).getInputStream()) {
            String base64 = ImageUtil.toCompressedBase64(in);
            if (base64 == null) {
                System.err.println("Could not read image: " + fileName);
            }
            return base64;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
