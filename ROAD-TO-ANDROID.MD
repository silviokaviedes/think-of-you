# Road to Android (Option A: Capacitor-only + Push Notifications)

This guide outlines the exact steps to turn the current Vue 3 app into an Android app using Capacitor only (no Ionic UI changes), with real push notifications via Firebase Cloud Messaging (FCM).

Assumptions:
- You are on Windows.
- You have Android Studio installed.
- You will host the backend + frontend at a public HTTPS URL for production.

## 0) Prerequisites

Install or verify the following:
- Node.js 20+
- Java 21 (already used by backend)
- Android Studio (with Android SDK + emulators)
- Firebase account

Recommended tools:
- npx available in PATH
- adb available in PATH (Android Studio installs it)

Notes:
- This assumes the Vue app is a client‑side SPA that can run entirely from the bundled `dist` assets inside a WebView. If you rely on server‑side routing or server rewrites for deep links, ensure the app still works when served from the local bundle.
- Push notifications on Android 13+ require runtime permission and token registration in the app. This is the main engineering change, along with backend storage and FCM send logic.
- If you need background data sync or more native features beyond push notifications, you’ll likely need additional native plugins or platform‑specific work later.
- Android Studio installation is required before running any Android build/sync steps. Easiest path:
  - Download Android Studio from the official site.
  - During setup, select the **Android SDK**, **Android SDK Platform**, and **Android SDK Build‑Tools** components.
  - After install, open Android Studio once so it can finish SDK setup.

## 1) Add Capacitor to the Vue app (no Ionic)

From the repo root:

```bash
cd frontend
npm install
npm install @capacitor/core @capacitor/cli
npx cap init "Thinking of You" "de.kaviedes.thinkofyou"
```

When prompted:
- App name: Thinking of You
- App ID (package name): de.kaviedes.thinkofyou
- Web directory: ../src/main/resources/static (matches current Vite output)

## 2) Build the web app

```bash
npm run build
```

This outputs the bundled assets to `src/main/resources/static`, which Capacitor will package into the Android app via `webDir`.

## 3) Add Android platform

```bash
npx cap add android
```

This creates frontend/android.

## 4) Open Android project

```bash
npx cap open android
```

Android Studio will open the project. Let Gradle sync.

## 5) Configure API base URL for production

Your current frontend uses relative /api/... URLs, which work if the Android WebView loads the same origin as the backend.

You have two choices:

A) Host the frontend with the backend (recommended):
- Backend serves the built frontend at the same domain
- No code changes needed for API calls

B) Host frontend separately:
- You must add a VITE_API_BASE_URL and change fetch calls to use it
- This requires code changes later

For now, use option A for minimal changes.

## 6) Configure Android WebView to load the built app

Capacitor will load the compiled static assets locally by default.

If you want the app to load live content from your hosted URL instead (useful for quick updates), set:

```bash
npx cap set android --scheme https --hostname your-domain.com
```

This is optional. If not set, it will load the bundled build.

## 7) Add Push Notifications (FCM)

### 7.1 Create Firebase project
- Go to Firebase Console
- Create a new project
- Add Android app with the same package name: de.kaviedes.thinkofyou
- Download google-services.json

### 7.2 Place Firebase config
- Copy google-services.json into:

```
frontend/android/app/google-services.json
```

### 7.3 Add Capacitor Push Notifications plugin

```bash
cd frontend
npm install @capacitor/push-notifications
npx cap sync android
```

### 7.4 Enable Firebase in Android build

Android Studio should already pick up google-services.json. If not:
- Ensure frontend/android/app/build.gradle applies the Google Services plugin
- Capacitor usually handles this, but verify if build errors appear

### 7.5 Add runtime permissions + token registration
Android 13+ requires explicit notification permission. Capacitor provides APIs to request it.

You will need a small amount of frontend code to:
- Request notification permission on app start
- Register the device to receive FCM tokens
- Send the token to your backend

(These are the only required code changes on the frontend.)

## 8) Backend support for FCM

You need a server endpoint to:
- Store device tokens per user
- Send push messages via FCM when a thought is received

Minimal backend tasks:
- Add a new Mongo collection for device tokens
- Create endpoints:
  - POST /api/push/register (token registration)
  - POST /api/push/send (trigger send, or call from service)
- Use Firebase Admin SDK to send push notifications

## 9) Test on a physical device

```bash
cd frontend
npm run build
npx cap sync android
npx cap open android
```

In Android Studio:
- Connect a device
- Click Run
- Verify:
  - Login works
  - WebSockets work while app is open
  - Push notifications appear when app is backgrounded

## 10) Release build

In Android Studio:
- Build > Generate Signed Bundle / APK
- Create a keystore
- Generate AAB for Play Store

## 11) Play Store checklist

- App icons + splash screen
- Privacy policy
- Data safety declaration
- Versioning (increment versionCode / versionName)

---

## Summary of what changes are needed (Option A)

- No UI changes needed: keep the existing Vue app as-is; Capacitor wraps it.
- Minimal code changes to support push notifications:
  - Add push notification permission + token registration in the Vue app
  - Add backend endpoints + FCM sending logic

If you want, I can draft the exact frontend code for Capacitor push token registration and the backend FCM integration steps next.

---

## Concrete checklist (before touching production code)

### A) Android project setup
- [x] Add Capacitor dependencies to `frontend/package.json` and initialize Capacitor.
- [x] Generate `capacitor.config.*` with app name + app ID.
- [x] Build web assets and add Android platform (`frontend/android`).
- [ ] Confirm Android Studio opens and Gradle sync completes.

### B) Push notifications (client)
- [x] Add `@capacitor/push-notifications` and sync Android project.
- [x] Implement runtime notification permission request for Android 13+.
- [x] Register device, obtain FCM token, and send token to backend.
- [x] Handle token refresh / invalidation (via re-registration events).

### C) Push notifications (server)
- [x] Add Mongo collection/model for device tokens (per user + device).
- [x] Add endpoint `POST /api/push/register` to store/update device tokens.
- [x] Trigger push notifications from the “thought sent” flow.
- [x] Add Firebase Admin SDK and credentials management.

### D) Release readiness
- [ ] Define `versionCode` / `versionName` in Android Gradle config.
- [ ] Create signing keystore and configure release signing.
- [ ] Generate signed AAB from Android Studio.
- [ ] Add app icons and splash screen assets.

### E) Play Store requirements
- [ ] Draft and host a Privacy Policy.
- [ ] Prepare Data Safety declaration (based on actual data collected).
- [ ] Complete Play Store listing assets (title, description, screenshots, feature graphic).

### F) Verification
- [ ] Test login, connections, thoughts, stats in Android emulator/device.
- [ ] Verify WebSocket updates work in foreground.
- [ ] Verify push notifications in background/closed state.

---

## What still needs to be done (technical + organizational)

### 1) Accounts and access (organizational)

#### 1.0 Install Android Studio (required before Android steps)
- If you don’t have it yet, install Android Studio before any Android build/sync steps:
  - Download Android Studio from the official site.
  - Keep the default install options (they include the SDK + Build Tools).
  - Launch Android Studio once to complete SDK setup.

#### 1.1 Google Play Console (required to publish)
- Create or use a Google account dedicated to the app.
- Sign up for a **Google Play Console developer account**.
  - Pay the one‑time registration fee.
  - Complete identity/organization verification when prompted.
  - Set up a developer profile (name, email, website).
- Add additional owners/testers to the Play Console (optional but recommended).

#### 1.2 Firebase (required for push notifications)
- Create a Firebase project for the app.
- In Firebase Console, add an **Android app** with package name `de.kaviedes.thinkofyou`.
- Download `google-services.json` and place it here:
  - `frontend/android/app/google-services.json`
- Generate a **Service Account** in Firebase and export the JSON key.
  - Decide how you will store credentials in production:
    - `FIREBASE_SERVICE_ACCOUNT_BASE64` (base64‑encoded JSON)
    - `FIREBASE_SERVICE_ACCOUNT_PATH` (path to JSON on the server)
    - or `GOOGLE_APPLICATION_CREDENTIALS` (standard fallback)
- Store credentials securely in your hosting provider’s secrets/variables.

#### 1.3 Hosting + domain (recommended)
- Ensure the backend + frontend are served over **HTTPS**.
- If using a custom domain, configure SSL and verify the domain.
- Confirm the API base URL strategy:
  - Option A (recommended): backend serves the frontend at same origin.
  - Option B: set `VITE_API_BASE_URL` and update frontend requests.

---

### 2) App build & signing (technical + organizational)

#### 2.1 Android Studio project verification
- Ensure Android Studio is installed first (see 1.0).
- Run:
  ```bash
  cd frontend
  npx cap open android
  ```
- Wait for Gradle sync to complete.
- Fix any missing SDK/Build Tools prompts in Android Studio.

#### 2.2 Signing key (required for Play Store)
- Generate a **keystore** for release signing.
- Store the keystore securely (never commit it).
- Configure signing in `frontend/android/app/build.gradle` for the `release` build type.
- Record:
  - keystore path
  - alias name
  - key password and keystore password
- Decide who owns and safeguards the keystore (you cannot change it later without migration).

#### 2.3 Build artifacts
- Build a **signed AAB** (Android App Bundle) for Play Store upload.
- Also generate a debug APK for internal testing.

---

### 3) Product readiness (organizational + compliance)

#### 3.1 Privacy policy
- Draft a privacy policy that covers:
  - data collected (usernames, tokens, metadata)
  - how data is stored (MongoDB, Firebase)
  - retention and deletion policy
  - contact method for requests
- Host the privacy policy on a public URL.
- Add the link in the Play Console listing.

#### 3.2 Data Safety form (Play Store)
- Fill out the Data Safety questionnaire in Play Console.
- Confirm actual data collection and sharing practices:
  - account data (username)
  - device identifiers / push tokens
  - analytics (if any)
  - encryption in transit (HTTPS)
- Ensure the answers match the actual implementation.

#### 3.3 Store listing assets
- Create:
  - App title and short description
  - Full description
  - App icon (512×512)
  - Feature graphic (1024×500)
  - Screenshots (phone; optional tablet)
- Decide content rating and target audience.
- Provide contact email and website.

---

### 4) Technical validation before release

#### 4.1 Functional checks on device
- Login, connection requests, thoughts, stats.
- WebSocket updates in foreground.
- Push notification delivery in background/closed state.

#### 4.2 Performance and stability
- Cold start time
- Network error handling
- Token refresh handling

#### 4.3 Release checklist
- Increment `versionCode` / `versionName`.
- Confirm correct environment variables for production.
- Upload AAB to the correct Play Console track:
  - Internal testing → Closed testing → Open testing → Production

---

### 5) Optional but recommended (organizational)
- Create a support email address (required by Play Console).
- Set up a basic website or landing page.
- Prepare a simple support/FAQ page.
