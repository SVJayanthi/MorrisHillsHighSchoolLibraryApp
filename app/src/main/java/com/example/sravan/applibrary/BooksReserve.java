package com.example.sravan.applibrary;

/**
 * Created by Sravan on 1/17/2018.
 */

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.sravan.applibrary.objects.Book;
import com.example.sravan.applibrary.objects.Event;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//Displays details for reserving a book and allows user to share it
public class BooksReserve extends AppCompatActivity
        implements  BooksListAdapter.ListItemClickListener {

    //Instantiate objects
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mBooksDatabaseReference;
    private DatabaseReference mCalendarDatabaseReference;

    private DatePickerDialog.OnDateSetListener mDateSetListener;


    private TextView title;
    private TextView author;
    private TextView available;
    private ProgressBar progressBar;
    private ImageView mImage;

    private Book mBookUser;

    //Interactive items
    private Button reserve;
    private Button complete;
    private TextView reserveText;
    private Button share;

    private String id;
    private String userId;

    private Toast mToast;
    private final static String TAG = BooksReserve.class.getSimpleName();

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reserve);

        id = getIntent().getStringExtra("Identification");
        Log.d(TAG, "Book Id is: " + id);

        title = (TextView) findViewById(R.id.reserve_booktitle);
        author = (TextView) findViewById(R.id.reserve_book_author);
        available = (TextView) findViewById(R.id.reserve_book_available);
        progressBar = (ProgressBar) findViewById(R.id.reserve_book_progress);
        mImage = (ImageView) findViewById(R.id.reserve_book_image);

        reserveText = (TextView) findViewById(R.id.reserve_date);

        reserve = (Button) findViewById(R.id.reserve_reserve);
        complete = (Button) findViewById(R.id.reserve_complete);
        share = (Button) findViewById(R.id.reserve_share);

        //Set onClickListener for each of the interactive items to be able to checkout, return, reserve, or share the book
        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        BooksReserve.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = year + "-" + month + "-" + day;
                reserveText.setText(date);
            }
        };

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reservation();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);

                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I am looking at " + id + ". It " +
                        "seems interesting and I would love to read it!");
                startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mStorageRef = mStorage.getReference();
        mBooksDatabaseReference = mDatabase.getReference().child("Library");
        mCalendarDatabaseReference = mDatabase.getReference().child("Calendar");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
    //Check out a book that the user plans on reading by adding event to server
    private void reservation() {
        DatabaseReference childDatabase = mCalendarDatabaseReference.push();
        String key = childDatabase.getKey();
        Date date = parseDate(reserveText.getText().toString());
        long time = (date.getTime()/1000) + 20000;
        Event event = new Event(mBookUser.getTitle(), Long.toString(time), "Book Reservation", mBookUser.getTitle(), key, "Front of Library", userId);
        childDatabase.setValue(event);

        Intent intent = new Intent(BooksReserve.this, BookReturn.class);
        intent.putExtra("Key", key);
        startActivity(intent);
    }

    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        DatabaseReference mBookReference = mBooksDatabaseReference.child(id);

        mBookReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.getValue(Book.class);
                mBookUser = book;

                title.setText(book.getTitle());
                author.setText(book.getAuthor());
                int availablen = book.getBookNumbers() - book.getBookOut();
                String availibility = Integer.toString(availablen);
                available.setText("Available- " + availibility);
                int star = 0;
                if (book.getBookReviews()!=0) {
                    star = (book.getBookStars()) / (book.getBookReviews());
                }
                progressBar.setMax(book.getBookNumbers());
                progressBar.setProgress(book.getBookOut());
                placeImage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void placeImage() {
        String positioning = (mBookUser.getTitle()).replace(' ', '-').toLowerCase();
        StorageReference photoRef = mStorageRef.child("users/" + positioning + ".jpg");

        GlideApp.with(this)
                .load(photoRef)
                .into(mImage);
    }



    @Override
    public void onStop() {
        super.onStop();

    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(BooksReserve.this, BooksDetail.class);
        intent.putExtra("Identification", mBookUser.getTitle());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

    }

    @Override
    public void onListItemClick(String clickedBook) {

        Intent intent = new Intent(BooksReserve.this, BooksReserve.class);
        intent.putExtra("Identification", clickedBook);

        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item " + clickedBook + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();

        startActivity(intent);

    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent sendMain = new Intent(BooksReserve.this, MainActivity.class);
                    startActivity(sendMain);

                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(BooksReserve.this, BooksActivity.class);
                    startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(BooksReserve.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(BooksReserve.this, CalendarActivity.class);
                    startActivity(sendCalendar);

                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(BooksReserve.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

}
