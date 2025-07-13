package it.uniroma3.siw.siwbooks.repository;

import it.uniroma3.siw.siwbooks.model.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookImageRepository extends JpaRepository<BookImage, Long> {
    // Di solito basta l’ID per recuperare l’immagine; non servono metodi extra.
}
