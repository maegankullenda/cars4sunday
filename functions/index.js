const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Trigger when a new event is created
exports.onEventCreated = functions.firestore
    .document('events/{eventId}')
    .onCreate(async (snap, context) => {
        const eventData = snap.data();
        const eventId = context.params.eventId;
        
        console.log('New event created:', eventId, eventData.title);
        
        // Get all users with FCM tokens and notifications enabled
        const usersSnapshot = await admin.firestore()
            .collection('users')
            .where('fcmToken', '!=', null)
            .where('notificationsEnabled', '==', true)
            .get();
        
        const tokens = [];
        usersSnapshot.forEach(doc => {
            const userData = doc.data();
            if (userData.fcmToken) {
                tokens.push(userData.fcmToken);
            }
        });
        
        if (tokens.length === 0) {
            console.log('No users with FCM tokens found');
            return;
        }
        
        console.log(`Sending notification to ${tokens.length} users`);
        
        const message = {
            data: {
                eventId: eventId,
                type: 'new_event',
                title: 'New Event Available!',
                body: `${eventData.title} - ${formatDate(eventData.date)}`
            },
            tokens: tokens
        };
        
        try {
            const response = await admin.messaging().sendMulticast(message);
            console.log('Successfully sent new event notification:', response.successCount, 'successful,', response.failureCount, 'failed');
            
            // Clean up invalid tokens
            if (response.failureCount > 0) {
                await cleanupInvalidTokens(response.responses, tokens);
            }
        } catch (error) {
            console.error('Error sending new event notification:', error);
        }
    });

// Trigger when an event is updated
exports.onEventUpdated = functions.firestore
    .document('events/{eventId}')
    .onUpdate(async (change, context) => {
        const beforeData = change.before.data();
        const afterData = change.after.data();
        const eventId = context.params.eventId;
        
        console.log('Event updated:', eventId, afterData.title);
        
        // Only send notification for significant changes
        const significantFields = ['title', 'date', 'location', 'description'];
        const hasSignificantChange = significantFields.some(field => 
            beforeData[field] !== afterData[field]
        );
        
        if (!hasSignificantChange) {
            console.log('No significant changes detected, skipping notification');
            return;
        }
        
        // Get attendees of this event
        const attendeeIds = afterData.attendees || [];
        
        if (attendeeIds.length === 0) {
            console.log('No attendees for this event, skipping notification');
            return;
        }
        
        console.log(`Event has ${attendeeIds.length} attendees`);
        
        // Get FCM tokens for attendees in batches (Firestore 'in' query limit is 10)
        const tokens = [];
        const batchSize = 10;
        
        for (let i = 0; i < attendeeIds.length; i += batchSize) {
            const batch = attendeeIds.slice(i, i + batchSize);
            
            const usersSnapshot = await admin.firestore()
                .collection('users')
                .where('id', 'in', batch)
                .where('fcmToken', '!=', null)
                .where('notificationsEnabled', '==', true)
                .get();
            
            usersSnapshot.forEach(doc => {
                const userData = doc.data();
                if (userData.fcmToken) {
                    tokens.push(userData.fcmToken);
                }
            });
        }
        
        if (tokens.length === 0) {
            console.log('No attendees with FCM tokens found');
            return;
        }
        
        console.log(`Sending update notification to ${tokens.length} attendees`);
        
        // Determine what changed for the notification message
        let changeDescription = 'has been updated';
        if (beforeData.title !== afterData.title) {
            changeDescription = 'title has been updated';
        } else if (beforeData.date !== afterData.date) {
            changeDescription = 'date has been changed';
        } else if (beforeData.location !== afterData.location) {
            changeDescription = 'location has been changed';
        } else if (beforeData.description !== afterData.description) {
            changeDescription = 'details have been updated';
        }
        
        const message = {
            data: {
                eventId: eventId,
                type: 'event_updated',
                title: 'Event Updated',
                body: `${afterData.title} ${changeDescription}`
            },
            tokens: tokens
        };
        
        try {
            const response = await admin.messaging().sendMulticast(message);
            console.log('Successfully sent event update notification:', response.successCount, 'successful,', response.failureCount, 'failed');
            
            // Clean up invalid tokens
            if (response.failureCount > 0) {
                await cleanupInvalidTokens(response.responses, tokens);
            }
        } catch (error) {
            console.error('Error sending event update notification:', error);
        }
    });

// Helper function to clean up invalid FCM tokens
async function cleanupInvalidTokens(responses, tokens) {
    const invalidTokens = [];
    
    responses.forEach((response, index) => {
        if (!response.success) {
            const error = response.error;
            if (error.code === 'messaging/invalid-registration-token' ||
                error.code === 'messaging/registration-token-not-registered') {
                invalidTokens.push(tokens[index]);
            }
        }
    });
    
    if (invalidTokens.length > 0) {
        console.log(`Cleaning up ${invalidTokens.length} invalid tokens`);
        
        // Remove invalid tokens from users
        const batch = admin.firestore().batch();
        
        for (const token of invalidTokens) {
            const usersSnapshot = await admin.firestore()
                .collection('users')
                .where('fcmToken', '==', token)
                .get();
            
            usersSnapshot.forEach(doc => {
                batch.update(doc.ref, { fcmToken: null });
            });
        }
        
        await batch.commit();
        console.log('Invalid tokens cleaned up');
    }
}

// Helper function to format date
function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return dateString;
    }
}
