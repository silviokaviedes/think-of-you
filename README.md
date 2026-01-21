# Thinking of You

A web application for couples (including polyamorous relationships) to non-verbally signal "I'm thinking of you".

## Features
- **User Accounts**: Registration, Login, Logout.
- **Partner Management**: Search by exact username, send/accept/reject connection requests, multiple partners supported.
- **Real-time Interaction**: Click a button to send a thought, recipient sees the counter increase in real-time via WebSockets.
- **Mood Support**: Optionally add a mood (happy, sad, angry, love, excited, worried, grateful, or neutral) to each thought.
- **Statistics**: View click history over time (1, 7, 30 days) with adjustable granularity.
- **Responsive Design**: Works on Desktop, Tablet, and Mobile.

## Architecture
- **Backend**: Spring Boot (Java 21), Spring Security (JWT), Spring Data MongoDB, Spring WebSocket.
- **Frontend**: Vanilla JS, CSS3, HTML5, Chart.js, SockJS, Stomp.
- **Database**: MongoDB.

## Getting Started

### Prerequisites
- Docker and Docker Compose

### Run the Application
Start everything with a single command:
```bash
docker compose up --build
```
The application will be available at `http://localhost:8080`.

## Deployment
For instructions on how to deploy this application to production easily, see the [Deployment Guide](DEPLOYMENT.md).

## API Documentation
- `POST /api/auth/register`: { username, password }
- `POST /api/auth/login`: { username, password } -> returns JWT
- `GET /api/users/search?username=...`: Search for a user
- `POST /api/connections/request`: { username } - Request a connection
- `GET /api/connections`: Get accepted partners
- `GET /api/connections/requests`: Get pending requests
- `POST /api/connections/{id}/accept`: Accept request
- `POST /api/connections/{id}/reject`: Reject request
- `POST /api/connections/{id}/think`: { mood } - Send a thought with optional mood (happy, sad, angry, love, excited, worried, grateful, none)
- `DELETE /api/connections/{id}`: Disconnect
- `GET /api/metrics?connectionId=...&from=...&to=...&bucketMinutes=...&direction=received|sent`: Get statistics

## Environment Variables
- `MONGO_HOST`: Hostname of the MongoDB server (default: localhost).

## Functions and Requirements

### Core Functions
- **User Authentication**: Secure registration and login system with JWT tokens
- **Partner Connections**: Search for users by exact username and send connection requests
- **Real-time Notifications**: WebSocket-based live updates when someone thinks of you
- **Thought Tracking**: Click counter to track "thinking of you" interactions between partners
- **Mood Expression**: Add emotional context to thoughts with 8 different mood options
- **Relationship Analytics**: View statistics of thought exchanges over customizable time periods
- **Multi-partner Support**: Supports polyamorous relationships with multiple simultaneous connections
- **Connection Management**: Accept, reject, or disconnect from partner requests

### Technical Requirements
- **Java 21**: Runtime environment requirement
- **Spring Boot 3.4.1**: Main application framework
- **MongoDB**: NoSQL database for data persistence
- **Docker & Docker Compose**: Containerization and orchestration
- **WebSocket Support**: Real-time bidirectional communication
- **JWT Authentication**: Stateless authentication mechanism

### Data Models
- **User**: Stores username, password hash, and creation timestamp
- **Connection**: Manages relationship status between users with bidirectional counters
- **ThoughtEvent**: Logs individual "thinking of you" events with timestamps and mood
- **Mood**: Enum with 8 emotional states (happy, sad, angry, love, excited, worried, grateful, none)

### Security Features
- Password hashing with Spring Security
- JWT-based stateless authentication
- Request validation and authorization
- CORS configuration for cross-origin requests

### Performance Features
- MongoDB indexing for unique usernames and optimized queries
- Connection pooling for database operations
- Efficient WebSocket message handling
- Responsive design for all device types
