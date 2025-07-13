package it.uniroma3.siw.siwbooks.service;

import it.uniroma3.siw.siwbooks.model.Author;
import it.uniroma3.siw.siwbooks.model.AuthorPhoto;
import it.uniroma3.siw.siwbooks.repository.AuthorPhotoRepository;
import it.uniroma3.siw.siwbooks.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
public class AuthorPhotoService {

    @Autowired
    private AuthorPhotoRepository authorPhotoRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Transactional
    public AuthorPhoto savePhoto(Long authorId, MultipartFile file) throws IOException {
        Optional<Author> opt = authorRepository.findById(authorId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Author non trovato");
        }
        Author author = opt.get();

        // CORREZIONE: Controlla se esiste già una foto per questo autore
        AuthorPhoto photo;
        if (author.getPhoto() != null) {
            // Aggiorna la foto esistente
            photo = author.getPhoto();
            System.out.println("Aggiornamento foto esistente per autore ID: " + authorId);
        } else {
            // Crea una nuova foto
            photo = new AuthorPhoto();
            photo.setAuthor(author);
            System.out.println("Creazione nuova foto per autore ID: " + authorId);
        }

        // Aggiorna i dati della foto
        photo.setFilename(file.getOriginalFilename());
        photo.setContentType(file.getContentType());
        photo.setData(file.getBytes());
        
        AuthorPhoto savedPhoto = authorPhotoRepository.save(photo);
        
        // Assicurati che la relazione sia aggiornata anche sull'autore
        author.setPhoto(savedPhoto);
        authorRepository.save(author);
        
        return savedPhoto;
    }

    @Transactional(readOnly = true)
    public Optional<AuthorPhoto> getPhotoByAuthorId(Long authorId) {
        return authorRepository.findById(authorId)
                .map(Author::getPhoto);
    }

    @Transactional
    public void deletePhoto(Long authorId) {
        Optional<Author> opt = authorRepository.findById(authorId);
        if (opt.isPresent()) {
            Author author = opt.get();
            if (author.getPhoto() != null) {
                AuthorPhoto photo = author.getPhoto();
                author.setPhoto(null);
                authorRepository.save(author);
                authorPhotoRepository.delete(photo);
            }
        }
    }
}