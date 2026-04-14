project for https://roadmap.sh/projects/image-processing-service
# Image Processing Service

A RESTful microservice for uploading, managing, and serving images with JWT-based authentication. Built with Spring Boot 4, PostgreSQL, and Spring Security.

## 🚀 Features

- **User Authentication** — Register and login with JWT token-based authentication
- **Image Upload** — Upload images with automatic validation (format, size, dimensions)
- **Image Management** — List, view details, and delete your images
- **Image Download** — Download images with proper content-type headers
- **Pagination & Sorting** — Browse images with pagination, sorted by upload date
- **Security** — JWT authentication, CORS support, input validation, path traversal protection
- **File Validation** — Automatic validation for file type (JPEG, PNG, GIF, WebP, BMP), size (max 10MB), and format
- **Error Handling** — Global exception handling with structured JSON error responses
- **Cleanup on Failure** — Automatic file cleanup when upload fails

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| **Java 17** | Programming language |
| **Spring Boot 4.0.3** | Framework |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Database access |
| **JWT (jjwt 0.12.3)** | Token-based authentication |
| **PostgreSQL** | Database |
| **Lombok** | Boilerplate code reduction |
| **Hibernate Validator** | Input validation |

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- (Optional) Docker for containerized deployment

## 🔧 Installation

### 1. Clone the Repository

```bash
git clone https://gitlab.com/your-username/image-processing-service.git
cd image-processing-service
```

### 2. Configure the Database

Create a PostgreSQL database:

```sql
CREATE DATABASE imageservice_db;
```

Update database credentials in `.env` or `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/imageservice_db
spring.datasource.username=postgres
spring.datasource.password=your-password
```

### 3. Configure Environment Variables

Copy the example environment file and customize it:

```bash
cp .env.example .env
```

Edit `.env` with your values:

```env
JWT_SECRET=your-super-secret-jwt-key-minimum-32-chars
SPRING_DATASOURCE_PASSWORD=your-database-password
STORAGE_LOCATION=/path/to/uploads
```

> **Important**: Generate a strong JWT secret using: `openssl rand -base64 64`

### 4. Build the Application

```bash
./mvnw clean package -DskipTests
```

### 5. Run the Application

```bash
java -jar target/image-processing-service-0.0.1-SNAPSHOT.jar
```

Or with Maven:

```bash
./mvnw spring-boot:run
```

The service will start on `http://localhost:8080`.

## 📚 API Documentation

### Authentication Endpoints

#### Register a New User

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securepass123"
}
```

**Response (200 OK):**
```json
{
  "token": null,
  "username": "john_doe",
  "message": "User registered successfully"
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securepass123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_doe",
  "message": "Login successful"
}
```

### Image Endpoints

> All image endpoints require JWT authentication. Include the token in the Authorization header:
> ```
> Authorization: Bearer <your-jwt-token>
> ```

#### Upload Image

```http
POST /api/images/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <image-file>
```

**Supported formats:** JPEG, PNG, GIF, WebP, BMP  
**Max file size:** 10 MB

**Response (201 Created):**
```json
{
  "id": 1,
  "originalFilename": "photo.jpg",
  "format": "image/jpeg",
  "fileSize": 2048576,
  "width": 1920,
  "height": 1080,
  "uploadedAt": "2026-04-14T10:30:00",
  "username": "john_doe"
}
```

#### List User Images (Paginated)

```http
GET /api/images?page=0&size=10
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "originalFilename": "photo.jpg",
      "format": "image/jpeg",
      "fileSize": 2048576,
      "width": 1920,
      "height": 1080,
      "uploadedAt": "2026-04-14T10:30:00",
      "username": "john_doe"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

#### Get Image Details

```http
GET /api/images/{id}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "originalFilename": "photo.jpg",
  "format": "image/jpeg",
  "fileSize": 2048576,
  "width": 1920,
  "height": 1080,
  "uploadedAt": "2026-04-14T10:30:00",
  "username": "john_doe"
}
```

#### Download Image

```http
GET /api/images/{id}/download
Authorization: Bearer <token>
```

**Response (200 OK):**  
Returns the image file with proper `Content-Type` and `Content-Disposition` headers.

#### Delete Image

```http
DELETE /api/images/{id}
Authorization: Bearer <token>
```

**Response (200 OK):**
```
Image deleted successfully
```

### Error Responses

All errors follow a consistent JSON format:

```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Validation failed: username - Username must be between 3 and 50 characters; password - Password is required;",
  "timestamp": 1744629000000
}
```

| HTTP Status | Error Type | Description |
|-------------|-----------|-------------|
| 400 | Bad Request | Invalid input or validation error |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Unexpected server error |

## 🧪 Testing

Run all tests:

```bash
./mvnw test
```

Run tests with coverage:

```bash
./mvnw test jacoco:report
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t image-processing-service .
```

### Run with Docker

```bash
docker run -d \
  --name image-service \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/imageservice_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=your-secret-key \
  -v /path/to/uploads:/app/uploads \
  image-processing-service
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    image: image-processing-service:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/imageservice_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: your-secret-key
      STORAGE_LOCATION: /app/uploads
    volumes:
      - uploads:/app/uploads
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      POSTGRES_DB: imageservice_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
  uploads:
```

Run:

```bash
docker-compose up -d
```

## 🔒 Security Considerations

- **JWT Secret**: Always use a strong, randomly generated secret in production
- **Password Hashing**: BCrypt with automatic salt generation
- **File Validation**: Only allowed image formats are accepted
- **Path Traversal Protection**: Filenames are validated to prevent directory traversal attacks
- **CORS**: Configurable allowed origins (default: localhost development URLs)
- **Input Validation**: All endpoints validate input with structured error responses

## 📁 Project Structure

```
src/main/java/com/karimkhan/image_processing_service/
├── controller/
│   ├── AuthController.java          # Authentication endpoints
│   └── ImageController.java         # Image CRUD endpoints
├── dto/
│   ├── AuthRequest.java             # Login/Register request DTO
│   ├── AuthResponse.java            # Auth response DTO
│   ├── ErrorResponse.java           # Error response DTO
│   └── ImageResponse.java           # Image response DTO
├── exception/
│   ├── GlobalExceptionHandler.java   # Global error handling
│   ├── ResourceNotFoundException.java
│   ├── FileStorageException.java
│   ├── InvalidFileException.java
│   └── UnauthorizedException.java
├── model/
│   ├── Image.java                   # Image entity
│   └── User.java                    # User entity
├── repository/
│   ├── ImageRepository.java         # Image data access
│   └── UserRepository.java          # User data access
├── security/
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── JwtAuthenticationFilter.java  # JWT request filter
│   ├── JwtUtil.java                 # JWT token utilities
│   └── UserDetailsServiceImpl.java   # User details service
└── service/
    ├── ImageService.java            # Image business logic
    └── StorageService.java          # File storage operations
```

## 🔧 Configuration

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `spring.datasource.url` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/imageservice_db` |
| `jwt.secret` | JWT signing secret | *(required)* |
| `jwt.expiration` | Token expiration in ms | `86400000` (24h) |
| `storage.location` | Upload directory path | `uploads` |
| `cors.allowed-origins` | Allowed CORS origins | `http://localhost:3000,http://localhost:5173` |
| `server.port` | HTTP server port | `8080` |

## 🐛 Troubleshooting

### Database Connection Error

Ensure PostgreSQL is running and credentials are correct:

```bash
psql -U postgres -d imageservice_db
```

### JWT Token Expired

Tokens expire after 24 hours by default. Login again to get a new token, or adjust `jwt.expiration`.

### File Upload Fails

- Check file size (max 10MB)
- Verify file format (JPEG, PNG, GIF, WebP, BMP only)
- Ensure `storage.location` directory has write permissions

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Merge Request

