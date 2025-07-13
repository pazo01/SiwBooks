package it.uniroma3.siw.siwbooks.model; // Assicurati che il package sia quello del tuo progetto

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users") // Questo DEVE corrispondere al nome della tabella nel tuo database PostgreSQL
public class User {

    public static final String DEFAULT_ROLE = "ROLE_USER"; // Ruolo per utenti standard registrati
    public static final String ADMIN_ROLE = "ROLE_ADMIN";  // Ruolo per amministratori

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Ottimo per PostgreSQL
    private Long id;

    @NotBlank(message = "L'username è obbligatorio")
    @Size(min = 3, max = 50, message = "L'username deve avere tra 3 e 50 caratteri")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, message = "La password deve avere almeno 6 caratteri") // Validazione sulla password in chiaro
    @Column(nullable = false)
    private String password; // Verrà memorizzata codificata

    @Column(nullable = false)
    private String role; // Impostato automaticamente durante la registrazione

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String email;

    // Campi opzionali per ora, ma utili per un profilo completo:
    @Size(max = 100)
    private String name; // Nome

    @Size(max = 100)
    private String surname; // Cognome

    // Costruttori
    public User() {
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
}