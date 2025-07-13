package it.uniroma3.siw.siwbooks.service;

import it.uniroma3.siw.siwbooks.model.Author;
import it.uniroma3.siw.siwbooks.model.Book;
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
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;

    // =========================================================================
    // ***** UNICA MODIFICA SIGNIFICATIVA QUI: METODO findAll *****
    // =========================================================================
    public long count() {
        return bookRepository.count();
    }

    
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        List<Book> books = bookRepository.findAll();
        for (Book book : books) {
            // Inizializza le collezioni "pigre" per evitare errori nel template
            Hibernate.initialize(book.getAuthors());
            Hibernate.initialize(book.getImages()); // <-- RIGA CHIAVE AGGIUNTA
        }
        return books;
    }

    // Il resto dei metodi rimane invariato
    @Transactional(readOnly = true)
    public boolean existsByTitle(String title) {
        return bookRepository.existsByTitleIgnoreCase(title);
    }
    
    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Hibernate.initialize(bookOpt.get().getAuthors());
            Hibernate.initialize(bookOpt.get().getImages());
            Hibernate.initialize(bookOpt.get().getReviews());
        }
        return bookOpt;
    }

    @Transactional
    public Book createNewBook(Book book, List<Long> authorIds) {
        if (authorIds != null && !authorIds.isEmpty()) {
            List<Author> authors = authorRepository.findAllById(authorIds);
            book.setAuthors(new HashSet<>(authors));
        }
        return this.bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long bookId, Book newBookData, List<Long> authorIds) {
        Book existingBook = this.findById(bookId).orElse(null);
        if (existingBook != null) {
            existingBook.setTitle(newBookData.getTitle());
            existingBook.setYear(newBookData.getYear());
            Set<Author> authors = new HashSet<>();
            if (authorIds != null && !authorIds.isEmpty()) {
                authors.addAll(authorRepository.findAllById(authorIds));
            }
            existingBook.setAuthors(authors);
            return this.bookRepository.save(existingBook);
        }
        return null;
    }
    
    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }
    
    
    @Transactional(readOnly = true)
    public List<Book> findTop4Books() {
        List<Book> allBooks = this.findAll();
        return allBooks.stream()
            .limit(4)
            .collect(Collectors.toList());
    }
}