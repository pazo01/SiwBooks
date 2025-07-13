package it.uniroma3.siw.siwbooks.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AuthorFormDto {

    @NotBlank(message = "Il nome non può essere vuoto")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Il cognome non può essere vuoto")
    @Size(max = 50)
    private String lastName;

    @NotNull(message = "La data di nascita è obbligatoria")
    @PastOrPresent(message = "La data di nascita non può essere nel futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    // Questo campo non è obbligatorio, è solo un interruttore per la logica
    private boolean isDead;

    // Questo campo è obbligatorio solo se isDead è true (logica gestita nel controller)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfDeath;

    // ***** MODIFICA QUI: Aggiunta @NotBlank *****
    @NotBlank(message = "La nazionalità è obbligatoria")
    @Size(max = 50)
    private String nationality;

    // Getter e Setter per tutti i campi...
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public boolean isDead() { return isDead; }
    public void setDead(boolean dead) { isDead = dead; }
    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
}