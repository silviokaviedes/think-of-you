# Backlog

## Future Features

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
- [ ] Manually verify the dashboard layout on desktop and Android WebView so sliders and compact energy labels do not overlap existing content.
