# Rent Video API

A simple Spring Boot REST API for an online video rental system. It uses MySQL for data, Basic Auth for login, and two roles: CUSTOMER and ADMIN.

## Requirements
- Java 17+
- Maven 3.8+ (or open the folder in IntelliJ IDEA, which comes with Maven)
- MySQL 8 running on `localhost:3306`

## Setup
1. Open `src/main/resources/application.properties` and set `spring.datasource.username` and `spring.datasource.password` to your MySQL login.
   The `rent_video` database is created automatically, and so are the tables.
2. Run the app:
   ```
   mvn spring-boot:run
   ```
3. Run the tests (they use an in-memory H2 database, so MySQL isn't needed):
   ```
   mvn test
   ```

## Endpoints

| Method | URL | Access | Description |
|--------|-----|--------|-------------|
| POST | `/api/auth/register` | Public | Register (role defaults to CUSTOMER) |
| POST | `/api/auth/login` | Public | Check email + password, returns user info |
| GET | `/api/auth/me` | Any logged-in user | Current user's info |
| GET | `/api/videos` | CUSTOMER, ADMIN | List available videos |
| GET | `/api/videos/{id}` | CUSTOMER, ADMIN | Get one video |
| POST | `/api/videos` | ADMIN | Create a video |
| PUT | `/api/videos/{id}` | ADMIN | Update a video |
| DELETE | `/api/videos/{id}` | ADMIN | Delete a video |

The API is stateless. After registering, send your email and password as a Basic Auth header with every private request.

## Status codes
- `200 OK` / `201 Created` / `204 No Content`: success
- `400 Bad Request`: missing or invalid fields
- `401 Unauthorized`: not logged in, or wrong email/password
- `403 Forbidden`: logged in but not allowed (e.g. a CUSTOMER creating a video)
- `404 Not Found`: no video with that id
- `409 Conflict`: email already registered

## Example requests (curl)

Register an admin and a customer:
```
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"email\":\"admin@mail.com\",\"password\":\"admin123\",\"firstName\":\"Ann\",\"lastName\":\"Admin\",\"role\":\"ADMIN\"}"

curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"email\":\"john@mail.com\",\"password\":\"john123\",\"firstName\":\"John\",\"lastName\":\"Doe\"}"
```

Log in:
```
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"john@mail.com\",\"password\":\"john123\"}"
```

Create a video (ADMIN):
```
curl -u admin@mail.com:admin123 -X POST http://localhost:8080/api/videos -H "Content-Type: application/json" -d "{\"title\":\"Inception\",\"director\":\"Christopher Nolan\",\"genre\":\"Sci-Fi\"}"
```

List videos (any logged-in user):
```
curl -u john@mail.com:john123 http://localhost:8080/api/videos
```

Update and delete (ADMIN):
```
curl -u admin@mail.com:admin123 -X PUT http://localhost:8080/api/videos/1 -H "Content-Type: application/json" -d "{\"title\":\"Inception\",\"director\":\"Christopher Nolan\",\"genre\":\"Thriller\",\"available\":true}"

curl -u admin@mail.com:admin123 -X DELETE http://localhost:8080/api/videos/1
```

## Project structure
```
src/main/java/com/example/rentvideo
├── RentVideoApplication.java
├── config/SecurityConfig.java          # Basic Auth, BCrypt, role rules
├── controller/AuthController.java      # register / login / me
├── controller/VideoController.java     # video CRUD
├── dto/                                # request/response objects
├── entity/User.java, Video.java, Role.java
├── exception/                          # custom errors + global handler
├── repository/                         # Spring Data JPA repositories
└── service/                            # business logic + UserDetailsService
```

## Note
As the spec requires, anyone can choose `"role":"ADMIN"` when registering. In a real system you would only let an existing admin create other admins.
