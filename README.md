# Task System

This project is a small Spring Boot learning project built while following the Hyperskill Java backend curriculum.

It is not meant to be a production-ready task manager. The goal is to practice core backend concepts in a realistic but still compact codebase:

- Spring Boot application structure
- REST controllers
- validation
- JPA entities and relationships
- Spring Data repositories
- Spring Security
- basic token-based authentication

## What It Does

The application supports:

- account registration
- token-based authentication
- creating tasks
- assigning tasks to users
- updating task status
- filtering tasks by author and assignee
- adding comments to tasks
- listing task comments

## Backend Structure

Main packages under `src/main/java/com/example/tasksystem`:

- `account`
  User account entity, registration flow, and account lookup.

- `task`
  Task entity, task DTOs, task service, and task endpoints.

- `comment`
  Comment entity, comment DTOs, and comment repository.

- `security`
  Security configuration, bearer token filter, token entity/repository/service, and authentication endpoints.

## Main API Areas

- `POST /api/accounts`
  Register a new user.

- `POST /api/auth/token`
  Authenticate with Basic auth and receive a bearer token.

- `GET /api/tasks`
- `POST /api/tasks`
- `PUT /api/tasks/{taskId}/assign`
- `PUT /api/tasks/{taskId}/status`
  Core task management endpoints.

- `POST /api/tasks/{taskId}/comments`
- `GET /api/tasks/{taskId}/comments`
  Comment endpoints for task discussion.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- H2 database
- Gradle

## Notes

- Data is stored in a local file-based H2 database for simplicity.
- The project was built incrementally as a learning exercise, so the focus is clarity and coverage of Spring concepts rather than production hardening.
