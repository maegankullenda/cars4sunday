# Firebase Setup Guide for CarsOnSunday

This guide will help you set up Firebase Firestore for centralized data storage in your CarsOnSunday app.

## Prerequisites

- Google account
- Android Studio
- Basic understanding of Firebase services

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter a project name (e.g., "CarsOnSunday")
4. Choose whether to enable Google Analytics (recommended)
5. Click "Create project"

## Step 2: Add Android App to Firebase Project

1. In your Firebase project console, click the Android icon (</>) to add an Android app
2. Enter your app's package name: `com.maegankullenda.carsonsunday`
3. Enter app nickname: "CarsOnSunday"
4. Click "Register app"
5. Download the `google-services.json` file
6. Place the `google-services.json` file in your project's `app/` directory

## Step 3: Enable Firestore Database

1. In the Firebase console, go to "Firestore Database" in the left sidebar
2. Click "Create database"
3. Choose "Start in test mode" for development (you can set up security rules later)
4. Select a location for your database (choose the closest to your users)
5. Click "Done"

## Step 4: Configure Security Rules (Optional for Development)

For development, you can use test mode. For production, you'll want to set up proper security rules:

1. In Firestore Database, go to the "Rules" tab
2. Replace the default rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write access to all users under any document
    // WARNING: This is for development only!
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**Note:** These rules allow full access. For production, implement proper authentication and authorization.

## Step 5: Test the Setup

1. Build and run your app
2. Go to Settings in the app
3. Toggle "Use Firebase (Remote)" to enable remote storage
4. Create a test user or event
5. Check the Firebase console to see if data appears in Firestore

## Step 6: Verify Data Structure

Your Firestore database will have the following collections:

- `users` - User accounts and profiles
- `events` - Event information and attendance
- `notices` - Notice board messages

## Troubleshooting

### Common Issues:

1. **"google-services.json not found"**

   - Make sure the file is in the `app/` directory
   - Clean and rebuild your project

2. **"Permission denied"**

   - Check your Firestore security rules
   - Ensure you're in test mode for development

3. **"Network error"**

   - Check your internet connection
   - Verify Firebase project settings

4. **"Authentication failed"**
   - Check your Firebase project configuration
   - Verify the package name matches

## Production Considerations

Before deploying to Google Play Store:

1. **Security Rules**: Implement proper Firestore security rules
2. **Authentication**: Consider implementing Firebase Auth for user management
3. **Backup**: Set up regular database backups
4. **Monitoring**: Enable Firebase Crashlytics and Analytics
5. **Costs**: Monitor Firestore usage and costs

## Testing Multiple Devices

To test with multiple devices:

1. Install the app on multiple devices/emulators
2. Enable Firebase storage in Settings
3. Create data on one device
4. Verify it appears on other devices
5. Test real-time updates

## Local vs Remote Storage

The app supports both storage modes:

- **Local Storage**: Data stored on device only (default)
- **Firebase Storage**: Data stored in cloud, shared across devices

You can switch between modes in the Settings screen.

## Next Steps

1. Implement proper user authentication with Firebase Auth
2. Add offline support with Firestore offline persistence
3. Implement push notifications for event updates
4. Add data validation and error handling
5. Set up automated testing for Firebase operations

## Support

If you encounter issues:

1. Check the [Firebase documentation](https://firebase.google.com/docs)
2. Review the [Firebase Android setup guide](https://firebase.google.com/docs/android/setup)
3. Check the [Firestore documentation](https://firebase.google.com/docs/firestore)
