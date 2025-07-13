package it.uniroma3.siw.siwbooks.repository;

import it.uniroma3.siw.siwbooks.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Prende tutte le recensioni di un dato libro
    List<Review> findByBookId(Long bookId);

    // Controlla se un utente ha già recensito un libro (unique constraint)
    Optional<Review> findByBookIdAndReviewerId(Long bookId, Long reviewerId);
}
