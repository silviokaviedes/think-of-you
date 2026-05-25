# Deployment Guide: The Path of Least Resistance

This guide provides the easiest way to take the "Thinking of You" application from your local machine to production using **Railway** and **MongoDB Atlas**.

## 1. Setup the Database (MongoDB Atlas)

Running MongoDB in a container is great for development, but for production, a managed service is more reliable and easier to maintain.

1.  Sign up for a free account at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas).
2.  **Create a Cluster**: Choose the "M0" (Free) tier.
3.  **Network Access**: In the Atlas dashboard, go to "Network Access" and click "Add IP Address". Select **"Allow Access From Anywhere"** (0.0.0.0/0) for initial setup, or better yet, restrict it later if your provider allows static IPs.
4.  **Database Access**: Create a database user with a username and password. Keep these handy.
5.  **Get Connection String**:
    *   Click "Connect" on your cluster.
    *   Choose "Drivers" and select "Java".
    *   Copy the connection string (it looks like `mongodb+srv://<username>:<password>@cluster0.xxxx.mongodb.net/?retryWrites=true&w=majority`).
    *   **Replace `<password>`** with the actual password you created.

## 2. Deploy the Application (Railway)

Railway is a Platform-as-a-Service (PaaS) that can build your app directly from your `Dockerfile`.

1.  Push your code to a **GitHub repository**.
2.  Log in to [Railway.app](https://railway.app/) using your GitHub account.
3.  Click **"New Project"** -> **"Deploy from GitHub repo"** and select your repository.
4.  **Important**: Do not deploy yet. We need to add environment variables.

### Configure Environment Variables

In Railway, go to the **Variables** tab for your service and add:

| Variable Name | Value |
| :--- | :--- |
| `SPRING_DATA_MONGODB_URI` | *Your MongoDB Atlas connection string from Step 1* |
| `PORT` | `8080` (Railway usually detects this, but explicit is better) |
| `FIREBASE_SERVICE_ACCOUNT_BASE64` | *Base64-encoded Firebase service account JSON for server-side FCM push delivery* |

*Note: The app is configured to use `8080` by default in the `Dockerfile`. Railway will automatically map its public URL to this internal port.*

For this project's Railway deployment, `FIREBASE_SERVICE_ACCOUNT_BASE64` has been confirmed as the working configuration for server-side Firebase push notifications.

### Recovery Email Sending on Railway

The app sends recovery-code emails through the **Resend HTTPS API**, not SMTP. This works on Railway Free/Hobby because it uses normal outbound HTTPS instead of blocked SMTP ports.

To make recovery email sending work on Railway:

1. Create a Resend account at `https://resend.com`.
2. In Resend, verify the sender domain or sender address you want to use.
3. In Resend, create an API key.
4. In Railway, open the project.
5. Select the deployed app service, not the MongoDB service.
6. Open the **Variables** tab.
7. Add these variables:

| Variable Name | Value |
| :--- | :--- |
| `RECOVERY_MAIL_ENABLED` | `true` |
| `RECOVERY_MAIL_FROM` | A verified Resend sender, for example `Thinking of You <noreply@your-domain.com>` or `noreply@your-domain.com` |
| `RESEND_API_KEY` | The Resend API key |

Optional:

| Variable Name | Value |
| :--- | :--- |
| `RESEND_API_URL` | Leave unset unless you need to override the default `https://api.resend.com/emails` |

8. Seal `RESEND_API_KEY` in Railway:
   - In the Variables tab, find `RESEND_API_KEY`.
   - Open its menu.
   - Choose **Seal** so the value cannot be viewed later in the Railway UI.
9. Redeploy the app service:
   - Open the service **Deployments** tab.
   - Trigger a redeploy, or push a new commit.
   - Railway applies changed variables only after a new deployment.
10. Verify the flow:
   - Open the deployed app.
   - Register a test account.
   - After pressing **Register**, enter a test email in the recovery-code delivery step.
   - Press **Create account**.
   - Confirm the recovery-code email arrives.
   - If it fails, check the Railway deployment logs for `Failed to send recovery email`.

Important notes:
- The app does not store the recovery email address in MongoDB. It only passes the address to Resend for that one email.
- The app intentionally does not log the submitted recovery email address or the recovery code.
- Resend requires the `RECOVERY_MAIL_FROM` sender to be verified in Resend before production sending works.

### Firebase / Android Build Note

For Android builds, do not commit `frontend/android/app/google-services.json` to git.

If your deployment/build environment only supports environment variables, store the file contents as a base64-encoded variable, for example `GOOGLE_SERVICES_JSON_B64`, and recreate the file before the Android Gradle build starts.

PowerShell example:

```powershell
[IO.File]::WriteAllBytes(
  "frontend/android/app/google-services.json",
  [Convert]::FromBase64String($env:GOOGLE_SERVICES_JSON_B64)
)
```

Bash example:

```bash
echo "$GOOGLE_SERVICES_JSON_B64" | base64 -d > frontend/android/app/google-services.json
```

Then build normally:

```bash
cd frontend/android
./gradlew assembleRelease
```

Important:
- `google-services.json` must exist before the Android build starts.
- Keep the file out of git and provide it through Railway variables or another secret store.
- The built APK/AAB contains the needed Firebase app configuration; the JSON file itself is primarily a build-time input.

## 3. Custom Domains

Railway allows you to use your own domain (e.g., `www.yourdomain.com`) instead of the default `.up.railway.app` URL.

1.  In your Railway project, select your service.
2.  Go to the **Settings** tab.
3.  Scroll down to the **Networking** section and find **Custom Domains**.
4.  Click **"Add Domain"** and enter your domain name.
5.  Railway will provide you with a **DNS record** (usually a `CNAME` pointing to Railway's proxy).
6.  Log in to your domain registrar (e.g., Namecheap, GoDaddy, Google Domains).
7.  Add the DNS record provided by Railway.
8.  Wait for DNS propagation (can take from a few minutes to 24 hours). Railway will automatically provision an SSL certificate once the domain is verified.

## 4. Verify Deployment

1.  Railway will start a build process using your `Dockerfile`. You can watch the logs in the "Deployments" tab.
2.  Once finished, Railway provides a public URL (e.g., `https://think-of-you-production.up.railway.app`).
3.  Open the URL and try to register an account to ensure the database connection is working.

## Troubleshooting

*   **WebSocket Connection Issues**: Railway supports WebSockets natively. If you use a custom domain later, ensure your SSL/TLS settings don't block `wss://` traffic.
*   **Startup Failures**: Check the "Deployments" logs in Railway. Ensure `SPRING_DATA_MONGODB_URI` is correct and Atlas allows the connection.
*   **Recovery emails do not send**: Confirm `RECOVERY_MAIL_ENABLED=true`, `RECOVERY_MAIL_FROM` is verified in Resend, `RESEND_API_KEY` is set on the app service, and the app was redeployed after the variables changed. Check both Railway deployment logs and Resend email logs.
