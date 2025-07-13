package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.Book;
import it.uniroma3.siw.siwbooks.model.Review;
import it.uniroma3.siw.siwbooks.model.User;
import it.uniroma3.siw.siwbooks.service.BookService;
import it.uniroma3.siw.siwbooks.service.ReviewService;
import it.uniroma3.siw.siwbooks.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;

    // =========================================================================
    // ***** METODO MANCANTE AGGIUNTO QUI *****
    // Gestisce la richiesta per la lista di tutti i libri all'URL /books
    @GetMapping("/books")
    public String showBookList(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books/bookList"; // Cerca /templates/books/bookList.html
    }
    // =========================================================================

    // Gestisce la richiesta per il dettaglio di un singolo libro
    @GetMapping("/books/{id}")
    public String showBookDetail(@PathVariable Long id, Model model) {
        Optional<Book> bookOpt = bookService.findById(id);
        if (bookOpt.isEmpty()) {
            return "redirect:/books";
        }
        Book book = bookOpt.get();
        model.addAttribute("book", book);
        model.addAttribute("newReview", new Review());
        return "books/bookDetail";
    }

    // Gestisce la creazione di una nuova recensione
    @PostMapping("/books/{id}/reviews")
    public String createReview(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("newReview") Review review,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userService.findByUsername(userDetails.getUsername()).orElse(null);

        if (reviewService.findByBookIdAndReviewerId(id, currentUser.getId()).isPresent()) {
            redirectAttributes.addFlashAttribute("reviewError", "Hai già scritto una recensione per questo libro.");
            return "redirect:/books/" + id;
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reviewError", "Ci sono stati errori nella tua recensione. Riprova.");
            return "redirect:/books/" + id;
        }

        Book book = bookService.findById(id).get();
        review.setBook(book);
        review.setReviewer(currentUser);
        reviewService.save(review);
        
        redirectAttributes.addFlashAttribute("reviewSuccess", "Recensione aggiunta con successo!");
        return "redirect:/books/" + id;
    }
}