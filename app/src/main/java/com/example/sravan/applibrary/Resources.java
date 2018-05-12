package com.example.sravan.applibrary;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.sravan.applibrary.objects.Book;
import com.example.sravan.applibrary.objects.Comments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

//Collection of select library resources for users to conduct research
public class Resources extends AppCompatActivity{

    //Instantiate objects
    private ImageButton button1;
    private ImageButton button2;
    private ImageButton button3;

    private Toast mToast;
    private final static String LOG_TAG = Resources.class.getSimpleName();

    //Get references for objects
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);


        View content = findViewById(R.id.resources_app_bar);
        View mainContent = content.findViewById(R.id.include_resources);
        button1 = (ImageButton) mainContent.findViewById(R.id.imageButton);
        button2 = (ImageButton) mainContent.findViewById(R.id.imageButton1);
        button3 = (ImageButton) mainContent.findViewById(R.id.imageButton2);

        //Sends user to hyperlinks to access resources
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlAsString = "https://www.gale.com/";
                openWebPage(urlAsString);

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlAsString = "http://online.infobaselearning.com/Login.aspx?app=Infobase&returnUrl=/Default.aspx";
                openWebPage(urlAsString);

            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlAsString = "http://www.jstor.org/";
                openWebPage(urlAsString);

            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            itemView.setShiftingMode(false);
            itemView.setChecked(false);
        }

    }




    //Function performs the process of opening a webpage
    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent sendMain = new Intent(Resources.this, MainActivity.class);
                    startActivity(sendMain);

                    return true;
                case R.id.navigation_books:
                    Intent sendBooks = new Intent(Resources.this, BooksActivity.class);
                    startActivity(sendBooks);

                    return true;
                case R.id.navigation_maps:
                    Intent sendMap = new Intent(Resources.this, MapsActivity.class);
                    startActivity(sendMap);

                    return true;
                case R.id.navigation_calendar:
                    Intent sendCalendar = new Intent(Resources.this, CalendarActivity.class);
                    startActivity(sendCalendar);


                    return true;
            }
            return false;
        }
    };
}