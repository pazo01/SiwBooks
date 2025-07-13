package it.uniroma3.siw.siwbooks.service;

import it.uniroma3.siw.siwbooks.model.Review;
import it.uniroma3.siw.siwbooks.repository.ReviewRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    // =======================================================
    // ***** METODO findByBookId RIPRISTINATO QUI *****
    // =======================================================
    
    
    public long count() {
        return reviewRepository.count();
    }
    
    @Transactional(readOnly = true)
    public List<Review> findByBookId(Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        // Inizializziamo i dati per evitare errori di lazy loading nell'API
        for(Review review : reviews) {
            Hibernate.initialize(review.getReviewer());
        }
        return reviews;
    }
    
    @Transactional(readOnly = true)
    public List<Review> findAll() {
        List<Review> reviews = (List<Review>) reviewRepository.findAll();
        for (Review review : reviews) {
            Hibernate.initialize(review.getReviewer());
            Hibernate.initialize(review.getBook());
        }
        return reviews;
    }

    @Transactional(readOnly = true)
    public Optional<Review> findByBookIdAndReviewerId(Long bookId, Long reviewerId) {
        return reviewRepository.findByBookIdAndReviewerId(bookId, reviewerId);
    }

    @Transactional
    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }
}