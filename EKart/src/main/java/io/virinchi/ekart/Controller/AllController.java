package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.Product;
import io.virinchi.ekart.Repository.CategoryRepository;
import io.virinchi.ekart.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//Public pages: landing, about, contact, and the main product-browsing home page.
//Mirrors AllController in SpringWebVir (root "/" route + a page that lists rows
//from the database via th:each), extended with search/category filtering.
@Controller
@RequiredArgsConstructor
public class AllController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final JavaMailSender jms;

    //Where "Send a Message" on contact.html actually goes - same JavaMailSender
    //pattern used for the signup confirmation email.
    private static final String SUPPORT_INBOX = "support@ekart.com";

    @GetMapping("/")
    public String landing(Model m) {
        //top-rated active products, shown as "Featured Products" on the landing page
        List<Product> featured = productRepo.findByActiveTrue().stream()
                .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                .limit(8)
                .collect(Collectors.toList());
        m.addAttribute("featuredProducts", featured);
        return "landing";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @PostMapping("/contact")
    public String contactPost(@RequestParam("name") String name,
                               @RequestParam("email") String email,
                               @RequestParam("subject") String subject,
                               @RequestParam("message") String message,
                               Model m) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(SUPPORT_INBOX);
        mail.setSubject("[EKart Contact] " + subject + " (from " + name + ")");
        mail.setText(message + "\n\nReply to: " + email);
        jms.send(mail);

        m.addAttribute("message", "Thank you for your message! We'll get back to you soon.");
        return "contact";
    }

    @GetMapping("/home")
    public String homeGet(@RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "category", required = false) String category,
                           Model m) {

        if (search != null && !search.isBlank()) {
            m.addAttribute("products", productRepo.findByNameContainingIgnoreCaseAndActiveTrue(search));
        } else if (category != null && !category.isBlank()) {
            m.addAttribute("products", productRepo.findByCategory_NameAndActiveTrue(category));
        } else {
            m.addAttribute("products", productRepo.findByActiveTrue());
        }

        m.addAttribute("categories", categoryRepo.findAll());
        m.addAttribute("search", search);
        m.addAttribute("selectedCategory", category);
        return "home";
    }

}
