package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.Author;
import it.uniroma3.siw.siwbooks.model.Book;
import it.uniroma3.siw.siwbooks.model.Review;
import it.uniroma3.siw.siwbooks.service.AuthorService;
import it.uniroma3.siw.siwbooks.service.BookImageService;
import it.uniroma3.siw.siwbooks.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private BookImageService bookImageService;

    @GetMapping
    public String showBookManagement(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        
        // Calcola valutazione media globale
        double totalRating = 0;
        int totalReviews = 0;
        for (Book book : books) {
            for (Review review : book.getReviews()) {
                totalRating += review.getRating();
                totalReviews++;
            }
        }
        double averageRating = totalReviews > 0 ? totalRating / totalReviews : 0;
        model.addAttribute("averageRating", averageRating);
        
        // Conta autori unici
        Set<Long> uniqueAuthors = new HashSet<>();
        for (Book book : books) {
            for (Author author : book.getAuthors()) {
                uniqueAuthors.add(author.getId());
            }
        }
        model.addAttribute("totalAuthors", uniqueAuthors.size());
        
        return "admin/bookManagement";
    }

    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("allAuthors", authorService.findAll());
        return "admin/bookForm";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book,
                          BindingResult bindingResult,
                          @RequestParam(value = "selectedAuthors", required = false) List<Long> authorIds,
                          Model model, RedirectAttributes redirectAttributes) {

        if (bookService.existsByTitle(book.getTitle())) {
            bindingResult.rejectValue("title", "error.book", "Un libro con questo titolo esiste già.");
        }
        if (authorIds == null || authorIds.isEmpty()) {
            bindingResult.rejectValue("authors", "error.book", "È necessario selezionare almeno un autore.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("allAuthors", authorService.findAll());
            return "admin/bookForm";
        }
        
        Book savedBook = bookService.createNewBook(book, authorIds);
        redirectAttributes.addFlashAttribute("formSuccess", "Libro aggiunto con successo! Ora puoi gestire le immagini.");
        return "redirect:/admin/books/" + savedBook.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id).orElse(null);
        if (book == null) return "redirect:/admin/books";
        
        model.addAttribute("book", book);
        model.addAttribute("allAuthors", authorService.findAll());
        return "admin/bookForm";
    }

    @PostMapping("/{id}/edit")
    public String editBook(@PathVariable Long id,
                           @Valid @ModelAttribute("book") Book book,
                           BindingResult bindingResult,
                           @RequestParam(value = "selectedAuthors", required = false) List<Long> authorIds,
                           Model model, RedirectAttributes redirectAttributes) {
        if (authorIds == null || authorIds.isEmpty()) {
            bindingResult.rejectValue("authors", "error.book", "È necessario selezionare almeno un autore.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("allAuthors", authorService.findAll());
            // Dobbiamo rimettere l'ID nel modello per la renderizzazione corretta del form in caso di errore
            book.setId(id);
            return "admin/bookForm";
        }
        
        bookService.updateBook(id, book, authorIds);
        redirectAttributes.addFlashAttribute("formSuccess", "Libro modificato con successo!");
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteById(id);
        redirectAttributes.addFlashAttribute("formSuccess", "Libro cancellato con successo.");
        return "redirect:/admin/books";
    }
    
    @PostMapping("/{id}/images/add")
    public String addBookImage(@PathVariable Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Per favore, seleziona un file.");
        } else {
            try {
                bookImageService.saveImage(id, file);
                redirectAttributes.addFlashAttribute("success", "Immagine aggiunta!");
            } catch (IOException | IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "Errore upload: " + e.getMessage());
            }
        }
        return "redirect:/admin/books/" + id + "/edit";
    }

    @PostMapping("/images/{imageId}/delete")
    public String deleteBookImage(@PathVariable Long imageId, RedirectAttributes redirectAttributes) {
        Long bookId = bookImageService.deleteImage(imageId);
        if(bookId != null) {
            redirectAttributes.addFlashAttribute("success", "Immagine cancellata.");
            return "redirect:/admin/books/" + bookId + "/edit";
        }
        return "redirect:/admin/books";
    }
}