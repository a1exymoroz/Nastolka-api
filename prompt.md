Act as an expert Java developer. I am building a learning project: a backend API for a board game application using Java and Spring Boot. Please generate the foundational code, leaving the core implementation details (like actual query logic, JWT generation/parsing algorithms, and database configurations) as `TODO` or placeholders `[...]` for me to complete.

Requirements:
1. Entities:
   - `User` entity (id, username, password, email).
   - `Game` entity (id, title, description, maxPlayers, etc.).
2. Authentication (JWT):
   - A basic `AuthController` with `/register` and `/login` endpoints.
   - A boilerplate `JwtUtil` class or filter, but leave the actual token creation and validation logic as a `TODO` for me to implement.
   - Security configuration (Spring Security) that allows public access to auth endpoints but secures the game endpoints. Leave the specific filter chain details as a `TODO`.
3. Game API:
   - A `GameController` with an endpoint to fetch all available games (`GET /api/games`).
4. Architecture:
   - Provide the basic standard Spring Boot layers: Controllers, Services (Interfaces and empty implementations), and Repositories (Spring Data JPA interfaces).

Please provide the code for the Entities, Repositories, Services, Controllers, and Security Configuration, using standard annotations (`@RestController`, `@Entity`, `@Service`, etc.). Do not write the full implementation for the JWT filter and Service logic, just set up the structure!