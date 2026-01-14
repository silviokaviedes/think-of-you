# Thinking of You

A web application for couples (including polyamorous relationships) to non-verbally signal "I'm thinking of you".

## Features
- **User Accounts**: Registration, Login, Logout.
- **Partner Management**: Search by exact username, send/accept/reject connection requests, multiple partners supported.
- **Real-time Interaction**: Click a button to send a thought, recipient sees the counter increase in real-time via WebSockets.
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
- `POST /api/connections/{id}/think`: Send a thought
- `DELETE /api/connections/{id}`: Disconnect
- `GET /api/metrics?connectionId=...&from=...&to=...&bucketMinutes=...&direction=received|sent`: Get statistics

## Environment Variables
- `MONGO_HOST`: Hostname of the MongoDB server (default: localhost).
