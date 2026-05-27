# Backlog

## Future Features

### Account recovery codes without stored email

Users can recover an account without requiring a persisted email address. The app generates recovery codes, stores only a hash, and can optionally send the raw code to a user-provided email address without saving that email.

#### Todos

- [x] Add Resend HTTPS API email delivery using environment-based configuration.
- [x] Add recovery-code fields to the user model: hashed recovery code, creation timestamp, and last-rotated timestamp.
- [x] Generate a secure recovery code during registration.
- [x] Store only the recovery-code hash in MongoDB; never store the raw recovery code.
- [x] Extend registration so users can optionally enter an email address used only to send the recovery code.
- [x] Do not persist the recovery email address in the app database.
- [x] Ensure request logging, app logging, and error messages do not print the email address or recovery code.
- [x] Add a Profile action to generate a new recovery code after confirming the current password.
- [x] Let Profile users optionally send the newly generated recovery code to a one-time email address.
- [x] Invalidate the previous recovery code whenever a new one is generated.
- [x] Add a forgot-password flow using username + recovery code + new password + repeated new password.
- [x] On successful recovery reset, update the password hash, invalidate the used recovery code, revoke all active refresh tokens, and generate a new recovery code.
- [x] Add rate limiting or abuse protection for registration recovery emails, Profile recovery emails, and recovery reset attempts.
- [x] Update the login/register UI with clear copy that email is optional, used only for sending the recovery code, and not stored.
- [x] Update the Profile UI with recovery-code generation and optional one-time email sending.
- [x] Update README and privacy policy to explain recovery-code processing and non-persistence of recovery email addresses.
- [x] Add backend tests for registration recovery-code generation, email send behavior, hash-only persistence, Profile rotation, reset success, reset failure, and token revocation.
- [x] Add Playwright coverage for registration recovery-code display, Profile recovery-code regeneration, and forgot-password reset.

### Energy levels sent with emoji

Users can set their current energy levels with three dashboard sliders:

- Body
- Mind
- Heart

When a user sends an emoji, the app also sends a snapshot of the current energy levels. The emoji remains the primary notification and main signal. The receiver can see the sender's energy levels in the dashboard next to or below the latest received emoji.

#### Todos

- [x] Add dashboard sliders for Body, Mind, and Heart energy levels.
- [x] Use a 0-100 integer scale for all three energy values.
- [x] Store the current energy levels per user, with default values of 50/50/50 for existing and new users.
- [x] Add backend `GET` and `PUT` endpoints for the authenticated user's energy-level preferences.
- [x] Keep backend APIs backwards compatible: existing clients that only send `{ "mood": "..." }` to `POST /api/connections/{id}/think` must continue to work.
- [x] Keep existing persisted data backwards compatible: users without stored energy levels and thought events without energy snapshots must load without errors.
- [x] Extend `ThoughtEvent` so each sent thought stores a snapshot of the sender's current Body, Mind, and Heart levels.
- [x] Extend connection dashboard responses with optional last-sent and last-received energy snapshots.
- [x] Extend WebSocket thought messages with optional energy snapshots while preserving the existing `type`, `sender`, `mood`, and `emoji` fields.
- [x] Update the frontend send flow so a sent emoji includes the currently saved energy context.
- [x] Display the partner's latest received energy levels in the dashboard near the emoji, without making energy levels more visually prominent than the emoji.
- [x] Display the user's latest sent energy levels in the dashboard where sent last-event details are shown.
- [x] Add backend tests for defaults, validation, event snapshots, connection DTO mapping, and backwards-compatible request handling.
- [x] Add frontend or Playwright coverage for setting energy levels, sending an emoji, and seeing the received energy levels in the other user's dashboard.
- [x] Manually verify the dashboard layout on desktop and Android WebView so sliders and compact energy labels do not overlap existing content.
