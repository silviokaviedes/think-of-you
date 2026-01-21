# Thinking of You

A web application for couples (including polyamorous relationships) to non-verbally signal "I'm thinking of you".

## Features
- **User Accounts**: Registration, Login, Logout.
- **Partner Management**: Search by exact username, send/accept/reject connection requests, multiple partners supported.
- **Real-time Interaction**: Click a button to send a thought, recipient sees the counter increase in real-time via WebSockets.
- **Instant Notifications**: Both sender and receiver get immediate notifications when thoughts are sent, including mood context.
- **Mood Support**: Optionally add a mood (happy, sad, angry, love, excited, worried, grateful, or neutral) to each thought.
- **Enhanced Statistics**: View click history over time (1, 7, 30 days) with adjustable granularity and mood distribution visualization.
- **Mood Analytics**: Stacked bar charts showing mood patterns and summary statistics for emotional insights.
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
- `GET /api/metrics/moods?connectionId=...&from=...&to=...&bucketMinutes=...&direction=received|sent`: Get mood-based statistics with distribution data

## Environment Variables
- `MONGO_HOST`: Hostname of the MongoDB server (default: localhost).

## Mood Statistics Feature

The application now includes comprehensive mood analytics in the statistics dashboard:

### Visualization Features
- **Stacked Bar Chart**: Time-based view showing mood distribution across selected periods
- **Color-Coded Moods**: Each mood has a distinct color for easy identification
- **Interactive Tooltips**: Hover over chart segments to see detailed mood counts
- **Mood Summary Grid**: Below the chart displaying total counts for each mood

### Available Moods
- 😊 Happy (Gold)
- 😢 Sad (Blue)
- 😠 Angry (Red-Orange)
- ❤️ Love (Hot Pink)
- 🤗 Excited (Orange)
- 😟 Worried (Purple)
- 🙏 Grateful (Green)
- 💭 Neutral (Gray)

### Filtering Options
- **Time Periods**: Last 24 hours, 7 days, or 30 days
- **Time Buckets**: Adjustable granularity (hourly, daily, etc.)
- **Direction**: Filter by sent or received thoughts
- **Per-Partner View**: Statistics shown for individual connections

## Notification System

The application features a comprehensive notification system for real-time engagement:

### Sender Notifications
- Immediate confirmation when a thought is sent successfully
- Displays the selected mood emoji for context
- Example: "Sent a thought 😊!"

### Receiver Notifications
- Real-time alerts when someone is thinking of you
- Shows the sender's username and their mood
- Example: "John is thinking of you ❤️!"
- Automatically updates the received counter

### Technical Implementation
- WebSocket-based instant messaging
- Separate message types for thoughts vs. system updates
- Toast-style notifications in the bottom-right corner
- 3-second auto-dismiss duration

## Functions and Requirements

### Core Functions
- **User Authentication**: Secure registration and login system with JWT tokens
- **Partner Connections**: Search for users by exact username and send connection requests
- **Real-time Notifications**: WebSocket-based live updates when someone thinks of you, with mood context
- **Bidirectional Notifications**: Both sender and receiver receive immediate feedback for every interaction
- **Thought Tracking**: Click counter to track "thinking of you" interactions between partners
- **Mood Expression**: Add emotional context to thoughts with 8 different mood options
- **Relationship Analytics**: View statistics of thought exchanges over customizable time periods
- **Mood Analytics**: Visualize emotional patterns with stacked bar charts and mood distribution summaries
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
- **MoodMetricsDTO**: Data transfer object containing time-bucketed mood distributions and total mood counts

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
