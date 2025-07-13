package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.Author;
import it.uniroma3.siw.siwbooks.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors; // Aggiungi questo import
import java.util.Map; // Aggiungi questo import
// For @RequestBody validation if needed in the future
// import jakarta.validation.Valid; 


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/authors")
public class AuthorRestController {

    @Autowired
    private AuthorService authorService;

    // GET /api/authors → tutti gli autori
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.findAll();
        return ResponseEntity.ok(authors);
    }

    // GET /api/authors/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        Optional<Author> opt = authorService.findById(id);
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/authors → crea autore (solo ADMIN)
    // SecurityConfig restricts this to ADMIN
    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) { // Add @Valid if Author has validation for creation
        Author saved = authorService.save(author);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // PUT /api/authors/{id} → aggiorna (solo ADMIN)
    // SecurityConfig restricts this to ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @RequestBody Author authorDetails) { // Add @Valid
        Optional<Author> opt = authorService.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Author author = opt.get();
        // Consider using a mapper or updating only non-null fields from authorDetails
        author.setFirstName(authorDetails.getFirstName());
        author.setLastName(authorDetails.getLastName());
        author.setDateOfBirth(authorDetails.getDateOfBirth());
        author.setDateOfDeath(authorDetails.getDateOfDeath());
        author.setNationality(authorDetails.getNationality());
        Author updated = authorService.save(author);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/authors/{id} (solo ADMIN)
    // SecurityConfig restricts this to ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (!authorService.findById(id).isPresent()) { // Check existence before delete
            return ResponseEntity.notFound().build();
        }
        authorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    // NUOVO ENDPOINT PER LA RICERCA - usato da Select2
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, String>>> searchAuthors(@RequestParam(value = "term", required = false, defaultValue = "") String term) {
        List<Author> authors = authorService.searchAuthors(term);
        
        // Select2 si aspetta un formato JSON specifico: una lista di oggetti con 'id' e 'text'
        List<Map<String, String>> response = authors.stream()
                .map(author -> Map.of(
                    "id", author.getId().toString(),
                    "text", author.getFirstName() + " " + author.getLastName()
                ))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(response);
    }
}