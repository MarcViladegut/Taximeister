package eps.udl.cat.meistertaxi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Switch driver;
    private ProgressDialog progressDialog;
    private boolean mailExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();

        progressDialog = new ProgressDialog(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        username = (EditText)findViewById(R.id.registerUsername);
        email = (EditText)findViewById(R.id.registerEmail);
        password = (EditText)findViewById(R.id.password);
        confirmPassword = (EditText)findViewById(R.id.confimPassword);
        driver = (Switch)findViewById(R.id.switch1);

        Button buttonCancel = (Button)findViewById(R.id.buttonCancel);
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
                if (checkInformation()){
                    progressDialog.setMessage("Registrando...");
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("auth", "createUserWithEmail:success");
                                        Toast.makeText(getApplicationContext(), getString(R.string.account_created_text), Toast.LENGTH_LONG).show();
                                        FirebaseUser currentUser = mAuth.getCurrentUser();

                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference("users");

                                        User user = new User(username.getText().toString(), email.getText().toString(), driver.isChecked());
                                        myRef.child(currentUser.getUid()).setValue(user);

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        progressDialog.dismiss();
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("auth", "createUserWithEmail:failure", task.getException());
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
        }
    }

    private boolean checkInformation(){
        // Check an empty fields
        if (username.getText().toString().equals("") || email.getText().toString().equals("") ||
            password.getText().toString().equals("") || confirmPassword.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "They are an empty fields",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check the same password
        if (!password.getText().toString().equals(confirmPassword.getText().toString())){
            Toast.makeText(getApplicationContext(), "The passwords are not equals",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.getText().toString().length() < 6){
            Toast.makeText(getApplicationContext(), "The password must have at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check is the correct format on email
        if (!isEmailValid(email.getText().toString())){
            Toast.makeText(getApplicationContext(), "The email format is incorrect",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        mAuth.fetchSignInMethodsForEmail(email.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (!task.getResult().getSignInMethods().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "The email has been already exists",
                                    Toast.LENGTH_SHORT).show();
                            mailExists=false;
                        } else {
                            mailExists=true;
                        }
                    }
                });
        return mailExists;
    }

    private static boolean isEmailValid(String email){
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
