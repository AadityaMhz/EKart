package io.virinchi.ekart.Controller;

import io.virinchi.ekart.Model.Role;
import io.virinchi.ekart.Model.SellerStatus;
import io.virinchi.ekart.Model.UserTbl;
import io.virinchi.ekart.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

//Signup / Login / Logout - same MD5 + HttpSession pattern taught in SpringWebVir's
//SignupLoginController, extended with roles so login redirects to the right dashboard.
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository uRepo;
    private final JavaMailSender mailSender;

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/signup")
    public String signupPost(HttpServletRequest request, Model m) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");

        if (uRepo.existsByUsername(username)) {
            m.addAttribute("error", "That username is already taken.");
            return "signup";
        }
        if (uRepo.existsByEmail(email)) {
            m.addAttribute("error", "An account with that email already exists.");
            return "signup";
        }

        String hashPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        UserTbl user = new UserTbl();
        user.setUsername(username);
        user.setPassword(hashPassword);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole(Role.BUYER);
        user.setSellerStatus(SellerStatus.NONE);

        uRepo.save(user);

        //Welcome email to the new user's own inbox, same pattern your
        //teacher's slides show in the Signup Controller. Wrapped in try/catch
        //so a broken mail config never blocks signup itself - the account is
        //already saved above by this point either way.
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject("Signup Successfully");
            mailMessage.setText("Welcome " + username + "!");
            mailSender.send(mailMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        m.addAttribute("message", "Signup successful! Please log in.");
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(HttpServletRequest request, Model m) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String hashPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        if (uRepo.existsByUsernameAndPassword(username, hashPassword)) {
            UserTbl user = uRepo.findByUsername(username);
            HttpSession session = request.getSession();
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole().name());

            //Send each role to their own landing page, same idea as home.html redirect
            //in the SpringWebVir template.
            switch (user.getRole()) {
                case SUPERADMIN:
                    return "redirect:/superadmin";
                case SELLER:
                    return "redirect:/seller/dashboard";
                default:
                    return "redirect:/home";
            }
        }

        m.addAttribute("error", "Username or password is incorrect");
        return "login";
    }

    @GetMapping("/logout")
    public String logoutGet(HttpSession session, Model m) {
        session.invalidate();
        m.addAttribute("message", "You have logged out!");
        return "login";
    }

}
