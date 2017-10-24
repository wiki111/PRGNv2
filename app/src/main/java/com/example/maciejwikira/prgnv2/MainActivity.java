package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button newPrgnBtn;
    private Button raportPrgnBtn;
    private Button searchPrgnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        Intent intent = new Intent(context, MainViewActivity.class);
        startActivity(intent);

    }


}
