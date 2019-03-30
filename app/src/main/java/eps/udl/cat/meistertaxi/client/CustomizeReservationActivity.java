package eps.udl.cat.meistertaxi.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import eps.udl.cat.meistertaxi.R;

public class CustomizeReservationActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String ZERO = "0";
    public static final String TWO_POINTS = ":";
    public static final String BAR = "/";

    public final Calendar calendar = Calendar.getInstance();

    ImageButton obtainDate, obtainHour;
    TextView tvDate, tvHour;
    Intent intentResult;

    final int minute = calendar.get(Calendar.MINUTE);
    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);
    final int month = calendar.get(Calendar.MONTH);
    final int year = calendar.get(Calendar.YEAR);

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_reservation);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Objects.requireNonNull(getSupportActionBar()).hide();

        intentResult = new Intent();

        TextView idReservation = findViewById(R.id.idReservationValue);
        idReservation.setText(Integer.toString(getIntent().getIntExtra("reservationId", 34)));

        Bundle bundle = getIntent().getParcelableExtra("bundleFromTo");
        LatLng startingPoint = bundle.getParcelable("reservationFrom");
        LatLng destinationPoint = bundle.getParcelable("reservationTo");

        TextView cityFrom = findViewById(R.id.startingPointCity);
        TextView cityTo = findViewById(R.id.destinationPointCity);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Address addressFrom, addressTo;
        try {
            addressFrom = geocoder.getFromLocation(startingPoint.latitude,
                    startingPoint.longitude, 1).get(0);
            cityFrom.setText(addressFrom.getLocality());

            addressTo = geocoder.getFromLocation(destinationPoint.latitude,
                    destinationPoint.longitude, 1).get(0);
            cityTo.setText(addressTo.getLocality());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Button cancelButton = findViewById(R.id.buttonCustomizeCancel);
        cancelButton.setOnClickListener(this);

        Button acceptButton = findViewById(R.id.buttonCustomizeAccept);
        acceptButton.setOnClickListener(this);

        obtainDate = findViewById(R.id.obtainDate);
        obtainHour = findViewById(R.id.obtainHour);
        obtainDate.setOnClickListener(this);
        obtainHour.setOnClickListener(this);

        tvDate = findViewById(R.id.tvDate);
        tvHour = findViewById(R.id.tvHour);

        tvDate.setText(((day > 9) ? day : ZERO + day) + BAR + (((month + 1) > 9) ? (month + 1) : ZERO + (month + 1))  + BAR + year);
        tvHour.setText(((hour < 10) ? String.valueOf(ZERO + hour) : String.valueOf(hour))
                + TWO_POINTS + ((minute < 10) ? String.valueOf(ZERO + minute) : String.valueOf(minute)));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonCustomizeAccept:
                setResult(Activity.RESULT_OK, intentResult);
                finish();
                break;
            case R.id.buttonCustomizeCancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
            case R.id.obtainDate:
                obtainDateValue();
                break;
            case R.id.obtainHour:
                obtainTimeValue();
                break;
        }
    }

    private void obtainDateValue(){
        DatePickerDialog date = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar tmp = Calendar.getInstance();
                final int actualMonth = month + 1;

                String dayUpdate = (dayOfMonth < 10) ? ZERO + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                String monthUpdate = (actualMonth < 10) ? ZERO + String.valueOf(actualMonth) : String.valueOf(actualMonth);

                tmp.set(Calendar.YEAR, year);
                tmp.set(Calendar.MONTH, month);
                tmp.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (tmp.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis()) {
                    tvDate.setText(dayUpdate + BAR + monthUpdate + BAR + year);
                    intentResult.putExtra("year", year);
                    intentResult.putExtra("month", month);
                    intentResult.putExtra("day", dayOfMonth);
                } else
                    Toast.makeText(getApplicationContext(), "The date would be more or equals than actual date", Toast.LENGTH_LONG).show();
            }
        }, year, month, day);
        date.show();
    }

    private void obtainTimeValue(){
        TimePickerDialog time = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar tmp = Calendar.getInstance();
                String hourUpdate = (hourOfDay < 10) ? String.valueOf(ZERO + hourOfDay) : String.valueOf(hourOfDay);
                String minuteUpdate = (minute < 10) ? String.valueOf(ZERO + minute) : String.valueOf(minute);

                tmp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                tmp.set(Calendar.MINUTE, minute);

                if (tmp.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis()) {
                    tvHour.setText(hourUpdate + TWO_POINTS + minuteUpdate);
                    intentResult.putExtra("hour", hourOfDay);
                    intentResult.putExtra("minute", minute);
                } else
                    Toast.makeText(getApplicationContext(), "The time would be more or equals than actual time", Toast.LENGTH_LONG).show();
            }
        }, hour, minute, false);

        time.show();
    }
}
