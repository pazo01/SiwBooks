package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.Review;
import it.uniroma3.siw.siwbooks.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // ***** METODO AGGIORNATO CON STATISTICHE REALI *****
    // Risponde a GET /admin/reviews
    @GetMapping("/admin/reviews")
    public String showReviewManagement(Model model) {
        List<Review> reviews = reviewService.findAll();
        model.addAttribute("reviews", reviews);
        
        // Calcola statistiche reali
        long totalReviews = reviews.size();
        model.addAttribute("totalReviews", totalReviews);
        
        // Calcola media voti
        double averageRating = reviews.stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
        model.addAttribute("averageRating", averageRating);
        
        return "admin/reviewManagement";
    }

    // Gestisce la richiesta POST per cancellare una recensione
    @PostMapping("/admin/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable("reviewId") Long reviewId, RedirectAttributes redirectAttributes) {

        Review review = reviewService.findById(reviewId).orElse(null);
        if (review == null) {
            redirectAttributes.addFlashAttribute("deleteError", "Recensione non trovata.");
            return "redirect:/admin/reviews";
        }

        Long bookId = review.getBook().getId();
        reviewService.deleteById(reviewId);

        redirectAttributes.addFlashAttribute("deleteSuccess", "Recensione cancellata con successo.");

        return "redirect:/admin/reviews";
    }
}