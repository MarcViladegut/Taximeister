package eps.udl.cat.meistertaxi;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import eps.udl.cat.meistertaxi.Driver.DriverMainActivity;
import eps.udl.cat.meistertaxi.client.ClientMainActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        email = findViewById(R.id.editText);
        password = findViewById(R.id.editText1);

        Button buttonNewUser = findViewById(R.id.buttonNewUser);
        buttonNewUser.setOnClickListener(this);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);

        TextView textView = findViewById(R.id.textForgotPassword);
        textView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNewUser:
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.buttonLogin:
                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(), "There are a blank fields",
                            Toast.LENGTH_SHORT).show();
                else
                    logIn(email.getText().toString(), password.getText().toString());
                break;
            case R.id.textForgotPassword:
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog_forgot_password, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilderUserInput.setView(mView);

                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton(R.string.send_text, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                Toast.makeText(MainActivity.this,
                                        getString(R.string.forgot_pass_text),
                                        Toast.LENGTH_LONG).show();
                            }
                        })

                        .setNegativeButton(R.string.text_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
                break;
        }
    }

    private void logIn(String email, String password) {
        progressDialog.setMessage("Iniciando sessión");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Check is the client user o driver user
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            FirebaseUser userLogin = mAuth.getCurrentUser();
                            DatabaseReference usersRef = database.getReference("users").child(userLogin.getUid());
                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Intent i;
                                    User userRead = dataSnapshot.getValue(User.class);
                                    if (userRead.isDriver())
                                        i = new Intent(getApplicationContext(), DriverMainActivity.class);
                                    else
                                        i = new Intent(getApplicationContext(), ClientMainActivity.class);
                                    progressDialog.dismiss();
                                    startActivity(i);
                                    finish();
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("auth", "signInWithEmail:success");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Error to read from Database",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w("auth", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
