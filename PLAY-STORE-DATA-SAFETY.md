# Google Play Data Safety Working Notes

Last updated: April 6, 2026

This file is a working checklist for completing the Google Play Data Safety form based on the current app implementation. It is not a legal opinion and should be reviewed before submission in Play Console.

## Important Review Notes

- Google Play requires the Data Safety form for published apps.
- Google Play requires a public privacy policy URL.
- Because this app lets users create an account, Google Play also requires an account deletion path in the app and outside the app.
- Play Console answers must cover the full shipped app, including any third-party SDK behavior.

## Current Implementation Summary

The current codebase indicates:

- No advertising SDK
- No third-party analytics SDK
- No sale of user data
- Push notifications via Firebase Cloud Messaging
- Account creation and login with username and password
- User interaction history and statistics stored server-side
- Device push tokens stored server-side for notification delivery

## Likely Data Safety Declarations

These entries are the most likely Play Console answers based on the current implementation.

### Data collected

Likely yes:

- Personal info: username
- App activity: in-app interactions such as sent/received thoughts, moods, connection history, and event/statistics history
- App info and performance: none identified in the current implementation
- Device or other identifiers: push notification token

### Data shared

Likely "No" for general third-party sharing in the Play Data Safety form, because Firebase Cloud Messaging appears to act as a service provider for push delivery rather than a separate sharing use case. This should still be re-checked in Play Console against your final release configuration and Google Play's latest definitions.

### Collection purpose

Likely purposes:

- App functionality
- Account management
- Fraud prevention / security / abuse prevention only to the extent session and auth controls are used

Not indicated by the current implementation:

- Advertising or marketing
- Personalization for ads
- Analytics

### Is data encrypted in transit?

Likely yes for production deployments, assuming the production app is served over HTTPS as intended by the project deployment guidance.

### Can users request deletion of their data?

Yes, in the current implementation:

- The app exposes a self-service account deletion flow in the Profile screen.
- A public `account-deletion.html` page exists for out-of-app deletion guidance.

Remaining publication work:

- Replace placeholder support/contact information on the public page
- Ensure the hosted privacy policy and external deletion page URLs are the exact ones submitted in Play Console

## Draft Console Worksheet

Use this as a pre-fill worksheet, then verify each answer in Play Console:

- Collects personal info: Yes, username
- Collects app activity: Yes, user interactions and history
- Collects device identifiers: Yes, push token
- Shares data with third parties: Review carefully; likely no reportable "sharing" beyond service providers
- Data encrypted in transit: Yes in production
- Users can request data deletion: Yes, via in-app deletion flow and external deletion instructions

## Before Submission

Confirm all of the following:

- Privacy policy is hosted on a public, non-editable URL
- The policy includes the real developer identity and privacy contact
- The policy matches the shipped app behavior
- Firebase behavior is reflected correctly
- Account deletion flow exists both in-app and outside the app
- Placeholder contact/support details have been replaced with real public information
- Play Console answers are reviewed against the latest Google guidance at submission time
