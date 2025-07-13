package it.uniroma3.siw.siwbooks.repository;

import it.uniroma3.siw.siwbooks.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    /**
     * Controlla se esiste un autore con lo stesso nome E cognome.
     * Ignora la differenza tra maiuscole e minuscole.
     */
    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    /**
     * Controlla se esiste un autore con lo stesso nome E cognome, escludendo un ID specifico.
     * Utile per le modifiche per evitare che un autore sia considerato duplicato di se stesso.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Author a WHERE " +
           "LOWER(TRIM(a.firstName)) = LOWER(TRIM(:firstName)) AND " +
           "LOWER(TRIM(a.lastName)) = LOWER(TRIM(:lastName)) AND " +
           "a.id != :id")
    boolean existsByFirstNameAndLastNameIgnoreCaseAndIdNot(@Param("firstName") String firstName, 
                                                          @Param("lastName") String lastName, 
                                                          @Param("id") Long id);

    /**
     * Cerca autori il cui nome O cognome contiene la stringa di ricerca.
     * Perfetto per una barra di ricerca.
     */
    List<Author> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String nameQuery, String nameQuery2);

    /**
     * Query per caricare un autore con tutti i suoi libri in una sola query
     * Questo evita il problema N+1 e assicura che i dati siano consistenti
     */
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books LEFT JOIN FETCH a.photo WHERE a.id = :id")
    Author findByIdWithBooks(@Param("id") Long id);

    /**
     * Query per caricare tutti gli autori con i loro libri in una sola query
     * Utile per la lista degli autori
     */
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books LEFT JOIN FETCH a.photo")
    List<Author> findAllWithBooks();

    /**
     * Query per verificare il numero effettivo di libri associati a un autore
     * direttamente dalla tabella di join
     */
    @Query("SELECT COUNT(b) FROM Author a JOIN a.books b WHERE a.id = :authorId")
    Long countBooksByAuthorId(@Param("authorId") Long authorId);
}