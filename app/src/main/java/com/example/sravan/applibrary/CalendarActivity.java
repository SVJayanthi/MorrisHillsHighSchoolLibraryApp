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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sravan.applibrary.objects.Event;
import com.example.sravan.applibrary.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

//Displays list of events that the user is interested or responsible for
public class CalendarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarAdapter.ListItemClickListener{

    //Instantiate objects
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mCalendarDatabaseReference;
    private StorageReference mStorageRef;


    private CalendarAdapter mAdapterCalendarDisplay;
    private RecyclerView mCalendarDisplay;

    private String userId;
    private int userGrade;

    private ImageView imageUser;
    private TextView nameUser;
    private TextView emailUser;
    private TextView gradeUser;

    private Toast mToast;
    private final static String TAG = CalendarActivity.class.getSimpleName();

    //Get references for each of the objects
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        mCalendarDatabaseReference = mDatabase.getReference().child("Calendar");


        View content = findViewById(R.id.calendar_app_bar);
        View mainContent = content.findViewById(R.id.include_calendar);
        mCalendarDisplay = (RecyclerView) mainContent.findViewById(R.id.calendar_display);


        LinearLayoutManager layoutManagerBooksDisplay = new LinearLayoutManager(this);
        mCalendarDisplay.setLayoutManager(layoutManagerBooksDisplay);


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
        navigation.setSelectedItemId(R.id.navigation_calendar);
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

        setCalendarAdapter();
    }

    private void setUpUser() {

        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);

        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                userGrade = currentUser.getGrade();

                nameUser.setText(currentUser.getName());
                emailUser.setText(currentUser.getEmail());
                gradeUser.setText("Grade " + Integer.toString(userGrade));
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



    private List<Event> mEvents = new ArrayList<>();
    private int count;


    private void setCalendarAdapter() {
        mEvents.clear();
        count = 0;

        if (userGrade > 12) {
            mCalendarDatabaseReference.orderByChild("userKey").limitToFirst(5).startAt("teacher").endAt("teacher" + "\uf8ff").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Event event = dataSnapshot.getValue(Event.class);

                    if (count != 0) {
                        if (event == mEvents.get(0)) {
                            setEventsCalendarAdapter();
                            return;
                        }
                    }

                    mEvents.add(event);
                    count++;
                    if (count > 4) {
                        setEventsCalendarAdapter();
                        return;
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            mCalendarDatabaseReference.orderByChild("userKey").limitToFirst(5).startAt("all").endAt("all" + "\uf8ff").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Event event = dataSnapshot.getValue(Event.class);

                    if (count != 0) {
                        if (event == mEvents.get(0)) {
                            setEventsCalendarAdapter();
                            return;
                        }
                    }

                    mEvents.add(event);
                    count++;
                    if (count > 4) {
                        setEventsCalendarAdapter();
                        return;
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void setEventsCalendarAdapter() {
        //Set the display adapter for the recycler view and query from server
        mAdapterCalendarDisplay = new CalendarAdapter(this, this, mEvents, mEvents.size());
        mCalendarDisplay.setAdapter(mAdapterCalendarDisplay);

    }


    @Override
    public void onStop() {
        super.onStop();


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
            Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I found an app for the library at school." +
                    " I recommend you download it and get started on reading some books!");
            startActivity(Intent.createChooser(intent, "Share"));
        } else if (id == R.id.nav_instructions) {
            Intent intent = new Intent(CalendarActivity.this, Instructions.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(CalendarActivity.this, Resources.class);
            startActivity(intent);
        } else if (id == R.id.nav_liscence) {
            Intent intent = new Intent(CalendarActivity.this, Liscence.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(CalendarActivity.this, Report.class);
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
                    Intent sendMain = new Intent(CalendarActivity.this, MainActivity.class);
                    startActivity(sendMain);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(CalendarActivity.this, BooksActivity.class);
                    startActivity(sendBooks);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(CalendarActivity.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    //Intent sendCalendar = new Intent(CalendarActivity.this, CalendarActivity.class);
                    //startActivity(sendCalendar);


                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(CalendarActivity.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

    @Override
    public void onListItemClick(int clickItemIndex, String key) {
        Intent intent;

        if (Character.toString(key.charAt(0)) == "-") {
            intent = new Intent(CalendarActivity.this, BookReturn.class);
        } else {
            intent = new Intent(CalendarActivity.this, CalendarDetail.class);
        }

        intent.putExtra("Key", key);

        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item " + key + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();

        startActivity(intent);
    }

}