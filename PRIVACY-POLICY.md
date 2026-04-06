# Privacy Policy for Thinking of You

Last updated: April 6, 2026

This Privacy Policy describes how the Thinking of You app handles information when you use the web app or Android app.

This draft is based on the current implementation in this repository. Before publishing to Google Play, replace the contact placeholders below with your real public contact details and host this policy at a public, non-editable URL.

## Developer and Contact

Developer/app name: Thinking of You

Privacy contact: REPLACE_WITH_PUBLIC_CONTACT_EMAIL

Website or support page: REPLACE_WITH_PUBLIC_SUPPORT_URL

## What the App Does

Thinking of You lets users create accounts, connect with other users, send "thinking of you" interactions, optionally attach moods, view connection statistics, and receive real-time and push notifications.

## Data We Collect

We collect the following categories of data to operate the app:

### 1. Account data

- Username
- Password, stored only as a hashed password on the server
- Account creation timestamp

### 2. Session and authentication data

- Access tokens used to authenticate API requests
- Refresh tokens used to restore sessions
- On the server, refresh tokens are stored in hashed form together with creation, last-use, expiry, revocation, and rotation metadata

### 3. Relationship and interaction data

- Connection requests and connection status between users
- Thought counts between connected users
- Individual thought events, including sender, recipient, timestamp, connection reference, and selected mood
- Event log and statistics derived from these interactions

### 4. User preference data

- Favorite mood buttons
- Dashboard display preference

### 5. Device and notification data

- Push notification token for supported devices
- Platform label such as Android
- Token registration timestamps such as created, updated, and last seen

## How We Use Data

We use this data only to provide and operate the app, including:

- Creating and securing user accounts
- Authenticating sessions and rotating refresh tokens
- Letting users find each other and manage connections
- Delivering thoughts, moods, counts, and statistics
- Sending real-time updates through WebSockets while the app is in use
- Sending push notifications through Firebase Cloud Messaging when configured
- Storing user preferences
- Maintaining service integrity and removing invalid push tokens

We do not use the app for advertising profiling, data brokerage, or in-app analytics based on the current implementation in this repository.

## Where Data Is Stored

Based on the current implementation:

- Application data is stored in MongoDB
- Push notifications are sent through Firebase Cloud Messaging
- Authentication and app logic run on the application server

Deployment-specific hosting providers may vary. For example, this project documentation references Railway and MongoDB Atlas, but the same code may also be self-hosted.

## Data Sharing

We do not sell user data.

We share data only when necessary to operate the service:

- Push notification tokens and notification payload delivery are processed through Firebase Cloud Messaging
- Data may be processed by your selected infrastructure providers, such as hosting and database providers, acting on behalf of the app operator

As of the current implementation, the app does not include advertising SDKs or third-party analytics SDKs.

## Security

Based on the current implementation:

- Passwords are stored as hashes, not plaintext
- Authenticated API access uses bearer tokens
- Refresh tokens are stored hashed on the server and rotated on refresh
- Refresh tokens are revoked on logout and after password changes
- Production deployments are intended to use HTTPS

No method of transmission or storage is perfectly secure, but reasonable technical measures are used to protect the data handled by the app.

## Data Retention

The current implementation retains data as follows:

- User accounts remain until they are deleted by the operator or through the in-app account deletion flow
- Connection and thought-history data remain associated with the account unless removed by the operator or through account deletion
- Refresh tokens expire after 60 days unless revoked earlier
- Revoked and expired refresh-token records may remain in the database until cleaned up by maintenance processes
- Push tokens remain until replaced, removed as invalid, or deleted by the operator

If you publish this app, you should review and finalize your production retention policy so that it matches your real operational practice.

## Your Choices

Users can currently:

- Log out
- Change their password
- Delete their account from the Profile screen by confirming their password and username
- Disconnect from other users
- Update mood and dashboard preferences

An external account-deletion information page should be published alongside the app so users can access deletion instructions outside the app as well.

## Children

The app is not designed for children.

## International Use

Your data may be processed in countries where your hosting, database, or push notification providers operate.

## Changes to This Policy

This policy may be updated when the app or its data practices change. Update the "Last updated" date when you publish a revised version.
