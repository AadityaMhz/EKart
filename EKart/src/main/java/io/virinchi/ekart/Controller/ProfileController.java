package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.SellerStatus;
import io.virinchi.ekart.Model.UserTbl;
import io.virinchi.ekart.Repository.OrderRepository;
import io.virinchi.ekart.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//profile.html - view/edit account details + order history, and becomesellerrr.html
//to apply for a seller account.
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepo;
    private final OrderRepository orderRepo;

    private UserTbl currentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;
        return userRepo.findByUsername(username);
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }
        m.addAttribute("user", user);
        m.addAttribute("orders", orderRepo.findByUserOrderByOrderDateDesc(user));
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpServletRequest request, HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        user.setFullName(request.getParameter("fullName"));
        user.setPhone(request.getParameter("phone"));
        user.setAddress(request.getParameter("address"));
        userRepo.save(user);

        m.addAttribute("message", "Profile updated!");
        m.addAttribute("user", user);
        m.addAttribute("orders", orderRepo.findByUserOrderByOrderDateDesc(user));
        return "profile";
    }

    @GetMapping("/become-seller")
    public String becomeSellerForm(HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }
        m.addAttribute("user", user);
        return "become-seller";
    }

    @PostMapping("/become-seller")
    public String becomeSellerSubmit(HttpServletRequest request, HttpSession session, Model m) {
        UserTbl user = currentUser(session);
        if (user == null) {
            m.addAttribute("error", "Login First!!!");
            return "login";
        }

        user.setBusinessName(request.getParameter("businessName"));
        user.setSellerStatus(SellerStatus.PENDING);
        userRepo.save(user);

        m.addAttribute("message", "Your seller application has been submitted for review.");
        m.addAttribute("user", user);
        return "become-seller";
    }

}
