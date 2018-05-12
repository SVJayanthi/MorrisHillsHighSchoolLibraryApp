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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.sravan.applibrary.objects.Book;
import com.example.sravan.applibrary.objects.Comments;
import com.example.sravan.applibrary.objects.User;
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
import java.util.ArrayList;
import java.util.List;

//Displays details for individual book and allows user to share it
public class BooksDetail extends AppCompatActivity
        implements BooksListAdapter.ListItemClickListener {

    //Instantiate objects
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mBooksDatabaseReference;
    private DatabaseReference mCommentsDatabaseReference;
    private DatabaseReference mCartDatabase;
    private StorageReference mStorageRef;


    private BooksListAdapter mBooksDisplay;
    private RecyclerView mBooksList;
    private CommentAdapter mCommentDisplay;
    private RecyclerView mCommentList;

    private TextView title;
    private TextView author;
    private TextView description;
    private TextView available;
    private RatingBar ratingBar;
    private ProgressBar progressBar;
    private ImageView mImage;

    private Book mBookUser;

    //Interactive items
    private Button checkout;
    private Button reserve;
    private Button share;

    private String id;
    private String userId;
    private int grade;

    private Toast mToast;
    private final static String TAG = BooksDetail.class.getSimpleName();

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        id = getIntent().getStringExtra("Identification");
        Log.d(TAG, "Book Id is: " + id);

        View content = findViewById(R.id.book_app_bar);
        View mainContent = content.findViewById(R.id.include_content_book);
        title = (TextView) mainContent.findViewById(R.id.book_title);
        author = (TextView) mainContent.findViewById(R.id.book_author);
        description = (TextView) mainContent.findViewById(R.id.book_description);
        available = (TextView) mainContent.findViewById(R.id.book_available);
        ratingBar = (RatingBar) mainContent.findViewById(R.id.book_rating);
        progressBar = (ProgressBar) mainContent.findViewById(R.id.book_progress);
        mImage = (ImageView) mainContent.findViewById(R.id.book_image);

        checkout = (Button) mainContent.findViewById(R.id.check_out);
        reserve = (Button) mainContent.findViewById(R.id.reserve);
        share = (Button) mainContent.findViewById(R.id.share);

        //Set onClickListener for each of the interactive items to be able to checkout, return, reserve, or share the book
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOut();
            }
        });
        reserve.setOnClickListener(new View.OnClickListener() {
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

        mBooksDatabaseReference = mDatabase.getReference().child("Library");
        mCommentsDatabaseReference = mDatabase.getReference().child("Comments");
        mCartDatabase = mDatabase.getReference().child("Cart");
        mStorageRef = mStorage.getReference();

        mBooksList = (RecyclerView) findViewById(R.id.books_similar);
        mCommentList = (RecyclerView) findViewById(R.id.comment_review);

        GridLayoutManager layoutManagerBooksDisplay = new GridLayoutManager(this, 2);
        mBooksList.setLayoutManager(layoutManagerBooksDisplay);

        LinearLayoutManager layoutManagerCommentsDisplay = new LinearLayoutManager(this);
        mCommentList.setLayoutManager(layoutManagerCommentsDisplay);

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
                description.setText(book.getDescription());
                int availablen = book.getBookNumbers() - book.getBookOut();
                String availibility = Integer.toString(availablen);
                available.setText("Available- " + availibility);
                int star = 0;
                if (book.getBookReviews()!=0) {
                    star = (book.getBookStars()) / (book.getBookReviews());
                }
                ratingBar.setNumStars(star);
                progressBar.setMax(book.getBookNumbers());
                progressBar.setProgress(book.getBookOut());
                grade = book.getTargetGrade();
                placeImage();

                setBooksAdapter();
                setCommentsAdapter();

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


    private List<Book> mBook = new ArrayList<>();
    private int count;

    private void setBooksAdapter() {
        mBook.clear();
        int higherGrade;
        if (grade > 11) {
            grade = 12;
            higherGrade =12;
        } else {
            higherGrade = grade + 1;
        }

        mBooksDatabaseReference.orderByChild("targetGrade").limitToFirst(4).startAt(grade).endAt(higherGrade).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                        Book book = dataSnapshot.getValue(Book.class);

                        if (book.getTitle() != mBookUser.getTitle()) {
                            mBook.add(book);

                            count++;
                        }


                        if (count > 3) {
                            setBookAdapter();
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



    //Set adapter for the book display
    private void setBookAdapter() {
        //Set the display adapter for the Recycler View, will also query from server
        mBooksDisplay = new BooksListAdapter(this, this, mBook, mBook.size());
        mBooksList.setAdapter(mBooksDisplay);
    }



    private List<Comments> mComments = new ArrayList<>();
    private int count1;

    private void setCommentsAdapter() {

        mComments.clear();

        mCommentsDatabaseReference.orderByChild("book").startAt(id).endAt(id + "\uf8ff").limitToFirst(2).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                        Comments comments = dataSnapshot.getValue(Comments.class);

                        mComments.add(comments);
                        count1++;

                        if (count1 > 1) {
                            setCommentAdapter();
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



    //Set adapter for the book display
    private void setCommentAdapter() {
        //Set the display adapter for the Recycler View, will also query from server
        mCommentDisplay = new CommentAdapter(this, mComments, mComments.size());
        mCommentList.setAdapter(mCommentDisplay);
    }


    //Check out a book that the user plans on reading by adding event to server
    private void checkOut() {

        if(mBookUser.getBookOut() + 1 > mBookUser.getBookNumbers()){
            if (mToast != null) {
                mToast.cancel();
            }

            String toastMessage = "All copies of " + mBookUser.getTitle() + " are currently out.";
            mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
            mToast.show();
            Intent intent = new Intent(BooksDetail.this, BooksReserve.class);
            intent.putExtra("Identification", mBookUser.getTitle());
            startActivity(intent);
        } else {
            addBook();

            Long tsLong = System.currentTimeMillis()/1000;
            Long date = tsLong + 2000000;
            String dateRet = date.toString();

            DatabaseReference childDatabase = mCartDatabase.child(dateRet);

            int dateI = date.intValue();
            mBookUser.setTimestamp(dateI);
            mBookUser.setAuthor(userId);

            childDatabase.setValue(mBookUser);

            Intent intent = new Intent(BooksDetail.this, BackpackActivity.class);
            startActivity(intent);
        }
    }


    private void addBook() {
        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);

        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                int books = currentUser.getBooks();

                int newBooks = books + 1;
                mUserDatabaseReference.child("books").setValue(newBooks);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.");

            }
        });
    }

    private void reservation() {

        Intent intent = new Intent(BooksDetail.this, BooksReserve.class);
        intent.putExtra("Identification", mBookUser.getTitle());
        startActivity(intent);
    }


    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public void onListItemClick(String clickedBook) {

        Intent intent = new Intent(BooksDetail.this, BooksDetail.class);
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
                    Intent sendMain = new Intent(BooksDetail.this, MainActivity.class);
                    startActivity(sendMain);

                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(BooksDetail.this, BooksActivity.class);
                    startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(BooksDetail.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(BooksDetail.this, CalendarActivity.class);
                    startActivity(sendCalendar);

                    return true;
                case R.id.navigation_backpack:
                    Intent sendBag = new Intent(BooksDetail.this, BackpackActivity.class);
                    startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

}
