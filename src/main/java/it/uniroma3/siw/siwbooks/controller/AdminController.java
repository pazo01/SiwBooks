package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.service.AuthorService;
import it.uniroma3.siw.siwbooks.service.BookService;
import it.uniroma3.siw.siwbooks.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private ReviewService reviewService; // Assicurati di avere questo service

    // Questo metodo mostra la pagina principale del pannello di amministrazione.
    @GetMapping("/dashboard")
    public String getAdminDashboard(Model model) {
        // Calcola le statistiche reali
        long totalBooks = bookService.count(); // Oppure bookService.findAll().size()
        long totalAuthors = authorService.count(); // Oppure authorService.findAll().size()
        long totalReviews = reviewService.count(); // Oppure reviewService.findAll().size()
        
        // Aggiungi le statistiche al modello
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalAuthors", totalAuthors);
        model.addAttribute("totalReviews", totalReviews);
        
        return "admin/dashboard"; // Cerca il template in /templates/admin/dashboard.html
    }

    // Aggiungi questo metodo per gestire /admin
    @GetMapping
    public String getAdminHome() {
        return "redirect:/admin/dashboard"; // Reindirizza alla dashboard
    }
}