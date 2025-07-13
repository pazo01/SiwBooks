package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.AuthorPhoto;
import it.uniroma3.siw.siwbooks.model.BookImage;
import it.uniroma3.siw.siwbooks.service.AuthorPhotoService;
import it.uniroma3.siw.siwbooks.service.BookImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {

    @Autowired
    private AuthorPhotoService authorPhotoService;

    @Autowired
    private BookImageService bookImageService;

    // Endpoint per "servire" la foto di un autore.
    // L'ID qui è l'ID dell'AUTORE, non della foto, per semplicità.
    @GetMapping("/image/author/{authorId}")
    public ResponseEntity<byte[]> getAuthorPhoto(@PathVariable Long authorId) {
        AuthorPhoto photo = authorPhotoService.getPhotoByAuthorId(authorId).orElse(null);
        if (photo != null && photo.getData() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(photo.getContentType()));
            headers.setContentLength(photo.getData().length);
            return ResponseEntity.ok().headers(headers).body(photo.getData());
        }
        // Se non c'è foto, potresti restituire un'immagine placeholder o un 404 Not Found.
        return ResponseEntity.notFound().build();
    }

    // Endpoint per "servire" un'immagine di un libro.
    // L'ID qui è l'ID dell'IMMAGINE, perché un libro può averne molte.
    @GetMapping("/image/book/{imageId}")
    public ResponseEntity<byte[]> getBookImage(@PathVariable Long imageId) {
        BookImage image = bookImageService.getImage(imageId).orElse(null);
        if (image != null && image.getData() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(image.getContentType()));
            headers.setContentLength(image.getData().length);
            return ResponseEntity.ok().headers(headers).body(image.getData());
        }
        return ResponseEntity.notFound().build();
    }
}