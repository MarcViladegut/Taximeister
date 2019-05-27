package eps.udl.cat.meistertaxi.Main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eps.udl.cat.meistertaxi.Client;
import eps.udl.cat.meistertaxi.Driver;
import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.User;

import static eps.udl.cat.meistertaxi.Constants.DEFAULT_GENDER;
import static eps.udl.cat.meistertaxi.Constants.DEFAULT_LICENCE;
import static eps.udl.cat.meistertaxi.Constants.NONE;
import static eps.udl.cat.meistertaxi.Constants.USERS_REFERENCE;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Switch driver;
    private ProgressDialog progressDialog;
    private boolean mailExists;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersRef;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);

        /* Initialize Firebase Auth */
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance();
        mUsersRef = mDatabase.getReference(USERS_REFERENCE);

        username = (EditText) findViewById(R.id.registerUsername);
        email = (EditText) findViewById(R.id.registerEmail);
        password = (EditText) findViewById(R.id.password);
        confirmPassword = (EditText) findViewById(R.id.confimPassword);
        driver = (Switch) findViewById(R.id.switch1);

        Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(this);

        Button buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.buttonCancel:
                intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.buttonSubmit:
                /* Check if the fields on registration UI are correct */
                if (checkInformation()) {
                    progressDialog.setMessage(getString(R.string.sign_in_msg));
                    progressDialog.show();

                    createUserWithEmail();
                }
                break;
        }
    }

    private boolean checkInformation() {
        // Check an empty fields
        if (username.getText().toString().equals(NONE) || email.getText().toString().equals(NONE) ||
                password.getText().toString().equals(NONE) || confirmPassword.getText().toString().equals(NONE)) {
            Toast.makeText(getApplicationContext(), getString(R.string.blank_fields_msg),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check the same password
        if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Toast.makeText(getApplicationContext(), getString(R.string.passwords_not_equals_msg),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        /* Check the passwords more than 6 characters */
        if (password.getText().toString().length() < 6) {
            Toast.makeText(getApplicationContext(), getString(R.string.password_six_char_msg),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check is the correct format on email
        if (!isEmailValid(email.getText().toString())) {
            Toast.makeText(getApplicationContext(), getString(R.string.incorrect_email_msg),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        /* Check is the email is available on Firebase */
        mAuth.fetchSignInMethodsForEmail(email.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (!task.getResult().getSignInMethods().isEmpty()) {
                            Toast.makeText(getApplicationContext(), getString(R.string.email_exists_msg),
                                    Toast.LENGTH_SHORT).show();
                            mailExists = false;
                        } else {
                            mailExists = true;
                        }
                    }
                });
        return mailExists;
    }

    private static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void createUserWithEmail() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            requestFirebaseTokenAndSignIn();

                            Toast.makeText(getApplicationContext(), getString(R.string.account_created_text), Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), getString(R.string.auth_failed_msg),
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void requestFirebaseTokenAndSignIn() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful())
                            return;

                        /* Create a user with token that serve to recieve a notifications */
                        String token = task.getResult().getToken();

                        mCurrentUser = mAuth.getCurrentUser();
                        user = new User(username.getText().toString(), email.getText().toString());
                        user.setToken(token);

                        if (driver.isChecked()) {
                            user.setDriver(true);
                            Driver driver = new Driver(user, DEFAULT_LICENCE);
                            mUsersRef.child(mCurrentUser.getUid()).setValue(driver);
                        } else {
                            user.setDriver(false);
                            Client client = new Client(user, DEFAULT_GENDER);
                            mUsersRef.child(mCurrentUser.getUid()).setValue(client);
                        }
                    }
                });
    }
}
