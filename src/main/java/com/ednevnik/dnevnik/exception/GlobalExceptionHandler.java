package com.ednevnik.dnevnik.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public RedirectView handleDataIntegrityViolation(DataIntegrityViolationException ex, 
                                                    HttpServletRequest request,
                                                    RedirectAttributes redirectAttributes) {
        String errorMessage = "An error occurred while processing your request. Please try again.";
        
        // Check if it's a constraint violation
        if (ex.getCause() instanceof ConstraintViolationException) {
            errorMessage = "Unable to complete the operation due to data constraints.";
        }
        
        redirectAttributes.addFlashAttribute("error", errorMessage);
        
        // Redirect back to the referring page, or to home if referer is not available
        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public RedirectView handleIllegalArgument(IllegalArgumentException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Invalid input provided: " + ex.getMessage());
        
        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleGenericException(Exception ex,
                                             HttpServletRequest request,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again later.");
        
        String referer = request.getHeader("Referer");
        return new RedirectView(referer != null ? referer : "/");
    }
} 