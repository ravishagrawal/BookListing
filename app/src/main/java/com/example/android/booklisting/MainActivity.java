package com.example.android.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Finding Button views and setting OnClickListeners
        ((Button)findViewById(R.id.search_btn)).setOnClickListener(this);
        ((Button)findViewById(R.id.clear_btn)).setOnClickListener(this);

        if( isNetworkStatusAvailable(getApplicationContext())) {
            Toast.makeText(MainActivity.this, "internet available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "internet is not available", Toast.LENGTH_SHORT).show();


        }

    }

    @Override
    public void onClick(View v) {

        //finding views by id and setting to variables
        EditText titleSearchEditText = (EditText) findViewById(R.id.search_title_text);
        EditText authorSearchEditText = (EditText) findViewById(R.id.search_author_text);

        if(v.getId() == R.id.search_btn) {

            //Formatting Strings from EditText views prior to setting corresponding variables.
            String titleString = formatSearchText(titleSearchEditText.getText().toString());
            String authorString = formatSearchText(authorSearchEditText.getText().toString());

            //Displaying toast message if search was pressed without entering text or if text was entered by
            //user, then starting BookReportActivity and sending text entered by user as extras in intent.
            if (titleString.equals("") && authorString.equals("")) {
                Toast.makeText(MainActivity.this, getString(R.string.no_input_toast_message), Toast.LENGTH_LONG).show();
            } else {

                Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
                intent.putExtra("TITLE_SEARCH_STRING", titleString);
                intent.putExtra("AUTHOR_SEARCH_STRING", authorString);
                startActivity(intent);
            }
        }else{

            //Clearing EditText views of any user typed text.
            titleSearchEditText.setText("");
            authorSearchEditText.setText("");
        }
    }

    public static boolean isNetworkStatusAvailable (Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if(netInfos != null)
                if(netInfos.isConnected())
                    return true;
        }
        return false;
    }

    /**
     * Formatting strings entered by user to be suitable for the google books API search.
     * First trimming leading and trailing spaces, then replacing all spaces between words with + character.
     */
    private String formatSearchText(String string) {
        String trimmedString = string.trim();
        do {
            trimmedString = trimmedString.replace(" ", "+");
        } while (trimmedString.contains(" "));
        return trimmedString;
    }
}
