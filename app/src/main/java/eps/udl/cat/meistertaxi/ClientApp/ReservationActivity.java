package eps.udl.cat.meistertaxi.ClientApp;

import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.Reservation;
import eps.udl.cat.meistertaxi.ReservationAdapter;

public class ReservationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser userLogin;
    private FirebaseDatabase database;

    private ArrayList<Reservation> listData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        userLogin = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        DatabaseReference ref = database.getReference("reservations").child(userLogin.getUid());

        // Create a list data which will be displayed in inner ListView.
        listData = new ArrayList<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot val : dataSnapshot.getChildren()){
                    Log.i("informacio", val.toString());
                    Reservation reservation = val.getValue(Reservation.class);
                    listData.add(reservation);
                }
                // Create the ArrayAdapter use the item row layout and the list data.
                ReservationAdapter reservationAdapter = new ReservationAdapter(getApplicationContext(), listData);

                // Set this adapter to inner ListView object.
                ListView listView = (ListView) findViewById(R.id.lvReservations);
                listView.setAdapter(reservationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "It's not possible to load a reservations", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
