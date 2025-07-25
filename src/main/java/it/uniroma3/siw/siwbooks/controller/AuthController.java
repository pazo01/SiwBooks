package it.uniroma3.siw.siwbooks.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("loginErrorGlobal", "Username o password non validi.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessageGlobal", "Logout effettuato con successo.");
        }
        return "login";
    }
}
