package com.example.maciejwikira.prgnv2;

import android.app.Activity;
import android.app.DialogFragment;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterParagonsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String chosenCategory;
    private EditText editFromDate;
    private EditText editToDate;

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
                Bundle extras = new Bundle();
                extras.putString("Chosen_Category", chosenCategory);
                extras.putString("Chosen_From_Date", editFromDate.getText().toString());
                extras.putString("Chosen_To_Date", editToDate.getText().toString());
                extras.putString("Reset", "false");
                resultIntent.putExtras(extras);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        Button resertButton = (Button)findViewById(R.id.resetButton);
        resertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                Bundle extras = new Bundle();
                extras.putString("Reset", "true");
                resultIntent.putExtras(extras);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        editFromDate = (EditText)findViewById(R.id.editFromDate);
        editFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arg = new Bundle();
                arg.putInt("Field_ID", R.id.editFromDate);
                newFragment.setArguments(arg);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });


        editToDate = (EditText)findViewById(R.id.editToDate);
        editToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arg = new Bundle();
                arg.putInt("Field_ID", R.id.editToDate);
                newFragment.setArguments(arg);
                newFragment.show(getFragmentManager(), "datePicker");
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
