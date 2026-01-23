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
- Web directory: dist

## 2) Build the web app

```bash
npm run build
```

This outputs frontend/dist which Capacitor will package into the Android app.

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

Capacitor will load the compiled dist locally by default.

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
