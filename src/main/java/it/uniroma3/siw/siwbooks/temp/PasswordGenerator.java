package it.uniroma3.siw.siwbooks.temp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "adminP@sswOrd!"; // LA TUA PASSWORD ADMIN SCELTA
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Password ADMIN codificata (da copiare in import.sql):");
        System.out.println(encodedPassword);
    }
}