package it.uniroma3.siw.siwbooks.service;

import it.uniroma3.siw.siwbooks.model.Book;
import it.uniroma3.siw.siwbooks.model.BookImage;
import it.uniroma3.siw.siwbooks.repository.BookImageRepository;
import it.uniroma3.siw.siwbooks.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
public class BookImageService {

    @Autowired
    private BookImageRepository bookImageRepository;
    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public BookImage saveImage(Long bookId, MultipartFile file) throws IOException {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Libro non trovato"));
        BookImage image = new BookImage();
        image.setFilename(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setData(file.getBytes());
        image.setBook(book);
        return bookImageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public Optional<BookImage> getImage(Long imageId) {
        return bookImageRepository.findById(imageId);
    }

    @Transactional
    public Long deleteImage(Long imageId) {
        return bookImageRepository.findById(imageId).map(image -> {
            Long bookId = image.getBook().getId();
            bookImageRepository.delete(image);
            return bookId;
        }).orElse(null);
    }
}