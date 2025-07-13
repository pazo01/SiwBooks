-- Inserisce l'utente admin nella tabella 'users'
-- SOSTITUISCI '$2a$10$YourActualAdminPasswordHashHere' CON L'HASH BCRYPT REALE DELLA TUA PASSWORD ADMIN
INSERT INTO users (id, username, password, role, name, surname, email) VALUES (1, 'admin', '$2a$10$XSxBbq9kd2XZYtDFpPKDaexY6Swa/0tLj.L5.T7AaIGngZM4LqiTC', 'ROLE_ADMIN', 'Admin', 'Principal', 'admin@siwbooks.com');

-- Inserisce gli autori, UN COMANDO PER OGNI AUTORE
INSERT INTO authors (id, first_name, last_name, date_of_birth, nationality) VALUES (1, 'J.K.', 'Rowling', '1965-07-31', 'British');
INSERT INTO authors (id, first_name, last_name, date_of_birth, nationality) VALUES (2, 'George R.R.', 'Martin', '1948-09-20', 'American');
INSERT INTO authors (id, first_name, last_name, date_of_birth, date_of_death, nationality) VALUES (3, 'J.R.R.', 'Tolkien', '1892-01-03', '1973-09-02', 'British');

-- Inserisce i libri, UN COMANDO PER OGNI LIBRO
INSERT INTO books (id, title, year_published) VALUES (1, 'Harry Potter e la pietra filosofale', 1997);
INSERT INTO books (id, title, year_published) VALUES (2, 'A Game of Thrones', 1996);
INSERT INTO books (id, title, year_published) VALUES (3, 'Lo Hobbit', 1937);
INSERT INTO books (id, title, year_published) VALUES (4, 'Harry Potter e la camera dei segreti', 1998);

-- Crea le relazioni nella tabella di join 'book_author', UN COMANDO PER OGNI RELAZIONE
-- Ora questi comandi funzioneranno perché gli autori e i libri saranno stati inseriti correttamente.
INSERT INTO book_author (book_id, author_id) VALUES (1, 1);
INSERT INTO book_author (book_id, author_id) VALUES (2, 2);
INSERT INTO book_author (book_id, author_id) VALUES (3, 3);
INSERT INTO book_author (book_id, author_id) VALUES (4, 1);



-- Step A: Aggiungiamo i due nuovi autori per il nostro libro
INSERT INTO authors (id, first_name, last_name, nationality) VALUES (4, 'Terry', 'Pratchett', 'British');
INSERT INTO authors (id, first_name, last_name, nationality) VALUES (5, 'Neil', 'Gaiman', 'British');

-- Step B: Aggiungiamo il nuovo libro
INSERT INTO books (id, title, year_published) VALUES (5, 'Good Omens', 1990);

-- Step C: Associamo il libro (ID 5) a ENTRAMBI gli autori (ID 4 e 5)
-- Inseriamo due righe nella tabella di join con lo stesso book_id
INSERT INTO book_author (book_id, author_id) VALUES (5, 4); -- Associa Good Omens a Terry Pratchett
INSERT INTO book_author (book_id, author_id) VALUES (5, 5); -- Associa

-- Nota sulla gestione degli ID:
-- Se le tue entità usano @GeneratedValue(strategy = GenerationType.IDENTITY),
-- e il tuo ddl-auto è 'create', potresti aver bisogno di sincronizzare le sequenze
-- di auto-incremento dopo questi inserimenti manuali.
-- Per PostgreSQL, potresti aggiungere alla fine dello script:
-- SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;
-- SELECT setval(pg_get_serial_sequence('authors', 'id'), COALESCE(MAX(id), 1)) FROM authors;
-- SELECT setval(pg_get_serial_sequence('books', 'id'), COALESCE(MAX(id), 1)) FROM books;



-- =========================================================================
-- ***** AGGIUNGI QUESTO BLOCCO ALLA FINE DELLO SCRIPT *****
-- Sincronizza i contatori degli ID per PostgreSQL dopo gli inserimenti manuali.
-- Questo comando dice al DB: "guarda l'ID più alto e fai partire il prossimo da lì".
-- Assicurati che i nomi delle tabelle ('users', 'authors', 'books') siano corretti.
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1)) FROM users;
SELECT setval(pg_get_serial_sequence('authors', 'id'), COALESCE(MAX(id), 1)) FROM authors;
SELECT setval(pg_get_serial_sequence('books', 'id'), COALESCE(MAX(id), 1)) FROM books;
-- =========================================================================