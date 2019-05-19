package eps.udl.cat.meistertaxi.ClientApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import eps.udl.cat.meistertaxi.Client;
import eps.udl.cat.meistertaxi.Main.MainActivity;
import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.Reservation;
import eps.udl.cat.meistertaxi.Route;

import static eps.udl.cat.meistertaxi.Constants.SPACE;

public class ClientMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener {

    // PayPal
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId(Integer.toString(R.string.client_id_paypal));

    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    ConstraintLayout estimatedCost;
    Reservation reservation;
    Route route;
    ClientMainActivity.ParserTask parserTask;
    ProgressBar progressBar;

    private TextView mNameTextView;
    private TextView mEmailTextView;
    private ImageView mAvatarImageView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        checkLocationPermission();

        MarkerPoints = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        estimatedCost = findViewById(R.id.reservation_fragment);
        estimatedCost.setVisibility(View.GONE);

        // Initialize a PayPal Services
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        // Initialize route
        route = new Route();

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onResume() {
        if (mMap != null) {
            updateMap();
        }
        updateNavigationDrawer();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);

        // Update a Map of the sharedPreferences
        updateMap();

        //Initialize Google Play Services
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        // Setting onclick event listener for the map
        mMap.setOnMapClickListener(this);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean traffic = sharedPreferences.getBoolean("transit", false);
        String style = sharedPreferences.getString("styleMap", "");
        String currency = sharedPreferences.getString("currency", "");
        TextView cost = findViewById(R.id.costValue);

        // Update a style map
        if (style != null) {
            switch (style) {
                case "1":
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_retro));
                    break;
                case "2":
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night));
                    break;
                default:
                    mMap.setMapStyle(null);
                    break;
            }
        }

        // Update a traffic
        mMap.setTrafficEnabled(traffic);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Update currency
        if (currency != null && reservation != null) {
            switch (currency) {
                case "1":
                    cost.setText(String.format("%.2f", reservation.getCostToPounds()) + " pounds");
                    break;
                case "2":
                    cost.setText(String.format("%.2f", reservation.getCostToDollars()) + " dollar");
                    break;
                default:
                    cost.setText(String.format("%.2f", reservation.getCost()) + " euros");
                    break;
            }
        }
    }

    public void updateNavigationDrawer(){

        NavigationView navigationView = findViewById(R.id.nav_view);
        View navHeaderView = navigationView.getHeaderView(0);
        mNameTextView = (TextView) navHeaderView.findViewById(R.id.navUsername);
        mEmailTextView = (TextView) navHeaderView.findViewById(R.id.navEmail);
        mAvatarImageView = (ImageView) navHeaderView.findViewById(R.id.imageViewAvatar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser userLogin = mAuth.getCurrentUser();
        DatabaseReference usersRef = database.getReference("users").child(userLogin.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Client userRead = dataSnapshot.getValue(Client.class);

                    mNameTextView.setText(userRead.getName() + SPACE + userRead.getSurname());
                    mEmailTextView.setText(userRead.getEmail());

                    switch (userRead.getGender()) {
                        case 0:
                            mAvatarImageView.setImageResource(R.mipmap.ic_avatar_robot_round);
                            break;
                        case 1:
                            mAvatarImageView.setImageResource(R.mipmap.ic_avatar_girl_round);
                            break;
                        case 2:
                            mAvatarImageView.setImageResource(R.mipmap.ic_avatar_boy_round);
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error to update NavigationDrawer",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mLastLocation != null) {
            LatLng myPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            onMapClick(myPosition);
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng point) {
        // Already two locations
        if (MarkerPoints.size() > 1) {
            MarkerPoints.clear();
            mMap.clear();
        }
        // Adding new item to the ArrayList
        MarkerPoints.add(point);

        if (MarkerPoints.size() == 2 && point.toString().equals(MarkerPoints.get(0).toString())) {
            MarkerPoints.remove(point);
            return;
        }

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(point);

        // For the start location, the color of marker is GREEN and for the end location, the color of marker is RED.
        if (MarkerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (MarkerPoints.size() == 2) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        // Add new marker to the Google Map Android API V2

        mMap.addMarker(options);


        // Checks, whether start and end locations are captured
        if (MarkerPoints.size() >= 2) {
            route.setOrigin(MarkerPoints.get(0));
            route.setDestination(MarkerPoints.get(1));
            progressBar.setVisibility(View.VISIBLE);
            // Getting URL to the Google Directions API
            String url = getUrl(route.getOrigin(), route.getDestination());
            Log.d("onMapClick", url);
            FetchUrl FetchUrl = new FetchUrl();

            // Start downloading json data from Google Directions API
            FetchUrl.execute(url);
        }
    }

    /**
     * A method to obtain a complete url for obtain a json routes
     */
    private String getUrl(LatLng ori, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + ori.latitude + "," + ori.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String api = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + api;
        Log.i("Date", parameters);

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.i("URL", url);

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {

        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder stringBuffer = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }

            data = stringBuffer.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            progressBar.setVisibility(View.INVISIBLE);
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Fetches data from url passed
     */
    @SuppressLint("StaticFieldLeak")
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            parserTask = new ClientMainActivity.ParserTask();
            // Execute on thread for parsing the JSON data
            if (result.contains("ZERO_RESULTS")) {
                Toast.makeText(ClientMainActivity.this, R.string.route_not_found, Toast.LENGTH_SHORT).show();
                mMap.clear();
                MarkerPoints.clear();
                ;
                return;
            }
            parserTask.execute(result);


        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            TextView date = findViewById(R.id.dayValue);
            TextView hour = findViewById(R.id.hourValue);
            TextView distance = findViewById(R.id.distanceValue);
            TextView duration = findViewById(R.id.durationValue);
            TextView cost = findViewById(R.id.costValue);
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();


                // Starts parsing data
                routes = parser.parse(jObject);

                route.setDistance(parser.distValue);
                route.setDuration(parser.duration);

                reservation = new Reservation(route, Calendar.getInstance().getTimeInMillis());
                date.setText(reservation.getDateToString());
                /*Calendar dateTime = reservation.getDateTime();
                int month = dateTime.get(Calendar.MONTH) + 1;
                date.setText(dateTime.get(Calendar.DAY_OF_MONTH) + "/" +
                        ((month < 10) ? ("0" + month) : month) + "/" + dateTime.get(Calendar.YEAR));*/

                hour.setText(reservation.getTimeToString());
                /*int hourOfDay = dateTime.get(Calendar.HOUR_OF_DAY);
                int minuteOfDay = dateTime.get(Calendar.MINUTE);
                hour.setText(((hourOfDay < 10) ? "0" + hourOfDay : hourOfDay) + ":" +
                        ((minuteOfDay < 10) ? "0" + minuteOfDay : minuteOfDay));*/
                distance.setText(parser.distance);
                duration.setText(parser.duration);

                String currency = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString("currency", "");

                if (currency != null)
                    switch (currency) {
                        case "1":
                            cost.setText(String.format("%.2f", reservation.getCostToPounds()) + " pounds");
                            break;
                        case "2":
                            cost.setText(String.format("%.2f", reservation.getCostToDollars()) + " dollars");
                            break;
                        default:
                            cost.setText(String.format("%.2f", reservation.getCost()) + " euros");
                            break;
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    LatLng position = new LatLng(Double.parseDouble(Objects.requireNonNull(point.get("lat"))),
                            Double.parseDouble(Objects.requireNonNull(point.get("lng"))));

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                        .width(10)
                        .color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
                estimatedCost.setVisibility(View.VISIBLE);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //Stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                startLocationPermissionRequest();
            } else
                startLocationPermissionRequest();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null)
                            buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Snackbar.make(findViewById(android.R.id.content),
                            R.string.text_permission, Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startLocationPermissionRequest();
                                }
                            }).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        int id = item.getItemId();

        if (id == R.id.reservation) {
            intent = new Intent(this, ReservationActivity.class);
            startActivity(intent);
        } else if (id == R.id.configuration) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void disconnectUser(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(view.getContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onBuyPressed(View pressed) {

        String currency = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("currency", "");
        String payPalText = "EUR";
        double cost = reservation.getCost();

        if (currency != null)
            switch (currency) {
                case "1":
                    cost = reservation.getCostToPounds();
                    payPalText = "GBP";
                    break;
                case "2":
                    cost = reservation.getCostToDollars();
                    payPalText = "USD";
                    break;
            }

        PayPalPayment payment = new PayPalPayment(new BigDecimal(Double.toString(cost)),
                payPalText, "MisterTaxi", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, 0);
    }

    public void onCustomizePressed(View pressed) {
        Intent intent = new Intent(this, CustomizeReservationActivity.class);
        Bundle bundle = new Bundle();

        intent.putExtra("reservationId", reservation.getIdReservation());
        bundle.putParcelable("reservationFrom", reservation.getOrigin());
        bundle.putParcelable("reservationTo", reservation.getDestination());

        intent.putExtra("bundleFromTo", bundle);

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        final DatabaseReference ref = database.getReference("reservations").child("numReservation");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue(Integer.class) == null) {
                                    ref.setValue(0);
                                    reservation.setIdReservation(0);
                                    ref.setValue(1);
                                } else {
                                    int num = dataSnapshot.getValue(Integer.class);
                                    reservation.setIdReservation(num);
                                    ref.setValue(num += 1);
                                }

                                //reservation.setPaid(true);

                                Calendar tmp = Calendar.getInstance();
                                tmp.set(Calendar.MINUTE, data.getIntExtra("minute", reservation.getCalendarFromDateTime().get(Calendar.MINUTE)));
                                tmp.set(Calendar.HOUR_OF_DAY, data.getIntExtra("hour", reservation.getCalendarFromDateTime().get(Calendar.HOUR_OF_DAY)));
                                tmp.set(Calendar.DAY_OF_MONTH, data.getIntExtra("day", reservation.getCalendarFromDateTime().get(Calendar.DAY_OF_MONTH)));
                                tmp.set(Calendar.MONTH, data.getIntExtra("month", reservation.getCalendarFromDateTime().get(Calendar.MONTH)));
                                tmp.set(Calendar.YEAR, data.getIntExtra("year", reservation.getCalendarFromDateTime().get(Calendar.YEAR)));

                                reservation.setDateTimeFromCalendar(tmp);
                                reservation.setOriginToString(getApplicationContext());
                                reservation.setDestinationToString(getApplicationContext());

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("reservations");

                                myRef.child(currentUser.getUid()).child(Integer.toString(reservation.getIdReservation())).setValue(reservation);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        showAlert(getString(R.string.reserve_successful), getString(R.string.reserve_successful_msg));
                        estimatedCost.setVisibility(View.GONE);
                        MarkerPoints.clear();
                        mMap.clear();

                        Log.i("paymentExample", confirm.toJSONObject().toString(4));

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "The user canceled", Toast.LENGTH_LONG).show();
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(this, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs", Toast.LENGTH_LONG).show();
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else {
            if (resultCode == Activity.RESULT_OK) {
                Calendar tmp = Calendar.getInstance();
                tmp.set(Calendar.MINUTE, data.getIntExtra("minute", reservation.getCalendarFromDateTime().get(Calendar.MINUTE)));
                tmp.set(Calendar.HOUR_OF_DAY, data.getIntExtra("hour", reservation.getCalendarFromDateTime().get(Calendar.HOUR_OF_DAY)));
                tmp.set(Calendar.DAY_OF_MONTH, data.getIntExtra("day", reservation.getCalendarFromDateTime().get(Calendar.DAY_OF_MONTH)));
                tmp.set(Calendar.MONTH, data.getIntExtra("month", reservation.getCalendarFromDateTime().get(Calendar.MONTH)));
                tmp.set(Calendar.YEAR, data.getIntExtra("year", reservation.getCalendarFromDateTime().get(Calendar.YEAR)));

                reservation.setDateTimeFromCalendar(tmp);
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String date_reservation = getString(R.string.date_reservation, format1.format(tmp.getTime()));
                TextView date = findViewById(R.id.dayValue);
                date.setText(date_reservation);
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                String hour_reservation = getString(R.string.hour_reservation, formatter.format(tmp.getTime()));
                TextView hour = findViewById(R.id.hourValue);
                hour.setText(hour_reservation);
            }
        }
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null).show();
    }
}
