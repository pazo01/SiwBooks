package it.uniroma3.siw.siwbooks.repository;

import it.uniroma3.siw.siwbooks.model.AuthorPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorPhotoRepository extends JpaRepository<AuthorPhoto, Long> {
    // In genere non servono metodi extra: usi l’ID della foto per recuperarla.
}
