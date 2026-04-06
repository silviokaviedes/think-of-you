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

### Android build modes: local vs Railway

Use one of these two modes, depending on which backend the Android app should call:

#### Mode A: Local Android testing against Docker Compose

Use this when your backend is running locally via `docker compose up --build`.

Commands:

```bash
docker compose up --build
cd frontend
npm install
npm run build:android-local
npm run android:sync
npm run android:open
```

PowerShell shortcut from `frontend`:

```powershell
.\scripts\android-local.ps1
```

Behavior:
- The Android build is configured to call `http://10.0.2.2:8080`
- `10.0.2.2` is the Android emulator alias for the host machine
- This mode is for emulator testing against the local Spring Boot app

#### Mode B: Android testing against Railway

Use this when the Android app should call your deployed Railway backend instead of the local machine.

Setup:
- Copy `frontend/.env.android-railway.example` to `frontend/.env.android-railway.local`
- Set:

```bash
VITE_API_BASE_URL=https://think-of-you-production.up.railway.app
```

Commands:

```bash
cd frontend
npm install
npm run build:android-railway
npm run android:sync
npm run android:open
```

PowerShell shortcut from `frontend`:

```powershell
.\scripts\android-railway.ps1
```

Behavior:
- The Android build uses `VITE_API_BASE_URL`
- No local `docker compose` backend is required for this mode

Important:
- `docker-compose.yml` remains the local backend stack only
- Railway continues to build the web/backend service from the root `Dockerfile`
- No second Docker Compose file is required just to switch Android between local and Railway
- Both PowerShell scripts support `-SkipOpen` if you only want build + sync without opening Android Studio

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
- [x] Confirm Android Studio opens and Gradle sync completes.

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
- [x] Define `versionCode` / `versionName` in Android Gradle config. Android now uses `versionCode 1` and `versionName "1.0.0"` in `frontend/android/app/build.gradle`.
- [x] Create signing keystore and configure release signing. A local release keystore has been created and Gradle signing now works through `frontend/android/keystore.properties` or `ANDROID_KEYSTORE_*` environment variables.
- [x] Generate signed AAB from Android Studio. Verified locally via `./gradlew.bat bundleRelease`, producing `frontend/android/app/build/outputs/bundle/release/app-release.aab`.
- [x] Generate and verify a local debug APK build (`./gradlew.bat assembleDebug` completed successfully).
- [x] Add app icons and splash screen assets. Android launcher icons and splash screens were replaced with branded assets generated from the repo script `frontend/scripts/generate-android-brand-assets.ps1`.
- [x] Automate production handling for `frontend/android/app/google-services.json` via `GOOGLE_SERVICES_JSON_B64` so Railway can recreate it from an environment variable during Android builds.
- [x] Document production handling for `frontend/android/app/google-services.json` via `GOOGLE_SERVICES_JSON_B64` for Railway-style environment-variable-only builds.
- [x] Document separate Android build/start flows for local Docker Compose backend vs Railway backend.
- [x] Set `FIREBASE_SERVICE_ACCOUNT_BASE64` in Railway so the backend can authenticate to Firebase Admin for server-side push delivery.

### E) Play Store requirements
- [ ] Draft and host a Privacy Policy. Draft added in `PRIVACY-POLICY.md`; public hosting and real contact details are still missing.
- [x] Prepare Data Safety declaration (based on actual data collected). Working notes added in `PLAY-STORE-DATA-SAFETY.md`; final Play Console review is still required.
- [ ] Complete Play Store listing assets (title, description, screenshots, feature graphic).
- [x] Add Google Play-compliant account deletion flow and external deletion page/link. The app now includes in-app account deletion from Profile plus `account-deletion.html` for out-of-app guidance.

### F) Verification
- [x] Test core Android app flow on emulator/device. Android app launch, login, dashboard access, sending thoughts, and general manual app testing have been completed successfully.
- [x] Verify connection list/state sync explicitly on Android after the latest WebSocket fix. Manually re-tested and confirmed working.
- [x] Verify the stats view explicitly on Android. Manually re-tested and confirmed working.
- [x] Verify WebSocket updates work in foreground. Manually re-tested and confirmed the dashboard refreshes live while open after switching SockJS to the backend `http://.../ws` endpoint.
- [x] Verify push notifications in background/closed state.

---

## Resume Here

Current status after local emulator testing:
- Android Studio project opens and Gradle sync completes.
- Android app launches in the emulator.
- Login from the Android emulator works against the local backend.
- Android now has two documented build modes: local emulator-to-host backend and Railway-backed build via `VITE_API_BASE_URL`.
- `google-services.json` has been added and the app no longer crashes during startup.
- Dashboard opens successfully after login.
- Sending a thought from the Android app works.
- A local Android debug build (`assembleDebug`) completes successfully and produces an APK.
- The app has been manually through-tested at a general level after the latest Android changes.
- Background push notifications now work against the Railway deployment after setting `FIREBASE_SERVICE_ACCOUNT_BASE64` and switching the backend send path to `sendEachForMulticast(...)`.
- Root cause for missing foreground live updates has been identified: the Android WebView rejected the generated `ws://...` SockJS URL. The frontend was updated to use the backend `http://.../ws` SockJS endpoint instead, and the Android project was rebuilt/synced.
- Foreground WebSocket updates have now been manually re-tested and confirmed working on Android with the current build.
- Connection list/state sync has now been manually re-tested and confirmed working on Android after the WebSocket fix.
- The stats view has now been manually re-tested and confirmed working on Android.
- Additional frontend debug logging remains in place around the WebSocket lifecycle by explicit team decision. It is no longer treated as a pending cleanup task.
- Android release metadata is now defined with `versionCode 1` and `versionName "1.0.0"`.
- Android release-signing wiring is now prepared in Gradle for either `frontend/android/keystore.properties` or `ANDROID_KEYSTORE_*` environment variables.
- The updated Android Gradle configuration has been re-verified with a successful `./gradlew.bat assembleDebug` build.
- A local Android release keystore has been created and wired into the Android build.
- A signed Android App Bundle build now completes successfully and produces `frontend/android/app/build/outputs/bundle/release/app-release.aab`.
- Android launcher icons and splash screens have been refreshed with branded release assets.
- Account deletion is now implemented in-app and documented through the public `account-deletion.html` page.
- Backend unit tests pass, and the full Playwright suite passes against the local Docker Compose stack when Playwright targets `http://127.0.0.1:8080`.

Next exact steps:
1. Host the privacy policy and account deletion page at public URLs and replace placeholder contact details.
2. Complete the remaining Play Store listing assets and submission metadata.

If Android behavior regresses again, use Android Studio Logcat first and check for Capacitor, Chromium, Firebase, and WebSocket-related errors before changing code.

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
- Status: added locally. Firebase-based app startup no longer crashes, the Android client receives an FCM token, and background/closed-state push delivery still needs verification.
- Status: added locally. Firebase-based app startup no longer crashes, the Android client receives an FCM token, and background/closed-state push delivery has been verified successfully against Railway.
- For Railway or other environments that only allow environment variables:
  - Base64-encode `google-services.json`
  - Store it as `GOOGLE_SERVICES_JSON_B64`
  - Recreate `frontend/android/app/google-services.json` from that variable before the Android build starts
- Generate a **Service Account** in Firebase and export the JSON key.
  - Decide how you will store credentials in production:
    - `FIREBASE_SERVICE_ACCOUNT_BASE64` (base64‑encoded JSON)
    - `FIREBASE_SERVICE_ACCOUNT_PATH` (path to JSON on the server)
    - or `GOOGLE_APPLICATION_CREDENTIALS` (standard fallback)
- Store credentials securely in your hosting provider’s secrets/variables.
- Status: `FIREBASE_SERVICE_ACCOUNT_BASE64` is set in Railway and confirmed working for FCM delivery.

#### 1.3 Hosting + domain (recommended)
- Ensure the backend + frontend are served over **HTTPS**.
- If using a custom domain, configure SSL and verify the domain.
- Confirm the API base URL strategy:
  - Option A: Android local testing uses `npm run build:android-local` and talks to `http://10.0.2.2:8080`
  - Option B: Railway-backed Android builds use `npm run build:android-railway` and `VITE_API_BASE_URL`

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
- Status: done locally. `frontend/android/app/google-services.json` has already been added locally and the project has been resynced after the latest frontend/WebSocket fixes.

#### 2.2 Signing key (required for Play Store)
- Generate a **keystore** for release signing.
- Store the keystore securely (never commit it).
- Configure signing in `frontend/android/app/build.gradle` for the `release` build type.
- Status: completed locally. Gradle release-signing wiring is already in place. Provide either:
  - `frontend/android/keystore.properties` based on `frontend/android/keystore.properties.example`, or
  - environment variables `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`
  before generating a signed release build in another environment.
- Record:
  - keystore path
  - alias name
  - key password and keystore password
- Decide who owns and safeguards the keystore (you cannot change it later without migration).

#### 2.3 Build artifacts
- Build a **signed AAB** (Android App Bundle) for Play Store upload.
- Also generate a debug APK for internal testing.
- Status: local debug APK build completed successfully via `./gradlew.bat assembleDebug`, and a signed release AAB build completed successfully via `./gradlew.bat bundleRelease`.

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
- Status: draft created in `PRIVACY-POLICY.md`. Before publishing, replace placeholder contact information and host it on a public, non-editable URL.

#### 3.2 Data Safety form (Play Store)
- Fill out the Data Safety questionnaire in Play Console.
- Confirm actual data collection and sharing practices:
  - account data (username)
  - device identifiers / push tokens
  - analytics (if any)
  - encryption in transit (HTTPS)
- Ensure the answers match the actual implementation.
- Status: working notes created in `PLAY-STORE-DATA-SAFETY.md` based on the current codebase. Final answers still need Play Console review.

#### 3.2.1 Account deletion requirement
- Google Play requires apps with in-app account creation to also provide account deletion.
- Add a readily discoverable in-app account deletion path.
- Add an external web page or support flow that users can access outside the app.
- Ensure deletion behavior and retention handling match the published privacy policy.
- Status: completed in the codebase. In-app deletion is available from Profile, and `src/main/resources/static/account-deletion.html` provides the external page. Public hosting and final contact details are still pending.

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
