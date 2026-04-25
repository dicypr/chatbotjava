# GroqBot — Java Chatbot with Groq API

A full-stack chatbot built with **Java 17 + Spring Boot 3** on the backend
and a sleek terminal-aesthetic frontend, powered by **Groq's LPU inference** for ultra-fast responses.

---

## Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Language   | Java 17                             |
| Framework  | Spring Boot 3.2                     |
| HTTP Client| java.net.http.HttpClient (built-in) |
| AI API     | Groq API (OpenAI-compatible)        |
| Models     | llama3-70b, llama3-8b, mixtral-8x7b, gemma2-9b |
| Frontend   | Vanilla HTML/CSS/JS (served by Spring Boot) |
| Build      | Maven                               |

---

## Project Structure

```
groq-chatbot/
├── pom.xml
└── src/main/
    ├── java/com/groqbot/
    │   ├── GroqChatbotApplication.java   # Entry point
    │   ├── controller/
    │   │   └── ChatController.java       # REST endpoints
    │   ├── service/
    │   │   └── GroqService.java          # Groq API calls
    │   └── model/
    │       ├── ChatMessage.java          # Message DTO
    │       ├── ChatRequest.java          # Request DTO
    │       └── ChatResponse.java         # Response DTO
    └── resources/
        ├── application.properties        # Config
        └── static/
            └── index.html               # Frontend UI
```

---

## Setup

### 1. Get a Groq API key
Sign up free at https://console.groq.com → API Keys → Create Key

### 2. Set your API key

**Linux/Mac:**
```bash
export GROQ_API_KEY=gsk_your_key_here
```

**Windows (CMD):**
```cmd
set GROQ_API_KEY=gsk_your_key_here
```

**Or edit `application.properties`** directly (not recommended for production):
```properties
groq.api.key=gsk_your_key_here
```

### 3. Build and run

```bash
# Clone / navigate to project directory
cd groq-chatbot

# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
```

### 4. Open the chatbot

Visit **http://localhost:8080** in your browser.

---

## API Endpoints

### `POST /api/chat`
Send a message and get a response.

**Request:**
```json
{
  "message": "Hello, what can you do?",
  "history": [],
  "model": "llama3-70b-8192"
}
```

**Response:**
```json
{
  "reply": "I can help you with...",
  "model": "llama3-70b-8192",
  "promptTokens": 45,
  "completionTokens": 120,
  "totalTokens": 165,
  "success": true,
  "error": null
}
```

### `GET /api/models`
Returns available models.

### `GET /api/health`
Health check endpoint.

---

## How it Works

```
Browser  →  POST /api/chat  →  ChatController
                                    ↓
                               GroqService
                                    ↓
                         java.net.http.HttpClient
                                    ↓
                          api.groq.com/openai/v1/chat/completions
                                    ↓
                               Parse JSON
                                    ↓
                           ChatResponse  →  Browser
```

The app uses Java's **built-in `HttpClient`** (no external HTTP library) to call
Groq's OpenAI-compatible endpoint. The full conversation history is sent with
each request to maintain context (Groq / LLMs are stateless).

---

## Adding Memory / Persistence

To persist conversations across restarts, add a DB layer:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
</dependency>
```

Then create a `Conversation` entity and save/load history by session ID.

---

## License
MIT
