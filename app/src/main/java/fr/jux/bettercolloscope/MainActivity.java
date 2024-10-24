package fr.jux.bettercolloscope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public String reponse = "init";
    public Intent i;
    TextView textView;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(myIntent, SECOND_ACTIVITY_REQUEST_CODE);
        TableLayout main_table = findViewById(R.id.main_table);
        textView = findViewById(R.id.reponse_text);
        main_table.setStretchAllColumns(true);
        init_tables(main_table);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (SECOND_ACTIVITY_REQUEST_CODE) : {
                if (resultCode == Activity.RESULT_OK) {
                    reponse = data.getStringExtra("reponse");
                }
                break;
            }
        }
        textView.setText(reponse);
    }

    public void init_tables(TableLayout main_table) {
        //TODO : CREATE TABLES FROM HTML RAW DATA
    }

}

