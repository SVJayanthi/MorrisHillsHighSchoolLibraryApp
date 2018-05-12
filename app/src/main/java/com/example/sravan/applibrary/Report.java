package com.example.sravan.applibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.sravan.applibrary.objects.Comments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sravan on 1/18/2018.
 */

//Allow user to send report about the app and any possible bugs it may contain
public class Report extends AppCompatActivity {


    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mCommentsDatabaseReference;

    private CommentAdapter mCommentDisplay;
    private RecyclerView mCommentList;

    private EditText mReport;

    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mReport = (EditText) findViewById(R.id.report_edit);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mSubmit = (Button) findViewById(R.id.submit);

        //Information submitted by user will be stored on the server database
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference mReportChild = mDatabase.child("Report").push();

                String key = mReportChild.getKey();

                String response = mReport.getText().toString();

                mDatabase.child("Report").child(key).child("Info").setValue(response);

            }
        });

        mCommentsDatabaseReference = mDatabase.child("Comments");

        mCommentList = (RecyclerView) findViewById(R.id.comment_review);

        LinearLayoutManager layoutManagerCommentsDisplay = new LinearLayoutManager(this);
        mCommentList.setLayoutManager(layoutManagerCommentsDisplay);
    }


    @Override
    public void onStart() {
        super.onStart();
        setCommentsAdapter();

    }

    private List<Comments> mComments = new ArrayList<>();
    private int count;

    private void setCommentsAdapter() {

        mComments.clear();

        mCommentsDatabaseReference.orderByChild("book").startAt("problem").endAt("problems").limitToFirst(2).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Comments comments = dataSnapshot.getValue(Comments.class);


                        if (count > 1) {
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


}
