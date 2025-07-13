package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.User;
import it.uniroma3.siw.siwbooks.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.debug("Showing registration form");
        model.addAttribute("user", new User());
        return "register";
    }
    
    
    @GetMapping("/check-username")
    @ResponseBody
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || username.trim().isEmpty() || username.length() < 3) {
            response.put("available", false);
            response.put("message", "Username deve essere almeno 3 caratteri");
        } else {
            boolean exists = userService.existsByUsername(username);
            response.put("available", !exists);
            response.put("message", exists ? "Username già in uso" : "Username disponibile");
        }
        
        return response;
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        logger.debug("Attempting to register user: {}", user.getUsername());

        // Validazione bean (NotBlank, Email, ecc.)
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors for user: {}", user.getUsername());
            model.addAttribute("registrationErrorGlobal", "Per favore correggi gli errori nel form.");
            return "register";
        }

        // Controllo username unico
        if (userService.existsByUsername(user.getUsername())) {
            logger.warn("Username {} already exists", user.getUsername());
            bindingResult.rejectValue("username", "error.user", "Username già esistente. Scegline un altro.");
            return "register";
        }

        // Controllo email unica
        if (userService.existsByEmail(user.getEmail())) {
            logger.warn("Email {} already exists", user.getEmail());
            bindingResult.rejectValue("email", "error.user", "Email già registrata. Usane un'altra.");
            return "register";
        }

        try {
            userService.registerUser(user);
            logger.info("User {} registered successfully", user.getUsername());
            redirectAttributes.addFlashAttribute(
                "registrationSuccessGlobal",
                "Registrazione avvenuta con successo! Ora puoi effettuare il login."
            );
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Unexpected error during registration for user: {}", user.getUsername(), e);
            model.addAttribute("registrationErrorGlobal", "Errore imprevisto durante la registrazione. Riprova.");
            return "register";
        }
    }
}
