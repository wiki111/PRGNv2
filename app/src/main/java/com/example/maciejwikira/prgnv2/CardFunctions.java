package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Maciej on 2017-11-02.
 */

public class CardFunctions {

    private Context context;
    private SQLiteDatabase db;
    private ParagonDbHelper mDbHelper;
    private ArrayList<Integer> itemIds;
    private CardListAdapter cardListAdapter;

    public CardFunctions(Context context){
        this.context = context;
        itemIds = new ArrayList<Integer>();
    }

    public void addCard(ContentValues cv){
        try{
            mDbHelper = new ParagonDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            String[] projection = {
                    CardContract.Card_Categories._ID,
                    CardContract.Card_Categories.CATEGORY_NAME
            };

            String selection = CardContract.Card_Categories.CATEGORY_NAME + " = ?";
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            Cursor cursor = db.query(
                    CardContract.Card_Categories.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            if((cursor != null) && (cursor.getCount() > 0)){
                db.insert(CardContract.Card.TABLE_NAME, null, cv); //dodanie rekordu do bazy danych
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(CardContract.Card_Categories.CATEGORY_NAME,  cv.get("category").toString().toLowerCase() );
                db.insert(CardContract.Card_Categories.TABLE_NAME, null, newCategoryValue);
                db.insert(CardContract.Card.TABLE_NAME, null, cv); //dodanie rekordu do bazy danych
            }

            cursor.close();

            //Wyświetlenie potwierdzenia pomyślnego wykonania operacji
            Toast toast = Toast.makeText(context, "Huraaa! Kartę dodano pomyślnie." , Toast.LENGTH_LONG);
            toast.show();
        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            Toast toast = Toast.makeText(context, "Ups, coś poszło nie tak... Może spróbuj jeszcze raz ?" + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            db.close();
        }
    }

    public void populateList(ListView lv, String query){
        //get reference do the database
        mDbHelper = new ParagonDbHelper(context);
        db = mDbHelper.getWritableDatabase();

        //declare what to get from db
        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "expiration",
                "img"
        };

        Cursor c;

        if(query == null){
            c = db.query(true, CardContract.Card.TABLE_NAME, cols,null, null, null, null, null, null);
        }else{
            c = db.rawQuery(query, null);
        }

        try{
            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(CardContract.Card._ID)));
            }

            String[] from = new String[]{
                    CardContract.Card.NAME,
                    CardContract.Card.CATEGORY,
                    CardContract.Card.EXPIRATION_DATE,
                    CardContract.Card.IMAGE_PATH
            };

            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.photoView
            };

            c.moveToFirst();

            //finally get and set adapter
            cardListAdapter = new CardListAdapter(context, R.layout.paragon_list_item, c, from, to, 0);
            lv.setAdapter(cardListAdapter);

        }finally {
            db.close();
        }
    }

}
