package com.example.maciejwikira.prgnv2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.List;

public class RaportActivity extends AppCompatActivity {

    private ListView raportListView;

    //DB declaration
    private SQLiteDatabase prgnDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raport);

        prgnDatabase = openOrCreateDatabase("prgnDatabase",MODE_PRIVATE,null);

        raportListView = (ListView)findViewById(R.id.raportListView);

        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        String[] from = new String[]{
                "name",
                "category",
                "date",
                "value",
                "img",
                "text"
        };

        int[] to = new int[]{
                R.id.prgnNameView,
                R.id.prgnCategoryView,
                R.id.prgnDateView,
                R.id.prgnValueView,
                R.id.prgnPhotoView,
                R.id.prgnTextView
        };

        Cursor c = prgnDatabase.query(true, "prgns", cols,null, null, null, null, null, null);
        c.moveToFirst();

        PrgnDBAdapter pdba = new PrgnDBAdapter(this, R.layout.list_item_layout, c, from, to, 0);
        raportListView.setAdapter(pdba);

        prgnDatabase.close();

    }



}
