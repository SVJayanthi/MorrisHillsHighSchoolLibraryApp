package com.example.sravan.applibrary;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.sravan.applibrary.objects.Book;
import com.example.sravan.applibrary.objects.Comments;
import com.example.sravan.applibrary.objects.Event;
import com.example.sravan.applibrary.objects.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//Shows the book currently checked out and allows user to return the book
public class BookReturn extends AppCompatActivity{

    //Instantiate objects
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mBooksDatabaseReference;
    private DatabaseReference mCalendarDatabaseReference;
    private DatabaseReference mCommentsDatabaseReference;

    private TextView eventTitle;
    private TextView title;
    private TextView bookR;

    //Interactive items
    private Button returnBook;
    private Button share;

    private TextView reviewTitle;
    private TextView reviewText;
    private RatingBar ratingBarUser;
    private Button submit;

    private ImageView mImage;

    private String id;
    private String userId;

    private Event mEvent;

    private Toast mToast;
    private final static String TAG = BooksDetail.class.getSimpleName();

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_return);

        id = getIntent().getStringExtra("Key");
        Log.d(TAG, "Event Id is: " + id);

        eventTitle = (TextView) findViewById(R.id.book_return);
        title = (TextView) findViewById(R.id.book_return_title);
        bookR = (TextView) findViewById(R.id.book_return_date);

        returnBook = (Button) findViewById(R.id.return_book);
        share = (Button) findViewById(R.id.share_return);


        reviewTitle = (TextView) findViewById(R.id.user_review);
        reviewText = (TextView) findViewById(R.id.user_comment);
        ratingBarUser = (RatingBar) findViewById(R.id.book_rating_bar);
        submit = (Button) findViewById(R.id.review_submit);


        mImage = (ImageView) findViewById(R.id.return_book_image);

        //Set onClickListener for each of the interactive items to be able to checkout, return, reserve, or share the book
        returnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnB();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);

                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I am reading this new novel and am loving it!");
                startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        //Set onRatingBarChangeListener to handle when the user reviews the book
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitComment();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mStorageRef = mStorage.getReference();
        mBooksDatabaseReference = mDatabase.getReference().child("Library");
        mCalendarDatabaseReference = mDatabase.getReference().child("Calendar");
        mCommentsDatabaseReference = mDatabase.getReference().child("Comments");



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_backpack);
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
        DatabaseReference mBagReference = mCalendarDatabaseReference.child(id);

        mBagReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);

                mEvent = event;

                eventTitle.setText(event.getEvent());
                title.setText(event.getBookKey());
                java.util.Date time = new java.util.Date((long) Long.parseLong(event.getDate())*1000);
                bookR.setText(time.toString());
                placeImage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    private void placeImage() {
        String positioning = (mEvent.getBookKey()).replace(' ', '-').toLowerCase();
        StorageReference photoRef = mStorageRef.child("users/" + positioning + ".jpg");


        GlideApp.with(this)
                .load(photoRef)
                .into(mImage);
    }


    int booksOut;
    int starsTot;
    int starRating;


    //Take the current rating statistics and add the recent user rating to the total
    private void submitComment() {
        DatabaseReference bookRef = mBooksDatabaseReference.child(mEvent.getBookKey());

        bookRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.getValue(Book.class);
                starsTot = book.getBookStars();
                starRating = book.getBookReviews();
                int stars = Math.round(ratingBarUser.getRating());
                int newStarTot = starsTot + stars;
                int newStarRating = starRating + 1;
                mBooksDatabaseReference.child(book.getTitle()).child("bookStars").setValue(newStarTot);
                mBooksDatabaseReference.child(book.getTitle()).child("bookReviews").setValue(newStarRating);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        int numStars = (int) ratingBarUser.getRating();
        String text = reviewText.getText().toString();
        String title = reviewTitle.getText().toString();

        Long tsLong = System.currentTimeMillis()/1000;
        int date = (int) (tsLong + 2000000);

        Comments newComment = new Comments(userId, id, numStars, text, date, title);
        DatabaseReference childDatabase = mCommentsDatabaseReference.push();

        childDatabase.setValue(newComment);

        String toastMessage = "Item rated for " + numStars + " stars.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();

    }

    //Return a book that was borrowed by removing event from server
    private void returnB() {
        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);

        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                int cart = currentUser.getCart();

                int newCart = cart - 1;
                mUserDatabaseReference.child("cart").setValue(newCart);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.");

            }
        });
        DatabaseReference bookRef = mBooksDatabaseReference.child(mEvent.getBookKey());
        int newBooksOut = booksOut - 1;
        bookRef.child("bookOut").setValue(newBooksOut);
        Long tsLong = System.currentTimeMillis()/1000;
        int timestamp = tsLong.intValue();
        bookRef.child("timestamp").setValue(timestamp);


        mCalendarDatabaseReference.child(mEvent.getKey()).removeValue();

        startActivity(new Intent(BookReturn.this, BackpackActivity.class));
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent sendMain = new Intent(BookReturn.this, MainActivity.class);
                    startActivity(sendMain);

                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(BookReturn.this, BooksActivity.class);
                    startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(BookReturn.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(BookReturn.this, CalendarActivity.class);
                    startActivity(sendCalendar);

                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(BookReturn.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

}

