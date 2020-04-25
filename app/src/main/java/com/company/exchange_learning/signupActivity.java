package com.company.exchange_learning;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.company.exchange_learning.model.BasicUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class signupActivity extends AppCompatActivity {
    private static final String TAG = "signupActivity";
    private EditText fname_v, lname_v, city_v, password_v, email_v;
    private Spinner countrySpinner, communitySpinner;
    private CardView signUp;
    private TextView goToLogin;
    private AVLoadingIndicatorView progressBar;
    private RadioGroup rgroup;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mAuth = FirebaseAuth.getInstance();
        fname_v = findViewById(R.id.FNametextInputEditText);
        lname_v = findViewById(R.id.LNametextInputEditText);
        city_v = findViewById(R.id.city_signup);
        signUp = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.signup_prog);
        password_v = findViewById(R.id.PasswordtextInputEditText);
        email_v = findViewById(R.id.EmailtextInputEditText);
        countrySpinner = findViewById(R.id.country_spinner);
        communitySpinner = findViewById(R.id.community_spinner);
        rgroup = findViewById(R.id.signup_rg);
//        RadioButton rb = findViewById(R.id.male_button);
//        rb.setSelected(true);
        populateCommunity();
        populateCountry();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });
        goToLogin = findViewById(R.id.goBackToLogin);
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String getGender() {

        try {
            int selectedId = rgroup.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            return radioButton.getText().toString();
        } catch (Exception e) {
            return null;
        }
    }


    public void signup() {
        final String fname = fname_v.getText().toString().trim();

        final String lname = lname_v.getText().toString().trim();

        final String email = email_v.getText().toString().trim();

        String password = password_v.getText().toString().trim();

        final String city = city_v.getText().toString().trim();

        final String country = countrySpinner.getSelectedItem().toString();
        final String community = communitySpinner.getSelectedItem().toString();

        final String gender = getGender();


        if (fname.length() == 0 || email.length() == 0 || lname.length() == 0 || city.length() == 0 || gender == null
        ) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else if (password.length() == 0) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "fName " + fname);
            Log.d(TAG, "lName " + lname);
            Log.d(TAG, "email " + email);
            Log.d(TAG, "password " + password);
            Log.d(TAG, "city " + city);
            Log.d(TAG, "country " + country);
            Log.d(TAG, "community " + community);
            Log.d(TAG, "Gender " + gender);

            signUp.setVisibility(View.INVISIBLE);
            hideKeyboard(signupActivity.this);
            progressBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fname + " " + lname)
                                        .build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User profile updated.");
                                                }
                                            }
                                        });
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Verfication Email sent");
                                                } else {
                                                    Log.d(TAG, "Unable to send verfication email");
                                                }
                                            }
                                        });
                                try {
                                    BasicUser newUser = new BasicUser(fname + " " + lname, gender, country, city, community, email, "none");
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("User_Information").child(user.getUid());
                                    myRef.setValue(newUser, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Log.d(TAG, "Error entering data " + databaseError.getMessage());
                                                Toast.makeText(signupActivity.this, "Some Error occurred while signing up", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.d(TAG, "Data inserted successfully");
                                                signUp.setVisibility(View.VISIBLE);
                                                progressBar.hide();
                                                showDialog();
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                                FirebaseAuth.getInstance().signOut();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(signupActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void populateCommunity() {
        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Computer Science");
        spinnerArray.add("Maths");
        spinnerArray.add("Physics");
        spinnerArray.add("Electronics");
        spinnerArray.add("Machine Learning");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        communitySpinner.setAdapter(adapter);
    }

    private void populateCountry() {
        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Pakistan");
        spinnerArray.add("Australia");
        spinnerArray.add("Spain");
        spinnerArray.add("France");
        spinnerArray.add("USA");
        spinnerArray.add("UK");
        spinnerArray.add("India");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);
    }

    private void showDialog() {
        new AlertDialog.Builder(signupActivity.this)
                .setTitle("SignUp successful")
                .setMessage("A verfication email has been sent to your email, please verify your account before logging in.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }
}
