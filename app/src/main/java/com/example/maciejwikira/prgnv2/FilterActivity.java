package com.example.maciejwikira.prgnv2;

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
import android.widget.EditText;
import android.widget.Spinner;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String chosenCategory;
    private EditText editFromDate;
    private EditText editToDate;
    private boolean showParagons;
    private String[] cols;
    private String[] from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_paragons);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        Spinner spinner = (Spinner)findViewById(R.id.categorySpinner);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        showParagons = bundle.getBoolean(MainViewActivity.CARDS_OR_PARAGONS);

        mDbHelper = new ParagonDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        int[] to = new int[]{
                R.id.categoryName
        };

        if(showParagons == true){
            cols = new String[]{
                    ParagonContract.Categories._ID,
                    ParagonContract.Categories.CATEGORY_NAME
            };

            from = new String[]{
                    ParagonContract.Categories.CATEGORY_NAME
            };
            c = db.query(true, ParagonContract.Categories.TABLE_NAME, cols, null,null,null,null,null,null);
        }else {
            cols = new String[]{
                    CardContract.Card_Categories._ID,
                    CardContract.Card_Categories.CATEGORY_NAME
            };

            from = new String[]{
                    CardContract.Card_Categories.CATEGORY_NAME
            };
            c = db.query(true, CardContract.Card_Categories.TABLE_NAME, cols, null,null,null,null,null,null);
        }


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
                if(showParagons == true){
                    extras.putString("Chosen_From_Date", editFromDate.getText().toString());
                    extras.putString("Chosen_To_Date", editToDate.getText().toString());
                }
                extras.putString("Reset", "false");
                resultIntent.putExtras(extras);
                setResult(MainViewActivity.RESULT_FILTER, resultIntent);
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
                setResult(MainViewActivity.RESULT_FILTER, resultIntent);
                finish();
            }
        });

        editFromDate = (EditText)findViewById(R.id.editFromDate);
        if(showParagons == true){
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
        }else {
            editFromDate.setVisibility(View.INVISIBLE);
        }



        editToDate = (EditText)findViewById(R.id.editToDate);
        if(showParagons == true){
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
        }else {
            editToDate.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String selection;
        String[] selectionArgs;

        if(showParagons == true){
            selection = ParagonContract.Categories._ID + " = ?";
            selectionArgs = new String[]{ Long.toString(id) };
            c = db.query(ParagonContract.Categories.TABLE_NAME, cols,selection,selectionArgs,null,null,null);
        }else {
            selection = CardContract.Card_Categories._ID + " = ?";
            selectionArgs = new String[]{ Long.toString(id) };
            c = db.query(CardContract.Card_Categories.TABLE_NAME, cols,selection,selectionArgs,null,null,null);
        }


        while(c.moveToNext()){
            if(showParagons == true)
                chosenCategory = c.getString(c.getColumnIndex(ParagonContract.Categories.CATEGORY_NAME));
            else
                chosenCategory = c.getString(c.getColumnIndex(CardContract.Card_Categories.CATEGORY_NAME));
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
