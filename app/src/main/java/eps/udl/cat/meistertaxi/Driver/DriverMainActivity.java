package eps.udl.cat.meistertaxi.Driver;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import eps.udl.cat.meistertaxi.MainActivity;
import eps.udl.cat.meistertaxi.R;

public class DriverMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

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
                startActivity(intent);
                finish();
            }
        });
    }
}
