package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.Book;
import it.uniroma3.siw.siwbooks.model.Review;
import it.uniroma3.siw.siwbooks.model.User;
import it.uniroma3.siw.siwbooks.service.BookService;
import it.uniroma3.siw.siwbooks.service.ReviewService;
import it.uniroma3.siw.siwbooks.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    // GET /api/books/{bookId}/reviews → tutte le recensioni di un libro (permitAll)
    @GetMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<List<Review>> getReviewsByBook(@PathVariable Long bookId) {
        List<Review> reviews = reviewService.findByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    // POST /api/books/{bookId}/reviews → inserisci nuova recensione (solo USER)
    @PostMapping("/api/books/{bookId}/reviews")
    public ResponseEntity<Review> createReview(@PathVariable Long bookId,
                                               @RequestBody Review reviewPayload,
                                               Principal principal) {
        // 1. Trova il libro
        Optional<Book> optBook = bookService.findById(bookId);
        if (optBook.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // 2. Trova l’utente loggato dal Principal (username)
        Optional<User> optUser = userService.findByUsername(principal.getName());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 3. Controlla se già esiste una review di quell’utente per quel libro
        Optional<Review> existing = reviewService.findByBookIdAndReviewerId(bookId, optUser.get().getId());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // 4. Salva la recensione
        Review r = new Review();
        r.setTitle(reviewPayload.getTitle());
        r.setRating(reviewPayload.getRating());
        r.setText(reviewPayload.getText());
        r.setBook(optBook.get());
        r.setReviewer(optUser.get());
        Review saved = reviewService.save(r);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // DELETE /api/reviews/{id} → elimina recensione (solo ADMIN)
    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Optional<Review> optReview = reviewService.findById(id);
        if (optReview.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
