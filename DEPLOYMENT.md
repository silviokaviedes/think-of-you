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

*Note: The app is configured to use `8080` by default in the `Dockerfile`. Railway will automatically map its public URL to this internal port.*

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
