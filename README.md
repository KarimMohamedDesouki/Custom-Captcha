# Custom CAPTCHA with Spring Boot 3 & Java 17

This project implements a custom CAPTCHA system using Spring Boot 3 and Java 17. The CAPTCHA is designed to enhance security by preventing automated bot interactions.

## Features
- ✅ Custom-generated CAPTCHA images
- 🔒 Secure and lightweight implementation
- ⚡ Easy integration with Spring Boot authentication
- 🔧 Configurable CAPTCHA complexity
- 📡 REST API for CAPTCHA generation and validation

## 🛠 Technologies Used
- **Spring Boot 3.2.3**
- **Java 17**
- **Thymeleaf** (if used for rendering CAPTCHA)
- **JCaptcha / Custom Java Graphics API**
- **MinIO** (optional for storage)
- **MongoDB / MySQL** (optional for CAPTCHA tracking)

## 🚀 Setup & Installation

### Prerequisites
Ensure you have the following installed:
- Java 17 ☕
- Maven 🛠
- Spring Boot CLI (optional) 🚀
- Git 🖥

### Clone the Repository
```sh
git clone https://github.com/your-username/custom-captcha-springboot.git
cd custom-captcha-springboot
```

### Clone the RepositoryRun the Project
```sh
clean install
mvn spring-boot:run
```

## 📡 API Endpoints

| Method | Endpoint            | Description                        |
|--------|---------------------|------------------------------------|
| GET    | `/captcha/generate` | Generates a new CAPTCHA image     |
| POST   | `/captcha/validate` | Validates user input against CAPTCHA |

## ⚙️ Configuration  
Modify `application.properties` or `application.yml` to configure CAPTCHA settings such as expiration time, difficulty, and storage options.  

## 🤝 Contributing  
Pull requests are welcome! Please open an issue first to discuss changes.  
