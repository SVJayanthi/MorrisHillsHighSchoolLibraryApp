package com.example.sravan.applibrary;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.sravan.applibrary.objects.Book;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;


//Home activity that will display a synopsis of important information for the user
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalendarAdapter.ListItemClickListener, BooksListAdapter.ListItemClickListener{

    //Instantiate objects
    //Google Firebase Database objects
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mBooksDatabaseReference;
    private DatabaseReference mCalendarDatabaseReference;
    private StorageReference mStorageRef;

    private String userId;

    private BooksListAdapter mAdapterRecommendedBooks;
    private BooksListAdapter mAdapterTrendingBooks;
    private CalendarAdapter mAdapterEventsCalendar;
    private RecyclerView mRecommendedBooks;
    private RecyclerView mTrendingBooks;
    private RecyclerView mEventsCalendar;

    private Button mMore1;
    private Button mMore2;
    private Button mMore3;
    private ImageView imageUser;
    private TextView nameUser;
    private TextView emailUser;
    private TextView gradeUser;


    private int userGrade;

    private Toast mToast;
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int RC_SIGN_IN = 2048;


    //Set up references for objects
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        View content = findViewById(R.id.main_app_bar);
        View mainContent = content.findViewById(R.id.include_main);

        mMore1 = (Button) mainContent.findViewById(R.id.bt_recommended_books);
        mMore2 = (Button) mainContent.findViewById(R.id.bt_trending_books);
        mMore3 = (Button) mainContent.findViewById(R.id.bt_events_calendar);


        mMore1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BooksActivity.class);
                intent.putExtra("Search", "recommended");
                startActivity(intent);
            }
        });

        mMore2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BooksActivity.class);
                intent.putExtra("Search", "trending");
                startActivity(intent);
            }
        });

        mMore3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });


        //Get server databse references
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mBooksDatabaseReference = mDatabase.getReference().child("Library");
        mCalendarDatabaseReference = mDatabase.getReference().child("Calendar");
        mStorageRef = mStorage.getReference();

        mRecommendedBooks = (RecyclerView) findViewById(R.id.recommended_books);
        mTrendingBooks = (RecyclerView) findViewById(R.id.trending_books);
        mEventsCalendar = (RecyclerView) findViewById(R.id.events_calendar);


        //Set up layout managers for recycler views
        LinearLayoutManager layoutManagerRecommendedBooks = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecommendedBooks.setLayoutManager(layoutManagerRecommendedBooks);

        LinearLayoutManager layoutManagerTrendingBooks = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mTrendingBooks.setLayoutManager(layoutManagerTrendingBooks);

        LinearLayoutManager layoutManagerEventsCalendar = new LinearLayoutManager(this);
        mEventsCalendar.setLayoutManager(layoutManagerEventsCalendar);



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
        navigation.setSelectedItemId(R.id.navigation_home);
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

        //Utilize user information on server database to tailor to unique user experience
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        setUpUser();

        setRecommendedAdapter();
        setTrendingAdapter();
        setCalendarAdapter();


        Log.d(TAG, "Start Complete");

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
                gradeUser.setText("Grade " + Integer.toString(currentUser.getGrade()));
                putUserImage();

                Log.d(TAG, "User is: " + currentUser.getName());
                Log.d(TAG, "User Grade is: " + userGrade);
                Log.d(TAG, "UserId is: " + userId);
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

    private List<Book> mRBooks = new ArrayList<>();
    private int count1;


    private void setRecommendedAdapter() {
        Log.d(TAG, "Enter Recommended");
        count1 = 0;
        if (userGrade > 12 ) {
            userGrade = 12;
        }

        mRBooks.clear();
        mBooksDatabaseReference.orderByChild("targetGrade").limitToFirst(5).startAt(userGrade).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAddedR:" + dataSnapshot.getKey());

                        Book book = dataSnapshot.getValue(Book.class);

                        mRBooks.add(book);

                        count1++;

                        if (count1 > 4) {
                            setRecommendedBookAdapter();
                            count1 = 0;
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

    private List<Book> mTBooks = new ArrayList<>();
    private int count2;


    private void setTrendingAdapter() {
        Log.d(TAG, "Enter Trending");
        count2 = 0;
        mTBooks.clear();
        mBooksDatabaseReference.orderByChild("timestamp").limitToLast(5).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAddedT:" + dataSnapshot.getKey());

                        Book book = dataSnapshot.getValue(Book.class);

                        mTBooks.add(book);

                        count2++;

                        if (count2 > 4) {
                            setTrendingBookAdapter();
                            count2 = 0;
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

    private List<Event> mEvents = new ArrayList<>();
    private int count3;


    private void setCalendarAdapter() {
        mEvents.clear();
        count3 = 0;
        Log.d(TAG, "Enter Calendar");


        if (userGrade > 12) {

            mCalendarDatabaseReference.orderByChild("userKey").startAt("teacher").endAt("teacher" + "\uf8ff").limitToFirst(5).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildAddedC:" + dataSnapshot.getKey());
                    Event event = dataSnapshot.getValue(Event.class);

                    mEvents.add(event);
                    count3++;
                    if (count3 > 4) {
                        setEventsCalendarAdapter();
                        count3 = 0;
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

            mCalendarDatabaseReference.orderByChild("userKey").startAt("all").endAt("all" + "\uf8ff").limitToFirst(5).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, "onChildAddedC:" + dataSnapshot.getKey());
                    Event event = dataSnapshot.getValue(Event.class);

                    mEvents.add(event);
                    count3++;
                    if (count3 > 4) {
                        setEventsCalendarAdapter();
                        count3 = 0;
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

    //Set adapters for each of the book/calendar displays
    private void setRecommendedBookAdapter() {
        Log.d(TAG, "Recommended Adapter");
        mAdapterRecommendedBooks = new BooksListAdapter(this, this, mRBooks, mRBooks.size());
        mRecommendedBooks.setAdapter(mAdapterRecommendedBooks);

    }

    private void setTrendingBookAdapter() {
        Log.d(TAG, "Trending Adapter");
        mAdapterTrendingBooks = new BooksListAdapter(this, this, mTBooks, mTBooks.size());
        mTrendingBooks.setAdapter(mAdapterTrendingBooks);

    }

    private void setEventsCalendarAdapter() {
        Log.d(TAG, "Calendar Adapter");
        mAdapterEventsCalendar = new CalendarAdapter(this, this, mEvents, mEvents.size());
        mEventsCalendar.setAdapter(mAdapterEventsCalendar);

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
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I found an app for the library at school." +
                    " I recommend you download it and get started on reading some books!");
            startActivity(Intent.createChooser(intent, "Share"));
        } else if (id == R.id.nav_instructions) {
            Intent intent = new Intent(MainActivity.this, Instructions.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(MainActivity.this, Resources.class);
            startActivity(intent);
        } else if (id == R.id.nav_liscence) {
            Intent intent = new Intent(MainActivity.this, Liscence.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(MainActivity.this, Report.class);
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
                    //Intent sendMain = new Intent(MainActivity.this, MainActivity.class);
                    //startActivity(sendMain);

                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(MainActivity.this, BooksActivity.class);
                    startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(MainActivity.this, CalendarActivity.class);
                    startActivity(sendCalendar);

                    return true;

                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(MainActivity.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

    @Override
    public void onListItemClick(String clickedBook) {

        Intent intent = new Intent(MainActivity.this, BooksDetail.class);
        intent.putExtra("Identification", clickedBook);

        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item " + clickedBook + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();

        startActivity(intent);

    }

    @Override
    public void onListItemClick(int clickItemIndex, String key) {
        Intent intent = new Intent(MainActivity.this, CalendarDetail.class);
        intent.putExtra("Key", key);

        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Event clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();

        startActivity(intent);
    }
}