package com.example.sravan.applibrary;

/**
 * Created by Sravan on 1/17/2018.
 */

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
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


import com.example.sravan.applibrary.objects.Event;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

//Displays details for individual events and allows user to share event details
public class CalendarDetail extends AppCompatActivity{

    //Instantiate objects
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mCalendarDatabaseReference;
    private StorageReference mStorageRef;

    private TextView mEvent;
    private TextView mDate;
    private TextView mInfo;
    private TextView mLocation;
    private ImageView mImage;

    //Share button_sign_in to send information
    private Button share;

    private String key;
    private Event mUserEvent;

    private Toast mToast;
    private final static String TAG = MainActivity.class.getSimpleName();

    @SuppressLint("RestrictedApi")
    @Override


        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        key = getIntent().getStringExtra("Key");

        View content = findViewById(R.id.event_app_bar);
        View mainContent = content.findViewById(R.id.include_event);
        mEvent = (TextView) mainContent.findViewById(R.id.event);
        mDate = (TextView) mainContent.findViewById(R.id.date);
        mInfo = (TextView) mainContent.findViewById(R.id.info);
        mLocation = (TextView) mainContent.findViewById(R.id.location);
        mImage = (ImageView) mainContent.findViewById(R.id.calendar_image);

        share = (Button) mainContent.findViewById(R.id.share_event);

        //Set onClickListener will share data to source of user choice
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);

                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I have an upcoming event, " + mEvent.getText().toString() +
                        ". I will do it and you should too. Make sure you aren't late!");
                startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();


        mStorageRef = mStorage.getReference();
        mCalendarDatabaseReference = mDatabase.getReference().child("Calendar");



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    //Recieve data from server about event
    @Override
    public void onStart() {
        super.onStart();

        DatabaseReference mCalendarEvent = mCalendarDatabaseReference.child(key);

        mCalendarEvent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);

                mUserEvent = event;

                mEvent.setText(event.getEvent());
                mInfo.setText(event.getEventInfo());
                java.util.Date time = new java.util.Date((long) Long.parseLong(event.getDate())*1000);
                mDate.setText(time.toString());
                mLocation.setText(event.getLocation());
                placeImage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void placeImage() {
        String positioning = (mUserEvent.getBookKey()).replace(' ', '-').toLowerCase();
        StorageReference photoRef = mStorageRef.child("users/" + positioning + ".jpg");


        GlideApp.with(this)
                .load(photoRef)
                .into(mImage);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent sendMain = new Intent(CalendarDetail.this, MainActivity.class);
                    startActivity(sendMain);

                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(CalendarDetail.this, BooksActivity.class);
                    startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(CalendarDetail.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(CalendarDetail.this, CalendarActivity.class);
                    startActivity(sendCalendar);

                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(CalendarDetail.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

}
