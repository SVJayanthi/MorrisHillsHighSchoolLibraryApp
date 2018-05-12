package com.example.sravan.applibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.sravan.applibrary.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

//Allows user to create a new account for utilizing the library
public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    //Instantiate objects
    private EditText mEmailView;
    private EditText mNameView;
    private EditText mPasswordView;
    private ImageButton mImageButton;
    private Spinner spinner;

    //List of possible search choices
    private String[] order = {"9", "10", "11", "12", "Teacher"};


    private Button mButton;
    private boolean mProfile = false;

    private final static String TAG = RegisterActivity.class.getSimpleName();

    //Set up references for objects
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.register_email);
        mNameView = (EditText) findViewById(R.id.register_name);

        mPasswordView = (EditText) findViewById(R.id.register_password);

        if (email != null) {
            mEmailView.setText(email);
        }
        if (password != null) {
            mPasswordView.setText(password);
        }


        mButton = (Button) findViewById(R.id.register_button);
        mImageButton = (ImageButton) findViewById(R.id.image_profile);

        //Set onClickListener will start the activity createAccount to register the User based off the fields filled
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick (View v) {
                createAccount();
            }
        });

        mImageButton.setOnClickListener(new OnClickListener() {
            public void onClick (View v) {
                dispatchTakePictureIntent();
            }
        });

        spinner = (Spinner) findViewById(R.id.grade_register);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.order, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setPrompt("Select Grade");


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }

    //Creates new User and new Account on database based off information specified in the fields
    private void createAccount() {
        Log.d(TAG, "createAccount:");
        if (!validateForm()) {
            return;
        }
        final String userEmail = mEmailView.getText().toString();
        final String userPass = mPasswordView.getText().toString();
        final String userName = mNameView.getText().toString();
        final int userGrade;
        if (spinner.getSelectedItem().toString().equals("Teacher")) {
            userGrade = 13;
        } else {
            userGrade = Integer.parseInt(spinner.getSelectedItem().toString());
        }


        setSubmitEnable(false);
        mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            final String id = currentUser.getUid();

                            User child = new User(userEmail, userPass, userName, userGrade, 0, 0, id);

                            mDatabase.child("Users").child(id).setValue(child);
                            setSubmitEnable(true);

                            Bitmap bitmap;
                            // Get the data from an ImageView as bytes
                            mImageButton.setDrawingCacheEnabled(true);
                            mImageButton.buildDrawingCache();
                            bitmap = mImageButton.getDrawingCache();

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            bitmap.recycle();


                            StorageReference photoRef = mStorageRef.child("users/" + id + ".jpg");


                            UploadTask uploadTask = photoRef.putBytes(byteArray);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                }
                            });

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            startActivity(new Intent(RegisterActivity.this , MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            setSubmitEnable(true);
                        }

                    }
                });
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageButton.setImageBitmap(imageBitmap);
            mProfile = true;
        }
    }


    //Disable user to submit another request will current request is processing
    private void setSubmitEnable(boolean enabled) {
        mEmailView.setEnabled(enabled);
        mPasswordView.setEnabled(enabled);
        mNameView.setEnabled(enabled);
        if (enabled) {
            mButton.setVisibility(View.VISIBLE);
        } else {
            mButton.setVisibility(View.GONE);
        }

    }

    //Check if user properly filled forms
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Required.");
            valid = false;
        } else {
            mEmailView.setError(null);
        }

        String name = mNameView.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameView.setError("Required.");
            valid = false;
        } else {
            mNameView.setError(null);
        }

        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Required.");
            valid = false;
        } else if(password.length() < 6){
            mPasswordView.setError("Password is too short.");
        } else {
            mPasswordView.setError(null);
        }

        return valid;
    }


    //Handles item clicks to change search query
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }
}
