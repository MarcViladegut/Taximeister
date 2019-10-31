package eps.udl.cat.meistertaxi.DriverApp;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

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
import eps.udl.cat.meistertaxi.Models.Driver;
import eps.udl.cat.meistertaxi.R;

import static eps.udl.cat.meistertaxi.Constants.DRIVER_LICENCE_PREFERENCE;
import static eps.udl.cat.meistertaxi.Constants.DRIVER_NAME_PREFERENCE;
import static eps.udl.cat.meistertaxi.Constants.DRIVER_SMOKE_PREFERENCE;
import static eps.udl.cat.meistertaxi.Constants.DRIVER_SURNAME_PREFERENCE;
import static eps.udl.cat.meistertaxi.Constants.USERS_REFERENCE;

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
        DatabaseReference usersRef = database.getReference(USERS_REFERENCE).child(userLogin.getUid());
        usersRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Driver userRead = dataSnapshot.getValue(Driver.class);
                Preference userPref = findPreference(DRIVER_NAME_PREFERENCE);
                Preference surnamePref = findPreference(DRIVER_SURNAME_PREFERENCE);
                Preference licencePref = findPreference(DRIVER_LICENCE_PREFERENCE);
                Preference smokePref = findPreference(DRIVER_SMOKE_PREFERENCE);

                userPref.setSummary(userRead.getName());
                surnamePref.setSummary(userRead.getSurname());
                licencePref.setSummary(Long.toString(userRead.getLicence()));
                smokePref.setSummary(userRead.isSmoke() ? R.string.yes_text : R.string.no_text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_read_database_msg),
                        Toast.LENGTH_SHORT).show();
            }
        });

        bindPreferenceSummaryToValue(findPreference(DRIVER_NAME_PREFERENCE));
        bindPreferenceSummaryToValue(findPreference(DRIVER_SURNAME_PREFERENCE));
        bindPreferenceSummaryToValue(findPreference(DRIVER_LICENCE_PREFERENCE));
        bindPreferenceSummaryToBoolean(findPreference(DRIVER_SMOKE_PREFERENCE));
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
            DatabaseReference usersRef = database.getReference(USERS_REFERENCE).child(userLogin.getUid());
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
                case DRIVER_NAME_PREFERENCE:
                    userRead.setName(stringValue);
                    preferenceChanged.setSummary(stringValue);
                    break;
                case DRIVER_SURNAME_PREFERENCE:
                    userRead.setSurname(stringValue);
                    preferenceChanged.setSummary(stringValue);
                    break;
                case DRIVER_LICENCE_PREFERENCE:
                    userRead.setLicence(Long.parseLong(stringValue));
                    preferenceChanged.setSummary(stringValue);
                    break;
                case DRIVER_SMOKE_PREFERENCE:
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

            if (preferenceChanged.getKey().equals(DRIVER_SMOKE_PREFERENCE)){
                if (preferenceChanged.equals("true"))
                    preferenceChanged.setSummary(R.string.yes_text);
                else
                    preferenceChanged.setSummary(R.string.no_text);
            } else
                preferenceChanged.setSummary(stringValue);

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(mAuth.getUid(), userRead.toMap());
            database.getReference(USERS_REFERENCE).updateChildren(childUpdates);
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
