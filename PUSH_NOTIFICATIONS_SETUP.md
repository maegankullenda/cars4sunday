# Push Notifications Setup Guide

This guide explains how to set up and deploy the Firebase Cloud Messaging (FCM) push notification system for the CarsOnSunday app.

## Overview

The push notification system uses Firebase Cloud Messaging (FCM) with Firebase Cloud Functions to automatically send notifications when:

1. **New Event Created**: All users with notifications enabled receive a notification
2. **Event Updated**: Only attendees of the specific event receive a notification when significant changes are made

## Architecture

```
Event Created/Updated → Cloud Function Trigger → FCM → User Devices
```

## Components

### Android App Components

1. **CarsOnSundayMessagingService**: Handles incoming FCM messages
2. **PushNotificationManager**: Creates and displays notifications
3. **MainActivity**: Handles FCM token registration and notification permissions
4. **User Model**: Extended with `fcmToken` and `notificationsEnabled` fields

### Firebase Components

1. **Cloud Functions**: Trigger notifications on Firestore changes
2. **Firestore**: Stores user FCM tokens and notification preferences
3. **FCM**: Delivers notifications to devices

## Setup Instructions

### 1. Deploy Firebase Cloud Functions

```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firebase in your project directory (if not already done)
firebase init

# Navigate to functions directory and install dependencies
cd functions
npm install

# Deploy the functions
firebase deploy --only functions
```

### 2. Configure Firestore Security Rules

The current rules allow all read/write access for development. For production, update `firestore.rules`:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Events are readable by all authenticated users
    match /events/{eventId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        (request.auth.uid == resource.data.createdBy ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN');
    }
  }
}
```

### 3. Create Firestore Indexes

Deploy the composite indexes for efficient queries:

```bash
firebase deploy --only firestore:indexes
```

### 4. Test the Implementation

#### Test New Event Notifications

1. Create a new event in the app
2. Check Firebase Functions logs: `firebase functions:log`
3. Verify notification appears on all user devices

#### Test Event Update Notifications

1. Update an existing event (title, date, location, or description)
2. Check that only attendees receive notifications
3. Verify notification content reflects the changes

## Notification Types

### New Event Notification

- **Trigger**: When a new event document is created in Firestore
- **Recipients**: All users with `fcmToken` and `notificationsEnabled: true`
- **Content**: "New Event Available! [Event Title] - [Date]"

### Event Update Notification

- **Trigger**: When an event document is updated with significant changes
- **Recipients**: Only attendees of the specific event
- **Significant Changes**: title, date, location, description
- **Content**: "[Event Title] [change description]"

## Troubleshooting

### Common Issues

1. **No notifications received**

   - Check if notification permissions are granted
   - Verify FCM token is stored in Firestore
   - Check Firebase Functions logs for errors

2. **Functions not triggering**

   - Ensure functions are deployed: `firebase deploy --only functions`
   - Check Firestore security rules allow writes
   - Verify event data structure matches expected format

3. **Invalid FCM tokens**
   - The system automatically cleans up invalid tokens
   - Users need to restart the app to get new tokens

### Debugging Commands

```bash
# View function logs
firebase functions:log

# Test functions locally
firebase emulators:start --only functions,firestore

# Check function deployment status
firebase functions:list
```

## Security Considerations

### Production Checklist

- [ ] Update Firestore security rules for authentication
- [ ] Implement proper user authentication with Firebase Auth
- [ ] Hash user passwords before storing
- [ ] Set up monitoring and alerting for function failures
- [ ] Implement rate limiting for notification sending
- [ ] Add user preference controls for notification types

### Token Management

- FCM tokens are automatically refreshed by the SDK
- Invalid tokens are cleaned up by Cloud Functions
- Tokens are removed when users uninstall the app

## Monitoring

### Key Metrics to Monitor

1. **Function Execution**: Success/failure rates
2. **Notification Delivery**: FCM delivery reports
3. **Token Validity**: Invalid token cleanup frequency
4. **User Engagement**: Notification open rates

### Firebase Console Monitoring

1. Go to Firebase Console → Functions
2. Monitor execution count and error rate
3. Check Cloud Messaging → Reports for delivery stats

## Cost Considerations

### Firebase Functions Pricing

- Free tier: 2M invocations/month
- Paid tier: $0.40 per million invocations

### FCM Pricing

- FCM is free for unlimited messages
- Cloud Functions usage applies for triggers

### Optimization Tips

1. Use batch operations for multiple token updates
2. Implement intelligent notification grouping
3. Add user preferences to reduce unnecessary notifications
4. Monitor and clean up inactive tokens regularly

## Future Enhancements

1. **Rich Notifications**: Add images and action buttons
2. **Notification Scheduling**: Allow delayed notifications
3. **User Preferences**: Granular notification controls
4. **Analytics**: Track notification engagement
5. **Multi-language**: Localized notification content
6. **Push to Web**: Add web push notifications support

## Support

For issues or questions:

1. Check Firebase Console logs
2. Review function execution history
3. Test with Firebase emulators locally
4. Consult Firebase documentation for FCM and Cloud Functions
