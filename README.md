# SEAL Hackathon - Backend (BE)

This is the robust RESTful API Backend for the **SEAL Hackathon Manager System**, built with Java and Spring Boot. It serves as the core engine for the application, handling complex business logic, database operations, and role-based access control.

## 🚀 Tech Stack

- **Language:** Java (JDK 17+)
- **Framework:** Spring Boot (Spring Web, Spring Security, Spring Data JPA)
- **Database:** MySQL
- **Build Tool:** Maven

## 📋 Prerequisites

Ensure you have the following installed:
- [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/javase-downloads.html) 17 or higher
- [Maven](https://maven.apache.org/) (or use the included Maven wrapper `mvnw`)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/)

## ⚙️ Setup & Installation

1. **Database Configuration**
   - Create a MySQL database (e.g., `seal_hackathon`).
   - Update the `src/main/resources/application.properties` (or `application.yml`) file with your database credentials.

2. **Build the Application**
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

## 📁 Key Features

- **Role-Based Access Control (RBAC):** Secure authentication and authorization for different roles (Admin, Judge, Mentor, User/Participant).
- **Event & Category Management:** Endpoints for creating and managing hackathon events, competition categories, and prize pools.
- **Expert Assignments:** Business logic for assigning experts (Judges/Mentors) to specific categories without overlaps.
- **Milestone & Submission Tracking:** APIs to manage team milestones, project submissions, and judge evaluations.
- **Optimistic Locking:** Implemented to handle concurrent updates and prevent data conflicts (e.g., during expert assignments).

## 🔗 Related Projects
- **[Frontend App](../FE/SEAL-Hackathon-FE)**: The React-based user interface for this backend.

## 📄 Notes
This is a project from students of FPT University - Software Engineering major for the SWP391 course.
