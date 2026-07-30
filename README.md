# SEAL Hackathon - Backend

This is the robust RESTful API Backend for the **SEAL Hackathon Manager System**, built with Java and Spring Boot. It serves as the core engine for the application, handling complex business logic, database operations, and role-based access control.

## 🚀 Tech Stack

- **Language:** Java (JDK 17+)
- **Framework:** Spring Boot (Spring Web, Spring Security, Spring Data JPA)
- **Database:** MySQL
- **Build Tool:** Maven
- **Architecture:** Layered Architecture (Controller, Service, Repository)

## ✨ Key Features

- **Role-Based Access Control (RBAC):** Secure authentication and authorization for different roles (Admin, Judge, Mentor, User/Participant) using Spring Security.
- **Event & Category Management:** Endpoints for creating and managing hackathon events, competition categories, rounds, and criteria.
- **Judging & Evaluation Engine:** Robust APIs to manage judges' batch scoring, score calibration, final round approvals, and dynamic leaderboards.
- **Mentorship & Consultation:** Built-in module for managing mentor-team consultations, booking sessions, and tracking interactions.
- **Awards & Prize Distribution:** Comprehensive logic for managing awards configurations and distributing them to top-ranking teams.
- **Milestone & Submission Tracking:** APIs to manage team milestones, project submissions, and tracking round completion statuses.
- **Concurrency Control:** Optimistic Locking implemented to handle concurrent updates and prevent data conflicts (e.g., during expert assignments or concurrent scoring).

## 📋 Prerequisites

Ensure you have the following installed:
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-downloads.html) 17 or higher
- [Maven](https://maven.apache.org/) (or use the included Maven wrapper `mvnw`)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/)

## ⚙️ Setup & Installation

1. **Database Initialization & Configuration**
   - Create a SQL Server/MySQL database (e.g., `seal_hackathon`).
   - **Crucial Step:** The project uses `spring.jpa.hibernate.ddl-auto=none`, meaning the schema is *not* auto-generated. You **must** execute the SQL scripts located in the `database/` directory (start with `Create Database.sql`) to initialize the tables. You can also run `seed_api_test_data.sql` to populate sample data.

2. **Environment Variables**
   - The application requires several environment variables to run properly. Set these in your environment or IDE, or update `src/main/resources/application.properties` directly:
     - **Database:** `DB_URL`, `DB_USER`, `DB_PASS`
     - **URLs:** `FRONTEND_URL`, `BACKEND_URL`
     - **Authentication:** `JWT_SECRET_KEY`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
     - **Services:** `GEMINI_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`

3. **Build the Application**
   Navigate to the `SEAL-Hackathon-BE` directory and run:
   ```bash
   ./mvnw clean install
   # Or if you have Maven installed globally:
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   # Or using global Maven:
   mvn spring-boot:run
   ```
   The backend API will start on `http://localhost:8080`.

## 🔗 Related Projects
- **[Frontend App](../../FE/SEAL-Hackathon-FE)**: The React-based user interface for this backend.

## 📄 Notes
This is a project from students of FPT University - Software Engineering major for the SWP391 course.
