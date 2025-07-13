package it.uniroma3.siw.siwbooks.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException e, 
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("errorMessage", 
            "File troppo grande! Dimensione massima consentita: 10MB");
        
        // Cerca di tornare alla pagina di provenienza
        String referer = request.getHeader("referer");
        if (referer != null && referer.contains("/admin/authors/")) {
            return "redirect:" + referer;
        }
        
        return "redirect:/admin/authors";
    }
}