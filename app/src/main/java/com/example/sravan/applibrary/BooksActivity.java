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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sravan.applibrary.objects.Book;
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
import java.util.Objects;

//Displays list of books based off user search request
public class BooksActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BooksListAdapter.ListItemClickListener, OnItemSelectedListener{

    //Instantiate objects
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mBooksDatabaseReference;
    private StorageReference mStorageRef;


    private BooksListAdapter mAdapterBooksDisplay;
    private RecyclerView mBooksDisplay;

    private Spinner spinner;

    private ImageView imageUser;
    private TextView nameUser;
    private TextView emailUser;
    private TextView gradeUser;

    private String searchCategory;
    private int userGrade;
    private String userId;

    //List of possible search choices
    private String[] bookOrder = {"Author", "Book Title", "Trending", "Recommended", "Available", "Avg. Rating"};

    private Toast mToast;
    private final static String TAG = BooksActivity.class.getSimpleName();

    //Get references for each of the objects
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        searchCategory = "author";

        if (getIntent().getStringExtra("Search")!= null) {
            searchCategory = getIntent().getStringExtra("Search");
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();


        mBooksDatabaseReference = mDatabase.getReference().child("Library");
        mStorageRef = mStorage.getReference();


        View content = findViewById(R.id.books_app_bar);
        View mainContent = content.findViewById(R.id.include_books);
        spinner = (Spinner) mainContent.findViewById(R.id.book_order);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.book_order, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        mBooksDisplay = (RecyclerView) findViewById(R.id.books_display);


        GridLayoutManager layoutManagerBooksDisplay = new GridLayoutManager(this, 2);
        mBooksDisplay.setLayoutManager(layoutManagerBooksDisplay);


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
        navigation.setSelectedItemId(R.id.navigation_books);
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
        setBooksAdapter();

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

                userGrade = currentUser.getGrade();

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

    private List<Book> mBooks = new ArrayList<>();
    private int count = 0;


    private void setBooksAdapter() {
        mBooks.clear();

        if (userGrade >12 ) {
            userGrade = 12;
        }
        int higherGrade;
        if (userGrade > 11) {
            higherGrade = 11;
        } else {
            higherGrade = userGrade + 1;
        }

        if (Objects.equals(searchCategory, "trending")) {
            mBooksDatabaseReference.orderByChild("timestamp").limitToLast(17).
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);


                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count > 16) {
                                setBookAdapter();
                                count = 0;
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

        } else if (Objects.equals(searchCategory, "recommended")) {
            mBooksDatabaseReference.orderByChild("targetGrade").startAt(userGrade).endAt(higherGrade).
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count > 6) {
                                setBookAdapter();
                                count = 0;
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

        } else if (Objects.equals(searchCategory, "available")) {
            mBooksDatabaseReference.orderByChild("bookOut").limitToFirst(17).
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count > 16) {
                                setBookAdapter();
                                count = 0;
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

        } else if (Objects.equals(searchCategory, "rating")) {
            mBooksDatabaseReference.orderByChild("bookReviews").limitToLast(17).
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);


                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count > 16) {
                                setBookAdapter();
                                count = 0;
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

        } else if (Objects.equals(searchCategory, "author")) {
            mBooksDatabaseReference.orderByChild("author").limitToFirst(17).
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }

                            mBooks.add(book);


                            count++;
                            if (count > 16) {
                                setBookAdapter();
                                count = 0;
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
        } else if (Objects.equals(searchCategory, "authorAD")) {
            mBooksDatabaseReference.orderByChild("author").startAt("A").endAt("E").
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count >= 3) {
                                setBookAdapter();
                                count = 0;
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
        } else if (Objects.equals(searchCategory, "authorEK")) {
            mBooksDatabaseReference.orderByChild("author").startAt("E").endAt("L").
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count > 3) {
                                setBookAdapter();
                                count = 0;
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
        } else if (Objects.equals(searchCategory, "authorLR")) {
            mBooksDatabaseReference.orderByChild("author").startAt("L").endAt("S").
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count >= 3) {
                                setBookAdapter();
                                count = 0;
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
        } else if (Objects.equals(searchCategory, "authorSZ")) {
            mBooksDatabaseReference.orderByChild("author").startAt("S").endAt("ZZ").
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);


                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count >= 3) {
                                setBookAdapter();
                                count = 0;
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

        } else if (Objects.equals(searchCategory, "title")) {
            mBooksDatabaseReference.orderByChild("title").limitToFirst(17).
                    addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                            Book book = dataSnapshot.getValue(Book.class);

                            if (count != 0 ) {
                                if (book == mBooks.get(0)) {
                                    setBookAdapter();
                                    count = 0;
                                    return;
                                }
                            }
                            mBooks.add(book);

                            count++;
                            if (count > 16) {
                                setBookAdapter();
                                count = 0;
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

    //Set adapter for the book display
    private void setBookAdapter() {
        //Set the display adapter for the Recycler View, will also query from server
        mAdapterBooksDisplay = new BooksListAdapter(this, this, mBooks, mBooks.size());
        mBooksDisplay.setAdapter(mAdapterBooksDisplay);

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
            Intent intent = new Intent(BooksActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I found an app for the library at school." +
                    " I recommend you download it and get started on reading some books!");
            startActivity(Intent.createChooser(intent, "Share"));
        } else if (id == R.id.nav_instructions) {
            Intent intent = new Intent(BooksActivity.this, Instructions.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(BooksActivity.this, Resources.class);
            startActivity(intent);
        } else if (id == R.id.nav_liscence) {
            Intent intent = new Intent(BooksActivity.this, Liscence.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(BooksActivity.this, Report.class);
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
                    Intent sendMain = new Intent(BooksActivity.this, MainActivity.class);
                    startActivity(sendMain);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

                    return true;
                case R.id.navigation_books:
                    //Intent sendBooks = new Intent(BooksActivity.this, BooksActivity.class);
                    //startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(BooksActivity.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(BooksActivity.this, CalendarActivity.class);
                    startActivity(sendCalendar);


                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(BooksActivity.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

    @Override
    public void onListItemClick(String clickedBook) {

        Intent intent = new Intent(BooksActivity.this, BooksDetail.class);
        intent.putExtra("Identification", clickedBook);


        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item " + clickedBook + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();

        startActivity(intent);

    }

    //Handles item clicks to change search query
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String category = bookOrder[pos];
        if(Objects.equals(category, bookOrder[0])) {
            searchCategory = "author";
        }
        if(Objects.equals(category, bookOrder[1])) {
            searchCategory = "title";
        }
        if(Objects.equals(category, bookOrder[2])) {
            searchCategory = "trending";
        }
        if(Objects.equals(category, bookOrder[3])) {
            searchCategory = "recommended";
        }
        if(Objects.equals(category, bookOrder[4])) {
            searchCategory = "available";
        }
        if(Objects.equals(category, bookOrder[5])) {
            searchCategory = "rating";
        }

        setBooksAdapter();
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }
}