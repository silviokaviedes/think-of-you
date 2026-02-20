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
- **Per-Connection Stats Switching**: Switch the statistics view between partners.
- **Last Mood Indicators**: See the most recent received and sent mood emoji per connection.
- **Dashboard Display Preference**: Switch dashboard cards between count totals and last event timestamp + emoji.
- **Responsive Design**: Works on Desktop, Tablet, and Mobile with a burger menu and bottom tab navigation.

## Architecture
- **Backend**: Spring Boot (Java 21), Spring Security (JWT), Spring Data MongoDB, Spring WebSocket.
- **Frontend**: Vue 3 + TypeScript (Vite), Chart.js, SockJS, STOMP.
- **Database**: MongoDB.

## Project Structure
```
think-of-you-3/
├── frontend/                     # Vue 3 + TypeScript frontend (Vite)
│   ├── src/
│   │   ├── assets/               # CSS and static assets
│   │   ├── App.vue               # Main UI
│   │   └── main.ts               # Vue entrypoint
│   ├── index.html                # Vite HTML template
│   ├── package.json              # Frontend dependencies/scripts
│   ├── tsconfig.json             # TS config
│   └── vite.config.ts            # Vite build config
├── src/
│   ├── main/
│   │   ├── java/de/kaviedes/thinkofyou3/
│   │   │   ├── config/           # WebSocket & Security config
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── model/            # MongoDB models
│   │   │   ├── repository/       # MongoDB repositories
│   │   │   ├── security/         # JWT utilities/filter
│   │   │   ├── service/          # Business logic
│   │   │   └── ThinkOfYou3Application.java
│   │   └── resources/
│   │       ├── static/           # Built frontend assets (from Vite)
│   │       └── application.properties
│   └── test/                     # Unit tests
├── build.gradle                  # Gradle dependencies
├── Dockerfile                    # Docker build instructions
├── docker-compose.yml            # Docker Compose configuration
└── README.md                     # This file
```

## Quick Start (Docker Compose)

The easiest way to run the application locally is with Docker Compose:

```bash
docker compose up --build
```

That's it! The application will be available at `http://localhost:8080`.

Docker Compose will automatically:
- Build and start the Spring Boot application
- Start a MongoDB database
- Connect the application to the database
- Handle all networking and data persistence

### Stopping the Application
```bash
docker compose down
```

### Development with Docker Compose
For development with hot reload:
```bash
docker compose up --build --watch
```

## Alternative: Manual Local Setup

If you prefer to run everything locally without Docker:

### Prerequisites
- Java 21
- MongoDB running locally
- Node.js 20+ (for frontend build/dev)

### Steps
1. **Start MongoDB** (if not already running):
   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

2. **Run the Spring Boot application**:
   ```bash
   # Unix/Linux/MacOS
   ./gradlew bootRun
   
   # Windows
   gradlew.bat bootRun
   ```

3. **Access the application**:
   Open `http://localhost:8080` in your browser

### Development Notes
- The Vue frontend uses Vite for hot reload and proxies API/WebSocket traffic
- Backend changes require a restart of the Spring Boot application
- Default MongoDB connection is to `localhost:27017`
- JWT tokens are stored in localStorage for development

### Frontend (Vue 3 + TypeScript)
The frontend source lives in `frontend/` and is built into `src/main/resources/static`.

#### Run frontend dev server (with backend running)
```bash
cd frontend
npm install
npm run dev
```

#### Build frontend for Spring Boot
```bash
cd frontend
npm install
npm run build
```

## Android (Capacitor)

This repo includes a Capacitor-based Android project under `frontend/android`.

### Quick Android build steps
```bash
cd frontend
npm install
npm run build
npx cap sync android
npx cap open android
```

Notes:
- Android Studio is required to open and build the Android project.
- The Capacitor webDir is set to `../src/main/resources/static` to reuse the existing Vite output.
- If you change web assets, rerun `npm run build` and `npx cap sync android`.

### Push notifications (FCM)
Client:
- Runtime permission is requested on Android 13+.
- The app registers the device token and calls `POST /api/push/register`.

Server:
- Set Firebase credentials with one of:
  - `FIREBASE_SERVICE_ACCOUNT_BASE64` (base64 JSON)
  - `FIREBASE_SERVICE_ACCOUNT_PATH` (path to JSON)
  - `GOOGLE_APPLICATION_CREDENTIALS` (fallback)
- Place `google-services.json` at `frontend/android/app/google-services.json` (do not commit it).
  - Without this file, Android builds will work but push notifications will not.

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
- `POST /api/push/register`: { token, platform } - Register device token for push notifications
- `GET /api/users/preferences/moods`: Get available moods + user's favorite mood buttons
- `PUT /api/users/preferences/moods`: { favoriteMoods } - Update favorite mood buttons (max 8)
- `GET /api/users/preferences/dashboard`: Get dashboard display mode (`counts` or `last_event`)
- `PUT /api/users/preferences/dashboard`: { mode } - Update dashboard display mode

## Environment Variables
- `MONGO_HOST`: Hostname of the MongoDB server (default: localhost).
- `FIREBASE_SERVICE_ACCOUNT_BASE64`: Base64-encoded Firebase service account JSON for FCM.
- `FIREBASE_SERVICE_ACCOUNT_PATH`: Path to Firebase service account JSON (alternative to base64).
- `GOOGLE_APPLICATION_CREDENTIALS`: Standard Firebase credentials path (fallback).

## UI/UX Features

### Mobile-First Navigation
- **Burger Menu**: Responsive hamburger menu for mobile devices (768px and below)
- **Bottom Tabs**: Quick navigation for Dashboard, Search, and Stats on mobile
- **Smooth Animations**: CSS transitions for menu toggle with transform effects
- **Accessible Design**: Proper ARIA labels and semantic HTML structure
- **Touch-Friendly**: Large tap targets optimized for mobile interaction

### Dashboard Card Modes
- **Counts Mode**: Shows total sent and received counts per connection.
- **Last Event Mode**: Hides totals and shows only the latest event timestamp and emoji for sent/received.
- **Profile Control**: Each user can switch this behavior from the Profile page.

### Design System
- **Modern Glass-morphism**: Frosted glass effect with backdrop blur
- **Consistent Theming**: CSS custom properties for colors and spacing
- **Micro-interactions**: Hover states, button animations, and visual feedback
- **Responsive Grid**: Flexible layouts that adapt to all screen sizes

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
- **Mood Expression**: Add emotional context to thoughts with a larger emoji set and configurable favorites
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
- **User Preferences**: Stores favorite moods and dashboard display mode (`counts` or `last_event`)
- **Connection**: Manages relationship status between users with bidirectional counters
- **ThoughtEvent**: Logs individual "thinking of you" events with timestamps and mood
- **Mood**: Enum with an expanded mood set (including hug and exhausted)
- **MoodMetricsDTO**: Data transfer object containing time-bucketed mood distributions and total mood counts
- **ConnectionDTO**: Includes counts, last sent/received mood, and last sent/received timestamp

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
