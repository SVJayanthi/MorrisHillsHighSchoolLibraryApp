package com.example.sravan.applibrary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Sravan on 1/18/2018.
 */

//Displays instructions to use app
public class Instructions extends AppCompatActivity {
    private TextView instruction;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        instruction = (TextView) findViewById(R.id.instructions);

        instruction.setText("This app is designed to allow for users to interact with a library. Users can reserve, check-out, return, and share books." +
                " In order to login, submit the email and password that was used to register for an account. If the user does not have an account, register for" +
                " a new account by clicking the register button_sign_in. After the user has submitted their information, they will be directed to the home page. The" +
                " home page will display trending and recommended books along with the events calendar. The user can click on any of these to reach the description" +
                " for each of these items. The user can navigate through the different pages through the navigation bar on the bottom of the screen. Then, the user" +
                " can click onto the books display page, which will display books based off a user search preference; the maps page, which will display a map for the user" +
                " to interact with; the calendar page, which will display the upcoming books due and events planned at the library; and the resources page, which will" +
                " provide the user with database resources to conduct research.");
    }
}
