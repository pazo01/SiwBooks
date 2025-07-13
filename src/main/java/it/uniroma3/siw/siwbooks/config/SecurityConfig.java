package it.uniroma3.siw.siwbooks.config;

import it.uniroma3.siw.siwbooks.model.User;
import it.uniroma3.siw.siwbooks.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            it.uniroma3.siw.siwbooks.model.User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con username: " + username));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().replace("ROLE_", ""))
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, 
                    HttpServletResponse response, Authentication authentication) 
                    throws IOException, ServletException {
                
                Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
                
                // Reindirizza gli admin al dashboard admin
                if (roles.contains("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard"); // Cambia da /admin a /admin/dashboard
                } else {
                    // Reindirizza gli utenti normali alla home
                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .failureUrl("/login?error")
                    .successHandler(authenticationSuccessHandler()) // Usa il custom success handler
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            )
            .authorizeHttpRequests(auth -> auth
                    // Risorse statiche
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/image/**").permitAll()
                    // Pagine pubbliche e di autenticazione
                    .requestMatchers("/", "/home", "/register", "/login", "/logout", "/books", "/books/{id}", "/authors", "/authors/{id}").permitAll()
                    .requestMatchers("/check-username").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/{id}", "/api/authors", "/api/authors/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/books/{id}/reviews").permitAll()

                    // =========================================================================
                    // ***** INIZIO PARTE AGGIUNTA PER LE RECENSIONI *****
                    // =========================================================================

                    // Solo gli utenti con ruolo USER possono POSTARE (creare) una nuova recensione
                    .requestMatchers(HttpMethod.POST, "/books/{id}/reviews").hasRole("USER")

                    // Solo gli utenti con ruolo ADMIN possono CANCELLARE una recensione
                    .requestMatchers(HttpMethod.POST, "/admin/reviews/{reviewId}/delete").hasRole("ADMIN")

                    // =========================================================================
                    // ***** FINE PARTE AGGIUNTA PER LE RECENSIONI *****
                    // =========================================================================
                    
                    // Autorizzazioni basate sui ruoli (esistenti)
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/books/{id}/reviews").authenticated() // Lasciato 'authenticated' come nel tuo file

                    // Regole più specifiche per l'admin (esistenti)
                    .requestMatchers(HttpMethod.POST, "/books/add", "/authors/add").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/books/**", "/authors/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/books/**", "/authors/**", "/api/books/**", "/api/authors/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/books/**", "/authors/**", "/api/books/**", "/api/authors/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/reviews/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("ADMIN")

                    // Tutte le altre richieste necessitano di autenticazione
                    .anyRequest().authenticated()
            );
        return http.build();
    }
}