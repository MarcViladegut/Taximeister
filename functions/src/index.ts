import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

//const functions = require('firebase-functions');
//const admin = require('firebase-admin');
admin.initializeApp();

/*exports.addWelcomeMessages = functions.auth.user().onCreate(user => {
    console.log('A new user signed in for the first time')
    const fullName = user.displayName || 'Anonymus';

    return admin.database().ref('messages').push({
        name: 'Firebase bot',
        text: `${fullName} signed in for the first time! Welcome!`,
    }).then(() => {
        console.log('Welcome message written to database.');
        return null;
    })
});*/

exports.sendNotifications = functions.database.ref("/users/{userUid}").onCreate(event => {
    const text = `${event.val().name} signed in for the first time! Welcome!`;
    console.log(`El valor de la variable text es ${text}`);
    const payload = {
        notification: {
            title: `${event.val().name}: Welcome to the Taximeister`,
            body: text ? text.length <= 100 ? text : text.substring(0, 97) + "..." : ""
        }
    };
    console.log(`El valor de la variable token es ${event.val().token}`);
    return admin.messaging().sendToDevice(event.val().token, payload);
});