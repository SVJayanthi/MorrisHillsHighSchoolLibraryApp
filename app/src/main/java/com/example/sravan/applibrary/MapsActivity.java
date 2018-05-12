package com.example.sravan.applibrary;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sravan.applibrary.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//Displays the map of the library with interactive buttons to direct them to appropriate searches
public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{


    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mUserDatabaseReference;
    private StorageReference mStorageRef;

    private String userId;

    //Instantiate objects
    private Button buttonAD;
    private Button buttonEK;
    private Button buttonLR;
    private Button buttonSZ;
    private Button buttonResources;
    private Button buttonCalendar;
    private Button buttonBackpack;
    private Button buttonShare;

    private ImageView imageUser;
    private TextView nameUser;
    private TextView emailUser;
    private TextView gradeUser;

    private Toast mToast;
    private final static String TAG = MapsActivity.class.getSimpleName();

    //Get references for objects
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();



        View content = findViewById(R.id.maps_app_bar);
        View mainContent = content.findViewById(R.id.include_maps);
        buttonAD = (Button) mainContent.findViewById(R.id.button1);
        buttonEK = (Button) mainContent.findViewById(R.id.button2);
        buttonLR = (Button) mainContent.findViewById(R.id.button3);
        buttonSZ = (Button) mainContent.findViewById(R.id.button4);
        buttonResources = (Button) mainContent.findViewById(R.id.buttonR);
        buttonCalendar = (Button) mainContent.findViewById(R.id.buttonC);
        buttonBackpack = (Button) mainContent.findViewById(R.id.buttonF);
        buttonShare = (Button) mainContent.findViewById(R.id.buttonS);

        //Based off section of library clicked, the books activity will be started with a query bounded by author's last names
        buttonAD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, BooksActivity.class);
                intent.putExtra("Search", "authorAD");
                startActivity(intent);
            }
        });
        buttonEK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, BooksActivity.class);
                intent.putExtra("Search", "authorEK");
                startActivity(intent);
            }
        });
        buttonLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, BooksActivity.class);
                intent.putExtra("Search", "authorLR");
                startActivity(intent);
            }
        });
        buttonSZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, BooksActivity.class);
                intent.putExtra("Search", "authorSZ");
                startActivity(intent);
            }
        });

        //Send the user to resource or calendar pages according to area of map selected (near computer lab or near calendar)
        buttonResources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, Resources.class);
                startActivity(intent);
            }
        });
        buttonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, CalendarActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

            }
        });

        buttonBackpack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, BackpackActivity.class);
                startActivity(intent);
            }
        });
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);

                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I found an app for the library at school." +
                        " I recommend you download it and get started on reading some books!");

            }
        });



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        imageUser = (ImageView) header.findViewById(R.id.user_image);
        nameUser = (TextView) header.findViewById(R.id.name_user);
        emailUser = (TextView) header.findViewById(R.id.email_user);
        gradeUser = (TextView) header.findViewById(R.id.grade_user);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_maps);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            itemView.setShiftingMode(false);
            itemView.setChecked(false);
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();
        setUpUser();

    }

    private void setUpUser() {

        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);

        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                nameUser.setText(currentUser.getName());
                emailUser.setText(currentUser.getEmail());
                gradeUser.setText("Grade " + Integer.toString(currentUser.getGrade()));
                putUserImage();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.");

            }
        });
    }


    private void putUserImage() {
        StorageReference photoRef = mStorageRef.child("users/" + userId + ".jpg");

        GlideApp.with(this)
                .load(photoRef)
                .into(imageUser);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I found an app for the library at school." +
                    " I recommend you download it and get started on reading some books!");
            startActivity(Intent.createChooser(intent, "Share"));
        } else if (id == R.id.nav_instructions) {
            Intent intent = new Intent(MapsActivity.this, Instructions.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(MapsActivity.this, Resources.class);
            startActivity(intent);
        } else if (id == R.id.nav_liscence) {
            Intent intent = new Intent(MapsActivity.this, Liscence.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(MapsActivity.this, Report.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent sendMain = new Intent(MapsActivity.this, MainActivity.class);
                    startActivity(sendMain);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(MapsActivity.this, BooksActivity.class);
                    startActivity(sendBooks);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_maps:
                    //Intent sendMap = new Intent(MapsActivity.this, MapsActivity.class);
                    //startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(MapsActivity.this, CalendarActivity.class);
                    startActivity(sendCalendar);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);



                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(MapsActivity.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };
}