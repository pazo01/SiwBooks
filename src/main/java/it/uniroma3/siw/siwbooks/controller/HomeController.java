package it.uniroma3.siw.siwbooks.controller;

import it.uniroma3.siw.siwbooks.service.AuthorService;
import it.uniroma3.siw.siwbooks.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @GetMapping({"/", "/home"})
    public String showHomePage(Model model) {
        // La versione più efficace: usa il metodo dedicato del service
        model.addAttribute("featuredBooks", this.bookService.findTop4Books());
        model.addAttribute("authors", this.authorService.findAll());
        
        return "home";
    }
}