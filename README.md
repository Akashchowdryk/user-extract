#  User Extract Backend

This is a Spring Boot backend application that fetches, processes, and provides user data through REST APIs. It supports data extraction, transformation, and integration with frontend applications.

---

##  Features

* Fetch user data from external APIs
* Process and transform user details
* REST API endpoints for frontend consumption
* JSON-based response handling
* CORS enabled for frontend integration

---

##  Tech Stack

* Java 21
* Spring Boot
* Spring Web
* Jackson (JSON processing)
* Maven

---

##  Project Structure

```
userextract/
│── src/main/java/com/user/userextract/
│   ├── controller/
│   ├── service/
│   ├── model/
│   └── UserExtractApplication.java
│
│── src/main/resources/
│   └── application.properties
│
│── pom.xml
```

---

##  Setup & Run

###  Clone Repository

```
git clone https://github.com/your-username/user-extract-backend.git
cd user-extract-backend
```

###  Build Project

```
mvn clean install
```

###  Run Application

```
mvn spring-boot:run
```

---

##  API Endpoints

| Method | Endpoint   | Description       |
| ------ | ---------- | ----------------- |
| GET    | /api/fetch | Fetch user data   |
| POST   | /api/edit  | Edit user details |

---

##  Example Response

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

##  Testing

* Use Postman or browser:

```
http://localhost:9090/api/fetch
```

---

##  Notes

* Ensure backend runs on **port 9090**
* Enable CORS for frontend integration
* No database required (API-based data)

---

## 👨‍💻 Author

Akash Chowdry k

---
