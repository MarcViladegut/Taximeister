package eps.udl.cat.meistertaxi.DriverApp;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import eps.udl.cat.meistertaxi.AppCompatPreferenceActivity;
import eps.udl.cat.meistertaxi.Client;
import eps.udl.cat.meistertaxi.Driver;
import eps.udl.cat.meistertaxi.R;

public class SettingsFragment extends AppCompatPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        setupActionBar();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser userLogin = mAuth.getCurrentUser();
        DatabaseReference usersRef = database.getReference("users").child(userLogin.getUid());
        usersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Driver userRead = dataSnapshot.getValue(Driver.class);
                Preference userPref = findPreference("driver_name");
                Preference surnamePref = findPreference("driver_surname");
                Preference licencePref = findPreference("driver_licence");
                Preference smokePref = findPreference("driver_smoke");

                userPref.setSummary(userRead.getName());
                surnamePref.setSummary(userRead.getSurname());
                licencePref.setSummary(Long.toString(userRead.getLicence()));
                smokePref.setSummary(userRead.isSmoke() ? R.string.yes_text : R.string.no_text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        bindPreferenceSummaryToValue(findPreference("driver_name"));
        bindPreferenceSummaryToValue(findPreference("driver_surname"));
        bindPreferenceSummaryToValue(findPreference("driver_licence"));
        bindPreferenceSummaryToBoolean(findPreference("driver_smoke"));
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        private Driver userRead;
        private String stringValue;
        private FirebaseAuth mAuth;
        private FirebaseDatabase database;
        private Preference preference;

        @Override
        public boolean onPreferenceChange(Preference preferenceChanged, Object value) {

            stringValue = value.toString();
            preference = preferenceChanged;
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();

            FirebaseUser userLogin = mAuth.getCurrentUser();
            DatabaseReference usersRef = database.getReference("users").child(userLogin.getUid());
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userRead = dataSnapshot.getValue(Driver.class);
                    updateUI(preference);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            return true;
        }

        private void updateUI(Preference preferenceChanged) {
            switch (preferenceChanged.getKey()){
                case "driver_name":
                    userRead.setName(stringValue);
                    preferenceChanged.setSummary(stringValue);
                    break;
                case "driver_surname":
                    userRead.setSurname(stringValue);
                    preferenceChanged.setSummary(stringValue);
                    break;
                case "driver_licence":
                    userRead.setLicence(Long.parseLong(stringValue));
                    preferenceChanged.setSummary(stringValue);
                    break;
                case "driver_smoke":
                    if (stringValue.equals("true")){
                        userRead.setSmoke(true);
                        preferenceChanged.setSummary(R.string.yes_text);
                    } else {
                        userRead.setSmoke(false);
                        preferenceChanged.setSummary(R.string.no_text);
                    }
                    break;
                default:
                    preferenceChanged.setSummary(stringValue);
            }

            if (preferenceChanged.getKey().equals("driver_smoke")){
                if (preferenceChanged.equals("true"))
                    preferenceChanged.setSummary(R.string.yes_text);
                else
                    preferenceChanged.setSummary(R.string.no_text);
            } else
                preferenceChanged.setSummary(stringValue);

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(mAuth.getUid(), userRead.toMap());
            database.getReference("users").updateChildren(childUpdates);
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static void bindPreferenceSummaryToBoolean(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), false));
    }
}
