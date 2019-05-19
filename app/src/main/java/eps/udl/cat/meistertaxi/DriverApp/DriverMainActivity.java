package eps.udl.cat.meistertaxi.DriverApp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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

import eps.udl.cat.meistertaxi.Driver;
import eps.udl.cat.meistertaxi.Main.MainActivity;
import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.User;

import static eps.udl.cat.meistertaxi.Constants.SPACE;

public class DriverMainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    private TextView nameDriver;
    private TextView licenceDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        // Initialize a components
        nameDriver = (TextView)findViewById(R.id.nameTaxiDriver);
        licenceDriver = (TextView)findViewById(R.id.licenceNum);

        // Configuración del listView para las reservas de cada dia del calendario
        ListView listReservations;
        ArrayAdapter<String> adapter;

        listReservations = findViewById(R.id.listReservation);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        adapter.add("Reserva 1");
        adapter.add("Reserva 2");
        adapter.add("Reserva 3");
        adapter.add("Reserva 4");
        adapter.add("Reserva 5");

        listReservations.setAdapter(adapter);

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

    @Override
    protected void onResume() {
        updateProfileDriver();
        super.onResume();
    }

    private void updateProfileDriver(){
        DatabaseReference usersRef = database.getReference("users").child(currentUser.getUid());
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
                Toast.makeText(getApplicationContext(), "Error to update a profile",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
