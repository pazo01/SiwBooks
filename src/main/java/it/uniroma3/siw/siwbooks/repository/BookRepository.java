package it.uniroma3.siw.siwbooks.repository;

import it.uniroma3.siw.siwbooks.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    // ***** METODO AGGIUNTO *****
    // Controlla se esiste un libro con lo stesso titolo, ignorando maiuscole/minuscole.
    // Spring Data JPA capisce cosa fare solo dal nome del metodo.
    boolean existsByTitleIgnoreCase(String title);
}