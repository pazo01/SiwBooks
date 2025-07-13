package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @GetMapping("/authors")
    public String showAuthorList(Model model) {
        model.addAttribute("authors", authorService.findAll());
        return "authors/authorList"; // Cerca /templates/authors/listAuthors.html
    }

    @GetMapping("/authors/{id}")
    public String getAuthor(@PathVariable("id") Long id, Model model) {
        model.addAttribute("author", authorService.findById(id).orElse(null));
        return "authors/authorDetail"; // Cerca /templates/authors/detailAuthor.html
    }
}