package com.example.maciejwikira.prgnv2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String chosenCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        Spinner spinner = (Spinner)findViewById(R.id.categorySpinner);

        mDbHelper = new ParagonDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        String[] cols = new String[]{
                ParagonContract.Categories._ID,
                ParagonContract.Categories.CATEGORY_NAME
        };

        String[] from = new String[]{
                ParagonContract.Categories.CATEGORY_NAME
        };

        int[] to = new int[]{
                R.id.categoryName
        };

        c = db.query(true, ParagonContract.Categories.TABLE_NAME, cols, null,null,null,null,null,null);
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.category_spinner_item, c, from, to, 0);
        spinner.setAdapter(simpleCursorAdapter);
        spinner.setOnItemSelectedListener(this);

        Button filterButton =(Button)findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Chosen_Category", chosenCategory);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        Button resertButton = (Button)findViewById(R.id.resetButton);
        resertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Chosen_Category", "");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String[] cols = new String[]{
                ParagonContract.Categories._ID,
                ParagonContract.Categories.CATEGORY_NAME
        };

        String selection = ParagonContract.Categories._ID + " = ?";
        String[] selectionArgs = { Long.toString(id) };

        c = db.query(ParagonContract.Categories.TABLE_NAME, cols,selection,selectionArgs,null,null,null);

        while(c.moveToNext()){
            chosenCategory = c.getString(c.getColumnIndex(ParagonContract.Categories.CATEGORY_NAME));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        c.close();
        db.close();
    }
}
