package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.model.Author;
import it.uniroma3.siw.siwbooks.model.dto.AuthorFormDto;
import it.uniroma3.siw.siwbooks.service.AuthorPhotoService;
import it.uniroma3.siw.siwbooks.service.AuthorService;
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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/authors")
public class AdminAuthorController {

    @Autowired private AuthorService authorService;
    @Autowired private AuthorPhotoService authorPhotoService;
    @Autowired private BookService bookService;

    @GetMapping
    public String showAuthorManagement(Model model) {
        List<Author> authors = authorService.findAll();
        model.addAttribute("authors", authors);
        
        // Calcola autori viventi (hanno data di nascita ma NON data di morte)
        long aliveAuthorsCount = authors.stream()
            .filter(author -> author.getDateOfBirth() != null && author.getDateOfDeath() == null)
            .count();
        model.addAttribute("aliveAuthorsCount", aliveAuthorsCount);
        
        // Calcola nazionalità uniche (esclude null e stringhe vuote)
        long uniqueNationalitiesCount = authors.stream()
            .filter(author -> author.getNationality() != null && !author.getNationality().trim().isEmpty())
            .map(Author::getNationality)
            .distinct()
            .count();
        model.addAttribute("uniqueNationalitiesCount", uniqueNationalitiesCount);
        
        // Calcola età media degli autori viventi
        double averageAge = authors.stream()
            .filter(author -> author.getDateOfBirth() != null && author.getDateOfDeath() == null)
            .mapToInt(author -> java.time.LocalDate.now().getYear() - author.getDateOfBirth().getYear())
            .average()
            .orElse(0.0);
        model.addAttribute("averageAge", (int) Math.round(averageAge));
        
        return "admin/authorManagement";
    }

    @GetMapping("/add")
    public String showAddAuthorForm(Model model) {
        model.addAttribute("authorDto", new AuthorFormDto());
        return "admin/authorForm";
    }

    @PostMapping("/add")
    public String addAuthor(@Valid @ModelAttribute("authorDto") AuthorFormDto authorDto, 
                           BindingResult bindingResult, 
                           RedirectAttributes redirectAttributes) {
        
        // CONTROLLO DUPLICATI - Prima della validazione standard
        if (authorService.existsByFirstNameAndLastName(authorDto.getFirstName(), authorDto.getLastName())) {
            bindingResult.rejectValue("firstName", "error", 
                "Un autore con il nome '" + authorDto.getFirstName() + "' e cognome '" + authorDto.getLastName() + "' è già registrato nel sistema.");
        }
        
        // Validazione custom per la data di morte
        if (authorDto.isDead() && authorDto.getDateOfDeath() == null) {
            bindingResult.rejectValue("dateOfDeath", "error.authorDto", 
                "Se l'autore è deceduto, la data di morte è obbligatoria.");
        }
        
        // Validazione aggiuntiva: data di morte deve essere dopo data di nascita
        if (authorDto.isDead() && authorDto.getDateOfDeath() != null && 
            authorDto.getDateOfBirth() != null && 
            authorDto.getDateOfDeath().isBefore(authorDto.getDateOfBirth())) {
            bindingResult.rejectValue("dateOfDeath", "error.authorDto", 
                "La data di morte deve essere successiva alla data di nascita.");
        }
        
        if (bindingResult.hasErrors()) {
            return "admin/authorForm";
        }
        
        try {
            Author savedAuthor = authorService.createNewAuthor(authorDto);
            redirectAttributes.addFlashAttribute("formSuccess", 
                "Autore '" + savedAuthor.getFirstName() + " " + savedAuthor.getLastName() + 
                "' aggiunto con successo! Ora puoi gestire i suoi libri e la sua foto.");
            return "redirect:/admin/authors/" + savedAuthor.getId() + "/edit";
        } catch (Exception e) {
            bindingResult.reject("globalError", "Errore durante il salvataggio: " + e.getMessage());
            return "admin/authorForm";
        }
    }
    
    @GetMapping("/{id}/edit")
    public String showEditAuthorForm(@PathVariable Long id, Model model) {
        Author author = authorService.findById(id).orElse(null);
        if (author == null) {
            return "redirect:/admin/authors";
        }
        
        AuthorFormDto authorDto = new AuthorFormDto();
        authorDto.setFirstName(author.getFirstName());
        authorDto.setLastName(author.getLastName());
        authorDto.setNationality(author.getNationality());
        authorDto.setDateOfBirth(author.getDateOfBirth());
        if(author.getDateOfDeath() != null) {
            authorDto.setDead(true);
            authorDto.setDateOfDeath(author.getDateOfDeath());
        }

        model.addAttribute("authorId", id);
        model.addAttribute("authorPhoto", author.getPhoto());
        model.addAttribute("authorDto", authorDto);
        model.addAttribute("author", author);
        model.addAttribute("allBooks", bookService.findAll());
        
        return "admin/authorForm";
    }

    @PostMapping("/{id}/edit")
    public String editAuthor(@PathVariable Long id, 
                             @Valid @ModelAttribute("authorDto") AuthorFormDto authorDto, 
                             BindingResult bindingResult, 
                             @RequestParam(value = "selectedBooks", required = false) List<Long> bookIds,
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        
        // CONTROLLO DUPLICATI PER LA MODIFICA (escludendo l'autore corrente)
        if (authorService.existsByFirstNameAndLastNameAndIdNot(
                authorDto.getFirstName(), authorDto.getLastName(), id)) {
            bindingResult.rejectValue("firstName", "error", 
                "Un altro autore con il nome '" + authorDto.getFirstName() + 
                "' e cognome '" + authorDto.getLastName() + "' è già registrato nel sistema.");
        }
        
        // Validazione custom per la data di morte
        if (authorDto.isDead() && authorDto.getDateOfDeath() == null) {
            bindingResult.rejectValue("dateOfDeath", "error.authorDto", 
                "Se l'autore è deceduto, la data di morte è obbligatoria.");
        }
        
        // Validazione aggiuntiva: data di morte deve essere dopo data di nascita
        if (authorDto.isDead() && authorDto.getDateOfDeath() != null && 
            authorDto.getDateOfBirth() != null && 
            authorDto.getDateOfDeath().isBefore(authorDto.getDateOfBirth())) {
            bindingResult.rejectValue("dateOfDeath", "error.authorDto", 
                "La data di morte deve essere successiva alla data di nascita.");
        }
        
        if (bindingResult.hasErrors()) {
            // SE CI SONO ERRORI, DOBBIAMO RIPOPOLARE IL MODELLO CON TUTTI I DATI
            Author author = authorService.findById(id).orElse(null);
            model.addAttribute("authorId", id);
            model.addAttribute("author", author);
            model.addAttribute("authorPhoto", author != null ? author.getPhoto() : null);
            model.addAttribute("allBooks", bookService.findAll());
            return "admin/authorForm";
        }
        
        try {
            authorService.updateAuthor(id, authorDto, bookIds);
            redirectAttributes.addFlashAttribute("formSuccess", 
                "Autore '" + authorDto.getFirstName() + " " + authorDto.getLastName() + "' modificato con successo!");
            return "redirect:/admin/authors";
        } catch (Exception e) {
            // In caso di errore, ripopola il modello e mostra l'errore
            Author author = authorService.findById(id).orElse(null);
            model.addAttribute("authorId", id);
            model.addAttribute("author", author);
            model.addAttribute("authorPhoto", author != null ? author.getPhoto() : null);
            model.addAttribute("allBooks", bookService.findAll());
            bindingResult.reject("globalError", "Errore durante la modifica: " + e.getMessage());
            return "admin/authorForm";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Author author = authorService.findById(id).orElse(null);
            if (author != null) {
                String authorName = author.getFirstName() + " " + author.getLastName();
                authorService.deleteById(id);
                redirectAttributes.addFlashAttribute("formSuccess", 
                    "Autore '" + authorName + "' cancellato con successo.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Autore non trovato.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Errore durante l'eliminazione: " + e.getMessage());
        }
        return "redirect:/admin/authors";
    }

 // SOSTITUISCI il metodo uploadAuthorPhoto esistente nel tuo AdminAuthorController con questo:

    @PostMapping("/{id}/photo")
    public String uploadAuthorPhoto(@PathVariable Long id, 
                                   @RequestParam("file") MultipartFile file, 
                                   RedirectAttributes redirectAttributes) {
        try {
            // Validazione file vuoto
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nessun file selezionato.");
                return "redirect:/admin/authors/" + id + "/edit";
            }

            // Validazione tipo file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", 
                    "Formato file non supportato. Carica solo immagini (JPG, PNG, GIF).");
                return "redirect:/admin/authors/" + id + "/edit";
            }

            // Validazione dimensione (5MB max)
            long maxSize = 5 * 1024 * 1024; // 5MB in bytes
            if (file.getSize() > maxSize) {
                redirectAttributes.addFlashAttribute("error", 
                    "File troppo grande. Dimensione massima: 5MB.");
                return "redirect:/admin/authors/" + id + "/edit";
            }

            // Salva la foto (ora gestisce automaticamente aggiornamento vs creazione)
            authorPhotoService.savePhoto(id, file);
            redirectAttributes.addFlashAttribute("success", "Foto caricata con successo!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Autore non trovato.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Errore durante il caricamento del file: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Errore imprevisto: " + e.getMessage());
        }

        return "redirect:/admin/authors/" + id + "/edit";
    }

    // AGGIUNGI questo nuovo metodo per rimuovere la foto
    @PostMapping("/{id}/photo/delete")
    public String deleteAuthorPhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            authorPhotoService.deletePhoto(id);
            redirectAttributes.addFlashAttribute("success", "Foto rimossa con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Errore durante la rimozione della foto: " + e.getMessage());
        }
        
        return "redirect:/admin/authors/" + id + "/edit";
    }

    // =========================================================================
    // NUOVI ENDPOINT AJAX PER IL CONTROLLO DUPLICATI
    // =========================================================================

    /**
     * Endpoint AJAX per verificare duplicati in tempo reale
     */
    @GetMapping("/check-duplicate")
    @ResponseBody
    public Map<String, Object> checkDuplicate(@RequestParam("firstName") String firstName,
                                             @RequestParam("lastName") String lastName,
                                             @RequestParam(value = "excludeId", required = false) Long excludeId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isDuplicate;
            if (excludeId != null) {
                isDuplicate = authorService.existsByFirstNameAndLastNameAndIdNot(firstName, lastName, excludeId);
            } else {
                isDuplicate = authorService.existsByFirstNameAndLastName(firstName, lastName);
            }
            
            response.put("isDuplicate", isDuplicate);
            response.put("message", isDuplicate ? 
                "Un autore con il nome '" + firstName + "' e cognome '" + lastName + "' è già registrato nel sistema." : 
                "Nessun duplicato trovato.");
            response.put("success", true);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Errore durante il controllo: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Endpoint AJAX per la ricerca di autori
     */
    @GetMapping("/search")
    @ResponseBody
    public List<Author> searchAuthors(@RequestParam("q") String query) {
        return authorService.searchAuthors(query);
    }
}