package it.uniroma3.siw.siwbooks.service;

import it.uniroma3.siw.siwbooks.model.Author;
import it.uniroma3.siw.siwbooks.model.Book;
import it.uniroma3.siw.siwbooks.model.dto.AuthorFormDto;
import it.uniroma3.siw.siwbooks.repository.AuthorRepository;
import it.uniroma3.siw.siwbooks.repository.BookRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private BookRepository bookRepository;

    // =========================================================================
    // METODI ESISTENTI CHE AVEVI GIÀ
    // =========================================================================
    
    public long count() {
        return authorRepository.count();
    }
    
    @Transactional
    public Author save(Author author) {
        return authorRepository.save(author);
    }

    @Transactional(readOnly = true)
    public List<Author> searchAuthors(String query) {
        List<Author> authors = authorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
        // Inizializza anche qui la collezione books per ogni autore
        for (Author author : authors) {
            Hibernate.initialize(author.getBooks());
            Hibernate.initialize(author.getPhoto());
        }
        return authors;
    }

    @Transactional
    public Author createNewAuthor(AuthorFormDto authorDto) {
        Author author = new Author();
        updateAuthorDataFromDto(author, authorDto);
        return this.save(author);
    }

    @Transactional
    public Author updateAuthor(Long id, AuthorFormDto authorDto, List<Long> bookIds) {
        // 1. Trova l'autore esistente dal DB
        Author author = this.findById(id).orElse(null);
        if (author != null) {
            // 2. Aggiorna i suoi dati anagrafici con quelli del form
            this.updateAuthorDataFromDto(author, authorDto);

            // 3. Prepara la nuova lista di libri basata sui checkbox selezionati
            Set<Book> selectedBooks = new HashSet<>();
            if (bookIds != null && !bookIds.isEmpty()) {
                selectedBooks.addAll(bookRepository.findAllById(bookIds));
            }

            // 4. Sincronizza le associazioni:
            //    - Rimuove l'autore dai libri che non sono più selezionati
            for (Book bookInDb : new HashSet<>(author.getBooks())) {
                if (!selectedBooks.contains(bookInDb)) {
                    bookInDb.getAuthors().remove(author);
                }
            }
            //    - Aggiunge l'autore ai libri appena selezionati
            for (Book book : selectedBooks) {
                book.getAuthors().add(author);
            }
            
            author.setBooks(selectedBooks);
            
            // 5. Salva l'autore con le associazioni aggiornate
            return this.authorRepository.save(author);
        }
        return null;
    }

    private void updateAuthorDataFromDto(Author author, AuthorFormDto dto) {
        author.setFirstName(dto.getFirstName() != null ? dto.getFirstName().trim() : null);
        author.setLastName(dto.getLastName() != null ? dto.getLastName().trim() : null);
        author.setNationality(dto.getNationality() != null ? dto.getNationality().trim() : null);
        author.setDateOfBirth(dto.getDateOfBirth());
        author.setDateOfDeath(dto.isDead() ? dto.getDateOfDeath() : null);
    }

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        // CORREZIONE: Usa la query ottimizzata che carica tutto in una volta
        return authorRepository.findAllWithBooks();
    }

    @Transactional(readOnly = true)
    public Optional<Author> findById(Long id) {
        // CORREZIONE: Usa la query ottimizzata che carica tutto in una volta
        Author author = authorRepository.findByIdWithBooks(id);
        return Optional.ofNullable(author);
    }

    @Transactional
    public void deleteById(Long id) {
        Optional<Author> authorOpt = this.findById(id);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            // Rimuovi l'autore da tutti i suoi libri prima di eliminarlo
            for (Book book : new HashSet<>(author.getBooks())) {
                book.getAuthors().remove(author);
            }
            author.getBooks().clear();
            authorRepository.delete(author);
        }
    }

    // =========================================================================
    // NUOVI METODI PER IL CONTROLLO DUPLICATI
    // =========================================================================

    @Transactional(readOnly = true)
    public boolean existsByFirstNameAndLastName(String firstName, String lastName) {
        // Trim degli spazi e controllo null per sicurezza
        String trimmedFirstName = firstName != null ? firstName.trim() : "";
        String trimmedLastName = lastName != null ? lastName.trim() : "";
        return authorRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(trimmedFirstName, trimmedLastName);
    }

    @Transactional(readOnly = true)
    public boolean existsByFirstNameAndLastNameAndIdNot(String firstName, String lastName, Long id) {
        String trimmedFirstName = firstName != null ? firstName.trim() : "";
        String trimmedLastName = lastName != null ? lastName.trim() : "";
        return authorRepository.existsByFirstNameAndLastNameIgnoreCaseAndIdNot(trimmedFirstName, trimmedLastName, id);
    }

    // =========================================================================
    // METODO DI DEBUGGING PER VERIFICARE INCONSISTENZE
    // =========================================================================
    
    @Transactional(readOnly = true)
    public void debugAuthorBooks(Long authorId) {
        Author author = authorRepository.findByIdWithBooks(authorId);
        if (author != null) {
            System.out.println("=== DEBUG AUTHOR: " + author.getFirstName() + " " + author.getLastName() + " ===");
            System.out.println("Author ID: " + author.getId());
            
            // Count dalla query diretta
            Long actualCount = authorRepository.countBooksByAuthorId(authorId);
            System.out.println("Books count from direct query: " + actualCount);
            
            // Count dalla collezione
            System.out.println("Books count from author.getBooks().size(): " + author.getBooks().size());
            
            // Verifica ogni libro associato
            for (Book book : author.getBooks()) {
                System.out.println("- Book: " + book.getTitle() + " (ID: " + book.getId() + ")");
                System.out.println("  Authors in this book: " + book.getAuthors().size());
                for (Author a : book.getAuthors()) {
                    System.out.println("    - " + a.getFirstName() + " " + a.getLastName() + " (ID: " + a.getId() + ")");
                }
            }
            
            if (!actualCount.equals((long) author.getBooks().size())) {
                System.err.println("INCONSISTENZA TROVATA! Count dalla query: " + actualCount + 
                                 ", Count dalla collezione: " + author.getBooks().size());
            }
        }
    }

    /**
     * Metodo per pulire eventuali inconsistenze nel database
     * Sincronizza le associazioni many-to-many tra autori e libri
     */
    @Transactional
    public void cleanupInconsistencies() {
        List<Author> allAuthors = authorRepository.findAll();
        
        for (Author author : allAuthors) {
            // Forza il caricamento dei libri
            Hibernate.initialize(author.getBooks());
            
            // Controlla ogni libro associato all'autore
            Set<Book> authorBooks = new HashSet<>(author.getBooks());
            for (Book book : authorBooks) {
                // Assicurati che la relazione sia bidirezionale
                if (!book.getAuthors().contains(author)) {
                    System.out.println("Fixing missing reverse relationship: " + 
                                     author.getFirstName() + " " + author.getLastName() + 
                                     " -> " + book.getTitle());
                    book.getAuthors().add(author);
                }
            }
            
            authorRepository.save(author);
        }
        
        System.out.println("Cleanup delle inconsistenze completato.");
    }
}