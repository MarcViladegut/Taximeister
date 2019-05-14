package eps.udl.cat.meistertaxi.client;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eps.udl.cat.meistertaxi.AppCompatPreferenceActivity;
import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.User;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        private User userRead;
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
                    userRead = dataSnapshot.getValue(User.class);
                    updateUI(preference);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            return true;
        }

        private void updateUI(Preference preferenceChanged) {
            if (preferenceChanged instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preferenceChanged;
                int index = listPreference.findIndexOfValue(stringValue);

                if (preferenceChanged.getKey().equals("gender"))
                    userRead.setGender(index);

                // Set the summary to reflect the new value.
                preferenceChanged.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                switch (preferenceChanged.getKey()){
                    case "username":
                        userRead.setName(stringValue);
                        preferenceChanged.setSummary(stringValue);
                        break;
                    case "surname":
                        userRead.setSurname(stringValue);
                        preferenceChanged.setSummary(stringValue);
                        break;
                    case "smoke":
                        if (stringValue.equals("true")){
                            userRead.setSmoke(true);
                            preferenceChanged.setSummary(R.string.yes_text);
                        } else {
                            userRead.setSmoke(false);
                            preferenceChanged.setSummary(R.string.no_text);
                        }
                        break;
                    case "transit":
                        if (stringValue.equals("true"))
                            preferenceChanged.setSummary(R.string.yes_text);
                        else
                            preferenceChanged.setSummary(R.string.no_text);
                        break;

                    default:
                        preferenceChanged.setSummary(stringValue);
                }
            }

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(mAuth.getUid(), userRead.toMap());
            database.getReference("users").updateChildren(childUpdates);
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("currency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_account);
            setHasOptionsMenu(true);

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            FirebaseUser userLogin = mAuth.getCurrentUser();
            DatabaseReference usersRef = database.getReference("users").child(userLogin.getUid());
            usersRef.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User userRead = dataSnapshot.getValue(User.class);
                    Preference userPref = findPreference("username");
                    Preference surnamePref = findPreference("surname");
                    Preference genderPref = findPreference("gender");
                    Preference smokePref = findPreference("smoke");

                    userPref.setSummary(userRead.getName());
                    surnamePref.setSummary(userRead.getSurname());
                    ListPreference listPreference = (ListPreference) genderPref;
                    genderPref.setSummary(userRead.getGender() >= 0 ? listPreference.getEntries()[userRead.getGender()] : null);
                    smokePref.setSummary(userRead.isSmoke() ? R.string.yes_text : R.string.no_text);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            bindPreferenceSummaryToValue(findPreference("username"));
            bindPreferenceSummaryToValue(findPreference("surname"));
            bindPreferenceSummaryToValue(findPreference("gender"));
            bindPreferenceSummaryToBoolean(findPreference("smoke"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_map);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("styleMap"));
            bindPreferenceSummaryToBoolean(findPreference("transit"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
