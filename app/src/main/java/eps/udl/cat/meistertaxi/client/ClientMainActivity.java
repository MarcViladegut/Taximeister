package eps.udl.cat.meistertaxi.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eps.udl.cat.meistertaxi.MainActivity;
import eps.udl.cat.meistertaxi.R;

public class ClientMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapClickListener {

    ClientMainActivity.ParserTask parserTask;


    //PAYPAL
    private static PayPalConfiguration config = new PayPalConfiguration()

            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId("ASOBY-QQvBf_kxLtW8pzZo0e7Giaj34a5ECUYzo8WBitAGQoo1HT0YBfDCYU2xe6TxBtUQj5qFRHXm7v");


    public static final double TAX_INITIAL = 2.36;

    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    ConstraintLayout estimatedCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkLocationPermission();

        // Initializing
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

        Intent intent = new Intent(this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        startService(intent);

    }

    @Override
    protected void onResume() {
        if (mMap != null) {
            updateMap();
        }
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
        String style = sharedPreferences.getString("styleMap", "");
        boolean traffic = sharedPreferences.getBoolean("transit", false);
        double price = Double.longBitsToDouble(sharedPreferences
                .getLong("price", Double.doubleToLongBits(0.0)));
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

        // Update currency
        if (currency != null) {
            switch (currency) {
                case "1":
                    cost.setText(String.format("%.2f", (price * 0.85)) + " pounds");
                    break;
                case "2":
                    cost.setText(String.format("%.2f", (price * 1.12)) + " dollar");
                    break;
                default:
                    cost.setText(String.format("%.2f", price) + " euros");
                    break;
            }
        }
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
            LatLng origin = MarkerPoints.get(0);
            LatLng dest = MarkerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getUrl(origin, dest);
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
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Fetches data from url passed
     */
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
            parserTask.execute(result);

        }
    }

    public double getCostService(int distance) {
        double priceKm;

        // tariff 1: Under 20 km
        if (distance <= 20000)
            priceKm = 0.002;
            // tariff 2: between 20 and 100 km
        else if (distance <= 100000)
            priceKm = 0.0015;
            // tariff 3: More than 100 km
        else
            priceKm = 0.001;

        return priceKm * distance + TAX_INITIAL;
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        public double getPrice() {
            return price;
        }

        double price;

        // Parsing the data in non-ui thread
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            TextView distance = findViewById(R.id.distanceValue);
            TextView duration = findViewById(R.id.durationValue);
            TextView cost = findViewById(R.id.costValue);
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                distance.setText(parser.distance);
                duration.setText(parser.duration);

                price = getCostService(parser.distValue);

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .edit();

                editor.putLong("price", Double.doubleToRawLongBits(price))
                        .apply();

                String currency = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getString("currency", "");

                String currencyText = "";
                String payPalText = "";

                if (currency != null)
                    switch (currency) {
                        case "1":
                            currencyText = " pounds";
                            payPalText = "GBP";
                            price *= 0.85;
                            break;
                        case "2":
                            currencyText = " dollars";
                            payPalText = "USD";
                            price *= 1.12;
                            break;
                        default:
                            payPalText = "EUR";
                            currencyText = " euros";
                            break;
                    }
                cost.setText(String.format("%.2f", price) + currencyText);

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

                    LatLng position = new LatLng(Double.parseDouble(point.get("lat")),
                            Double.parseDouble(point.get("lng")));

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
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
                            "Location permission is needed for this application", Snackbar.LENGTH_INDEFINITE)
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
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();

        if (id == R.id.reservation) {
            // TODO implements de firebase to store a reservations
        } else if (id == R.id.configuration) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.disconnect) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onBuyPressed(View pressed) {

        String currency = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getString("currency", "");
        String payPalText = "";

        if (currency != null)
            switch (currency) {
                case "1":
                    payPalText = "GBP";
                    break;
                case "2":
                    payPalText = "USD";
                    break;
                default:
                    payPalText = "EUR";
                    break;
            }

        PayPalPayment payment = new PayPalPayment(new BigDecimal(Double.toString(parserTask.getPrice())), payPalText, "MisterTaxi",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    showAlert("Compra PayPal","La seva compra per PayPal s'ha efectuat correctament.\nEs una versió de prova no s'efectuarà cap carrec a la seva tarjeta");
                    estimatedCost.setVisibility(View.GONE);
                    MarkerPoints.clear();
                    mMap.clear();
                    Log.i("paymentExample", confirm.toJSONObject().toString(4));

                    // TODO: send 'confirm' to your server for verification.
                    // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                    // for more details.

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
    }

    private void showAlert(String tittle, String message){
        new AlertDialog.Builder(this)
                .setTitle(tittle)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }
}
