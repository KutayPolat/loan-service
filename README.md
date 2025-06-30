# loan-service v.0.1

- clone project and run 'mvn clean install -T4'
- Uses an in-memory H2 database (auto-reset on each run). 
- Tables and sample data are auto-initialized at startup.Includes a default admin user and a sample user with customerId = 1.
- it will create users to test easily. 'admin' user and sample user which customerId = 1
- Use the provided Postman collection to test endpoints.
- Typical flow:
  - Authenticate (login)
  - Create a loan for the user
  - List active loans and their installments
  - Pay an installment using the payment endpoint

📌 Available APIs

login, Authenticate and receive JWT token
Create a new loan
List all loans for the logged-in user
View all installments for a loan
Pay a loan installment

🚀 Open to Improvements for V.0.2

- Logging : Introduce a more structured and centralized logging mechanism (e.g., SLF4J + Logback with log levels and trace IDs).

- Add a caching layer (e.g. Redis) to store user info and invalidate cache on user updates.

- Response Handling : Improve response consistency using a global response wrapper and exception handler
