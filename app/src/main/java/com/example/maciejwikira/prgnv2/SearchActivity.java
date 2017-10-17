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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchInputField = (EditText)findViewById(R.id.searchInputField);

        searchParagonContent = (CheckBox)findViewById(R.id.searchsParagonContent);

        buttonSearch = (Button)findViewById(R.id.buttonSearch);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thingToFind = searchInputField.getText().toString();
                searchForItems(thingToFind);
            }
        });
    }

    private void searchForItems(String thingToFind){

        prgnDatabase = openOrCreateDatabase("prgnDatabase", MODE_PRIVATE,null);

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

        c = prgnDatabase.query(true, "prgns", cols,null, null, null, null, null, null);

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

                title_index = c.getColumnIndex("name");
                itemNameContent = c.getString(title_index).toLowerCase();
                match = pattern.matcher(itemNameContent);
                if(match.find()){
                    indexes.add(c.getString(c.getColumnIndex("_id")));
                }

                if(searchParagonContent.isChecked()) {
                    text_index = c.getColumnIndex("text");
                    itemContent = c.getString(text_index);
                    itemContent.toLowerCase();
                    match = pattern.matcher(itemContent);
                    if (match.find()) {
                        indexes.add(c.getString(c.getColumnIndex("_id")));
                        Toast toast = Toast.makeText(this, "Znaleziono : " + match.group().substring(0), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

            }
        } finally {
            c.close();
        }


        c = prgnDatabase.query(true, "prgns", cols, "_id in ("+makePlaceholders(indexes.size())+") ", indexes.toArray(new String[indexes.size()]), null, null, null, null);
        c.moveToFirst();

        pdba = new PrgnDBAdapter(this, R.layout.list_item_layout, c, from, to, 0);
        searchOutputField.setAdapter(pdba);

        prgnDatabase.close();

    }

    String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
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
