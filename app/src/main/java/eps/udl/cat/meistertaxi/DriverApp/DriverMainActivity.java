package eps.udl.cat.meistertaxi.DriverApp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import eps.udl.cat.meistertaxi.Driver;
import eps.udl.cat.meistertaxi.Main.MainActivity;
import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.Reservation;
import eps.udl.cat.meistertaxi.ReservationAdapter;

import static eps.udl.cat.meistertaxi.Constants.BAR;
import static eps.udl.cat.meistertaxi.Constants.RESERVATION_REFERENCE;
import static eps.udl.cat.meistertaxi.Constants.SPACE;
import static eps.udl.cat.meistertaxi.Constants.USERS_REFERENCE;
import static eps.udl.cat.meistertaxi.Constants.ZERO;

public class DriverMainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private TextView nameDriver;
    private TextView licenceDriver;

    private ArrayList<Reservation> listData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        /* Initialize Firebase */
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        /* Initialize a components */
        nameDriver = (TextView)findViewById(R.id.nameTaxiDriver);
        licenceDriver = (TextView)findViewById(R.id.licenceNum);

        /* Configuración del listView para las reservas de cada dia del calendario */
        CalendarView calendarView = (CalendarView)findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                updateListReservations(year, month, dayOfMonth);
            }
        });

        Calendar calendar = Calendar.getInstance();
        updateListReservations(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Configure the buttons of the UI
        ImageButton config = findViewById(R.id.configButton);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsFragment.class);
                startActivity(intent);
            }
        });

        ImageButton exit = findViewById(R.id.disconnectButton);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                mAuth.signOut();
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateListReservations(int year, int month, int dayOfMonth){
        DatabaseReference ref = database.getReference(RESERVATION_REFERENCE).child(currentUser.getUid());
        listData = new ArrayList<>();
        final String dataReservation = ((dayOfMonth < 10) ? (ZERO + dayOfMonth) : dayOfMonth) + BAR +
                ((month < 10) ? (ZERO + (month + 1)) : (month + 1)) + BAR + year;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot val : dataSnapshot.getChildren()){
                    Reservation reservation = val.getValue(Reservation.class);
                    if (reservation.getDateToString().equals(dataReservation))
                        listData.add(reservation);
                }
                if (listData.size()==0){
                    Toast.makeText(getApplicationContext(), getString(R.string.no_reservations),
                            Toast.LENGTH_SHORT).show();
                }
                // Create the ArrayAdapter use the item row layout and the list data.
                ReservationAdapter reservationAdapter = new ReservationAdapter(getApplicationContext(), listData);

                // Set this adapter to inner ListView object.
                ListView listReservations = findViewById(R.id.listReservation);
                listReservations.setAdapter(reservationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_read_database_msg),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        updateProfileDriver();
        super.onResume();
    }

    private void updateProfileDriver(){
        DatabaseReference usersRef = database.getReference(USERS_REFERENCE).child(currentUser.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Driver driver = dataSnapshot.getValue(Driver.class);

                    nameDriver.setText(driver.getName() + SPACE + driver.getSurname());
                    licenceDriver.setText(Long.toString(driver.getLicence()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_read_database_msg),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
