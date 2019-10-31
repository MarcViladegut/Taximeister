package eps.udl.cat.meistertaxi.FirebaseCloudMessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import eps.udl.cat.meistertaxi.Models.Client;
import eps.udl.cat.meistertaxi.Models.Driver;
import eps.udl.cat.meistertaxi.Main.MainActivity;
import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.Models.User;

import static eps.udl.cat.meistertaxi.Constants.USERS_REFERENCE;

public class MyService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0)
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        sendNotification(remoteMessage.getNotification().getBody());
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_icon_app)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public void sendRegistrationToServer(String token) {
        saveOnDatabase(token);
    }

    public void saveOnDatabase(final String token){
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser userLogin = mAuth.getCurrentUser();
        DatabaseReference usersRef = database.getReference(USERS_REFERENCE).child(userLogin.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> childUpdates = new HashMap<>();
                User userRead = dataSnapshot.getValue(User.class);
                if (userRead.isDriver()){
                    Driver driver;
                    driver = dataSnapshot.getValue(Driver.class);
                    driver.setToken(token);
                    childUpdates.put(mAuth.getUid(), driver.toMap());
                } else {
                    Client client;
                    client = dataSnapshot.getValue(Client.class);
                    client.setToken(token);
                    childUpdates.put(mAuth.getUid(), client.toMap());
                }
                database.getReference(USERS_REFERENCE).updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_read_database_msg),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
