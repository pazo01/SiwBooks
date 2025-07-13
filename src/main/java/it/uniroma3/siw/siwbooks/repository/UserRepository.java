package it.uniroma3.siw.siwbooks.repository;

import it.uniroma3.siw.siwbooks.model.User;
import org.springframework.data.jpa.repository.JpaRepository; // Usa JpaRepository
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Metodo per trovare un utente per username (necessario per Spring Security)
    Optional<User> findByUsername(String username);

    // Metodo per verificare se esiste già un utente con quello username (utile per la registrazione)
    boolean existsByUsername(String username);

    // Metodo per trovare un utente per email (utile per la registrazione)
    Optional<User> findByEmail(String email);

    // Metodo per verificare se esiste già un utente con quella email
    boolean existsByEmail(String email);
}