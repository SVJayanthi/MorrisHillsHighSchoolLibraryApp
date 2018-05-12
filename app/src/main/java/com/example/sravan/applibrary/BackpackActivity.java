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
import com.google.android.gms.tasks.Tasks;
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

//Activity displays the user's cart and books they have checked out
public class BackpackActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,BooksListAdapter.ListItemClickListener,
        CalendarAdapter.ListItemClickListener {

    //Instantiate objects
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mBooksDatabaseReference;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mCalendarDatabaseReference;
    private DatabaseReference mCartDatabase;
    private StorageReference mStorageRef;


    private BooksListAdapter mAdapterCartDisplay;
    private CalendarAdapter mAdapterBagDisplay;
    private RecyclerView mCartDisplay;
    private RecyclerView mBagDisplay;

    private String userId;
    private int userBooks;
    private int userCart;

    private Button mCheckOut;

    private ImageView imageUser;
    private TextView nameUser;
    private TextView emailUser;
    private TextView gradeUser;

    private Toast mToast;
    private final static String TAG = BooksActivity.class.getSimpleName();

    //Get references for each of the objects
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpack);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mCheckOut = (Button) findViewById(R.id.check_out);

        mBooksDatabaseReference = mDatabase.getReference().child("Library");
        mCalendarDatabaseReference = mDatabase.getReference().child("Calendar");

        mCartDatabase = mDatabase.getReference().child("Cart");
        mStorageRef = mStorage.getReference();

        mCartDisplay = (RecyclerView) findViewById(R.id.books_cart);
        mBagDisplay = (RecyclerView) findViewById(R.id.books_out);


        GridLayoutManager layoutManagerBooksDisplay = new GridLayoutManager(this, 2);
        mCartDisplay.setLayoutManager(layoutManagerBooksDisplay);

        LinearLayoutManager layoutManagerBooksDisplayO = new LinearLayoutManager(this);
        mBagDisplay.setLayoutManager(layoutManagerBooksDisplayO);


        mCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCart.size() > 0) {
                    checkOut();
                }
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
        setUpUser();

    }

    private void setUpUser() {

        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);

        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                userBooks = currentUser.getBooks();
                userCart = currentUser.getCart();


                nameUser.setText(currentUser.getName());
                emailUser.setText(currentUser.getEmail());
                gradeUser.setText("Grade " + Integer.toString(currentUser.getGrade()));
                putUserImage();

                Log.d(TAG, "Cart Number: " + userBooks);
                Log.d(TAG, "Out Number: " + userCart);

                if (userBooks > 0) {
                    setCartsAdapter();
                    Log.d(TAG, "Cart Number: " + userBooks);
                }
                if (userCart > 0) {
                    setEventsAdapter();
                    Log.d(TAG, "Out Number: " + userCart);
                }

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

    private List<Book> mCart = new ArrayList<>();
    private List<Event> mOut = new ArrayList<>();



    private void setCartsAdapter() {
        mCart.clear();

        mCartDatabase.orderByChild("author").limitToFirst(userBooks).startAt(userId).endAt(userId + "\uf8ff").
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                        Book book = dataSnapshot.getValue(Book.class);


                        mCart.add(book);
                        if (mCart.size() > (userBooks - 1)) {
                            setCartAdapter();
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


    private void setEventsAdapter() {
        mOut.clear();

        mCalendarDatabaseReference.orderByChild("userKey").limitToFirst(userCart).startAt(userId).endAt(userId + "\uf8ff").
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                        Event event = dataSnapshot.getValue(Event.class);

                        mOut.add(event);

                        if (mOut.size() > (userCart - 1)) {
                            setEventAdapter();
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

    //Set adapter for the book display
    private void setCartAdapter() {
        //Set the display adapter for the Recycler View, will also query from server
        mAdapterCartDisplay = new BooksListAdapter(this, this, mCart, mCart.size());
        mCartDisplay.setAdapter(mAdapterCartDisplay);
    }

    private void setEventAdapter() {
        mAdapterBagDisplay = new CalendarAdapter(this, this, mOut, mOut.size());
        mBagDisplay.setAdapter(mAdapterBagDisplay);
    }


    private int booksOut;

    private void checkOut() {
        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);
        mUserDatabaseReference.child("books").setValue(0);

        mCartDatabase.orderByChild("author").startAt(userId).endAt(userId+"\uf8ff").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Book book = dataSnapshot.getValue(Book.class);

                int time = book.getTimestamp();
                String timing = Integer.toString(time);
                mCartDatabase.child(timing).removeValue();
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

        String keyMain = null;
        boolean count = true;

        addCartBooks();

        for (int i = 0; i < mCart.size(); i++) {
            DatabaseReference childDatabase = mCalendarDatabaseReference.push();
            String key = childDatabase.getKey();
            if (count) {
                keyMain = key;
                count = false;
            }

            Long tsLong = System.currentTimeMillis()/1000;
            Long date = tsLong + 2000000;
            String dateRet = date.toString();
            Event event = new Event((mCart.get(i)).getTitle(), dateRet, "Return Book", mCart.get(i).getTitle(), key, "Front of Library", userId);

            childDatabase.setValue(event);

            DatabaseReference bookRef = mBooksDatabaseReference.child((mCart.get(i)).getTitle());

            bookRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Book book = dataSnapshot.getValue(Book.class);
                    booksOut = book.getBookOut();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            int newBooksOut = booksOut + 1;
            bookRef.child("bookOut").setValue(newBooksOut);
            int timestamp = tsLong.intValue();
            bookRef.child("timestamp").setValue(timestamp);
        }


        Intent intent = new Intent(BackpackActivity.this, BookReturn.class);
        intent.putExtra("Key", keyMain);
        startActivity(intent);
    }




    private void addCartBooks() {
        mUserDatabaseReference = mDatabase.getReference().child("Users").child(userId);

        mUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                int carts = currentUser.getCart();

                int newCarts = carts + mCart.size();
                mUserDatabaseReference.child("cart").setValue(newCarts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.");

            }
        });
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
            Intent intent = new Intent(BackpackActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "Hey Guys! I found an app for the library at school." +
                    " I recommend you download it and get started on reading some books!");
            startActivity(Intent.createChooser(intent, "Share"));
        } else if (id == R.id.nav_instructions) {
            Intent intent = new Intent(BackpackActivity.this, Instructions.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(BackpackActivity.this, Resources.class);
            startActivity(intent);
        } else if (id == R.id.nav_liscence) {
            Intent intent = new Intent(BackpackActivity.this, Liscence.class);
            startActivity(intent);
        } else if (id == R.id.nav_report) {
            Intent intent = new Intent(BackpackActivity.this, Report.class);
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
                    Intent sendMain = new Intent(BackpackActivity.this, MainActivity.class);
                    startActivity(sendMain);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(BackpackActivity.this, BooksActivity.class);
                    startActivity(sendBooks);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(BackpackActivity.this, MapsActivity.class);
                    startActivity(sendMap);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(BackpackActivity.this, CalendarActivity.class);
                    startActivity(sendCalendar);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);



                    return true;
                case R.id.navigation_backpack:
                    //Intent sendBag = new Intent(BackpackActivity.this, BackpackActivity.class);
                    //startActivity(sendBag);

                    return true;
            }
            return false;
        }
    };

    @Override
    public void onListItemClick(String clickedBook) {

        Intent intent = new Intent(BackpackActivity.this, BooksDetail.class);
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
        Intent intent = new Intent(BackpackActivity.this, BookReturn.class);
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
