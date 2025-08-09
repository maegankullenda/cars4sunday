# Calendar Integration Feature

## Overview

The CarsOnSunday app now includes automatic calendar integration that adds events to the user's local phone calendar when they choose to attend an event. This provides a seamless way for users to receive notifications through their existing calendar app.

## Requirements

### Google Account Setup

- **Google Account Required**: To use calendar features, users must have a Google account added to their device
- **Calendar App**: Works with Google Calendar and other calendar apps that support Android's calendar provider
- **Permissions**: Calendar read/write permissions are required

### Setup Instructions

1. Add a Google account to your device (Settings > Accounts & sync)
2. Grant calendar permissions when prompted
3. Attend events in the app - they'll automatically appear in your calendar

## Features

### 1. Automatic Calendar Addition

- When a user clicks "Attend" on an event, it's automatically added to their default calendar
- Events are added with a 15-minute reminder notification
- All event details (title, description, location, date/time) are preserved

### 2. Deep Link Integration

- Each calendar event includes a custom URL that links back to the event in the app
- URL format: `carsonsunday://event_detail/{eventId}`
- Users can tap the link in their calendar to open the event details in the app

### 3. Smart Calendar Management

- Events are automatically removed from the calendar when users leave an event
- Duplicate prevention - won't add the same event twice
- Calendar status is displayed in the UI for attending users

### 4. Permission and Account Handling

- Graceful handling of calendar permissions
- Google account requirement detection
- Clear guidance for users on setup requirements
- Fallback behavior when requirements aren't met

## Technical Implementation

### Core Components

#### CalendarManager.kt

- Main utility class for calendar operations
- Handles adding/removing events from calendar
- Manages calendar permissions and Google account detection
- Builds deep links for app navigation
- Provides calendar account information

#### EventsViewModel.kt

- Integrates calendar operations with event attendance
- Automatically adds events to calendar when attending
- Removes events from calendar when leaving
- Provides calendar status information to UI
- Handles Google account requirement checks

#### EventsScreen.kt

- Shows calendar status for attending users
- Displays different status messages based on requirements:
  - "📅 Calendar permission needed"
  - "📅 Google account needed"
  - "📅 Added to calendar"
  - "📅 Not in calendar"
- Integrates permission and account setup dialogs

### Permission Management

#### AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
```

#### Deep Link Configuration

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="carsonsunday" />
</intent-filter>
```

### Calendar Event Structure

Each calendar event includes:

- **Title**: Event title
- **Description**: Event description + metadata + deep link
- **Location**: Event location
- **Start Time**: Event date/time
- **End Time**: Event date/time + 2 hours (default duration)
- **Reminder**: 15 minutes before event
- **URL**: Deep link back to app

### Deep Link Handling

#### MainActivity.kt

- Handles incoming deep links from calendar
- Parses event ID from URL
- Navigates to event detail screen

## User Experience

### Setup Flow

1. User attends an event in the app
2. If no Google account is set up, show guidance dialog
3. If no calendar permissions, show permission dialog
4. Once requirements are met, event is added to calendar
5. User receives calendar notification 15 minutes before event
6. User can tap the event in their calendar to open it in the app

### Visual Indicators

- Calendar status is shown for attending users with specific messages:
  - Red text for missing requirements
  - Green text for successful calendar integration
- Permission and account setup dialogs with clear explanations
- Clear feedback when events are added/removed

### Benefits

- **Leverages existing calendar apps**: Users can use their preferred calendar app
- **Reliable notifications**: Uses the phone's built-in notification system
- **Seamless integration**: No need for separate notification setup
- **Cross-platform**: Works with any calendar app that supports Android calendar provider
- **Google Calendar sync**: Events sync across devices when using Google account

## Error Handling

### Permission Denied

- App continues to work without calendar integration
- Users can still attend/leave events
- Permission can be granted later through system settings
- Clear guidance provided to users

### No Google Account

- App detects missing Google account setup
- Provides clear instructions on how to add Google account
- Graceful fallback - app continues to work normally
- Users can add Google account later and calendar features will work

### Calendar Unavailable

- Graceful fallback when calendar is not accessible
- Error logging for debugging
- No impact on core app functionality

### Network Issues

- Calendar operations are local, so network issues don't affect functionality
- Events are still managed in the app regardless of calendar status
- Google Calendar sync happens in background when network is available

## Future Enhancements

### Potential Improvements

1. **Customizable reminders**: Allow users to set different reminder times
2. **Multiple calendars**: Support for choosing which calendar to use
3. **Event duration**: Allow users to set custom event duration
4. **Recurring events**: Support for recurring calendar events
5. **Calendar sync**: Enhanced sync with cloud calendars (Google Calendar, etc.)

### Advanced Features

1. **Smart scheduling**: Suggest optimal event times based on calendar conflicts
2. **Travel time**: Include travel time in calendar events
3. **Location integration**: Add map links to calendar events
4. **Weather integration**: Include weather information in calendar events
5. **Multiple account support**: Support for multiple Google accounts

## Testing

### Manual Testing Checklist

- [ ] Test with Google account set up
- [ ] Test without Google account (should show guidance)
- [ ] Test with calendar permissions granted
- [ ] Test without calendar permissions (should show guidance)
- [ ] Attend an event and verify it appears in calendar
- [ ] Leave an event and verify it's removed from calendar
- [ ] Test deep link from calendar back to app
- [ ] Test permission request dialog
- [ ] Test Google account setup dialog
- [ ] Test behavior when requirements are denied
- [ ] Verify reminder notifications work
- [ ] Test with different calendar apps

### Edge Cases

- [ ] Multiple events at same time
- [ ] Events with special characters in title/description
- [ ] Very long event descriptions
- [ ] Events in different time zones
- [ ] Calendar app not installed
- [ ] Multiple Google accounts on device
- [ ] Google account sync disabled

## Security & Privacy

### Data Protection

- Only event data is shared with calendar
- No personal user data is transmitted
- Calendar permissions are clearly explained
- Users can revoke permissions at any time
- Google account information is only used for calendar operations

### Privacy Considerations

- Calendar events are stored locally on device
- Google Calendar sync is handled by Google's services
- Deep links only contain event IDs, not personal information
- Permission usage is transparent to users
- No tracking of calendar usage patterns

## Troubleshooting

### Common Issues

#### "Google account needed" message

- **Solution**: Add a Google account to your device
- **Steps**: Settings > Accounts & sync > Add account > Google

#### "Calendar permission needed" message

- **Solution**: Grant calendar permissions to the app
- **Steps**: Settings > Apps > CarsOnSunday > Permissions > Calendar

#### Events not appearing in calendar

- **Check**: Ensure Google account is set up and syncing
- **Check**: Verify calendar permissions are granted
- **Check**: Ensure Google Calendar app is installed and working

#### Notifications not working

- **Check**: Verify Google Calendar notifications are enabled
- **Check**: Ensure device notifications are enabled for calendar
- **Check**: Check Google account sync status

## Conclusion

The calendar integration feature provides a robust, user-friendly solution for event notifications that leverages existing calendar infrastructure. It offers reliability, flexibility, and a seamless user experience while maintaining the app's core functionality. The Google account requirement ensures compatibility with the most widely-used calendar system while providing clear guidance to users on setup requirements.
