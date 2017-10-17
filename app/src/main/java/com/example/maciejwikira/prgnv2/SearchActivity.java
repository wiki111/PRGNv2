package com.example.maciejwikira.prgnv2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends AppCompatActivity {

    private SQLiteDatabase prgnDatabase;
    private ListView searchOutputField;
    private EditText searchInputField;
    private Button buttonSearch;
    private String thingToFind;
    private CheckBox searchParagonContent;
    private ListView categoryList;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInputField = (EditText)findViewById(R.id.searchInputField);
        searchParagonContent = (CheckBox)findViewById(R.id.searchsParagonContent);
        buttonSearch = (Button)findViewById(R.id.buttonSearch);
        categoryList = (ListView)findViewById(R.id.categoryList);

        ParagonDbHelper mDbHelper = new ParagonDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        String[] colsToGet = new String[]{
                ParagonContract.Categories._ID,
                ParagonContract.Categories.CATEGORY_NAME
        };

        String[] fromCols = new String[]{
                ParagonContract.Categories.CATEGORY_NAME
        };

        int[] toFields = new int[]{
            R.id.categoryName
        };

        Cursor cursor;

        cursor = db.query(true, ParagonContract.Categories.TABLE_NAME, colsToGet, null, null, null, null, null, null);
        cursor.moveToFirst();

        Toast tst = Toast.makeText(this, cursor.getString(cursor.getColumnIndex(ParagonContract.Categories.CATEGORY_NAME)), Toast.LENGTH_SHORT);
        tst.show();

        CategoryAdapter ca;
        ca = new CategoryAdapter(this, R.layout.category_list_item, cursor, fromCols, toFields, 0);
        categoryList.setAdapter(ca);

       // cursor.close();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thingToFind = searchInputField.getText().toString();
                searchForItems(thingToFind);
            }
        });
    }

    private void searchForItems(String thingToFind){


        searchOutputField = (ListView)findViewById(R.id.searchOutputField);

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

        //TextRecognizer tr = new TextRecognizer.Builder(this).build();
        //Bitmap imgr;
       // Frame frame;
        //SparseArray<TextBlock> items;
        //TextBlock item;

        String itemContent;
        String itemNameContent;
        List<String> indexes = new ArrayList<String>();

        Cursor c;
        PrgnDBAdapter pdba;
        //int img_index;
        int text_index;
        int title_index;

        Matcher match;
        Pattern pattern = Pattern.compile(thingToFind.toLowerCase());

        c = db.query(true, ParagonContract.Paragon.TABLE_NAME, cols,null, null, null, null, null, null);

        try {
            while (c.moveToNext()) {

                /*
                img_index = c.getColumnIndex("img");
                imgr = BitmapFactory.decodeFile(c.getString(img_index));
                frame = new Frame.Builder().setBitmap(imgr).build();
                items = tr.detect(frame);

                for(int i = 0; i < items.size(); i++){
                    item = items.valueAt(i);
                    itemContent = item.getValue().toLowerCase();
                    match = pattern.matcher(itemContent);

                    if(match.find()){
                        indexes.add(c.getString(c.getColumnIndex("_id")));
                        i = items.size();
                        Toast toast = Toast.makeText(this,"Znaleziono : " + match.group().substring(0), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                */

                title_index = c.getColumnIndex(ParagonContract.Paragon.NAME);
                itemNameContent = c.getString(title_index).toLowerCase();
                match = pattern.matcher(itemNameContent);
                if(match.find()){
                    indexes.add(c.getString(c.getColumnIndex(ParagonContract.Paragon._ID)));
                }

                if(searchParagonContent.isChecked()) {
                    text_index = c.getColumnIndex(ParagonContract.Paragon.CONTENT);
                    itemContent = c.getString(text_index);
                    itemContent.toLowerCase();
                    match = pattern.matcher(itemContent);
                    if (match.find()) {
                        indexes.add(c.getString(c.getColumnIndex(ParagonContract.Paragon._ID)));
                        Toast toast = Toast.makeText(this, "Znaleziono : " + match.group().substring(0), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

            }
        } finally {
            c.close();
        }


        c = db.query(true, ParagonContract.Paragon.TABLE_NAME, cols, ParagonContract.Paragon._ID + " in ("+makePlaceholders(indexes.size())+") ", indexes.toArray(new String[indexes.size()]), null, null, null, null);
        c.moveToFirst();

        pdba = new PrgnDBAdapter(this, R.layout.list_item_layout, c, from, to, 0);
        searchOutputField.setAdapter(pdba);

    }

    String makePlaceholders(int len) {
        if (len < 1) {
            Toast error = Toast.makeText(this, "Nie znaleziono", Toast.LENGTH_SHORT);
            error.show();
            return "";
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }


}
