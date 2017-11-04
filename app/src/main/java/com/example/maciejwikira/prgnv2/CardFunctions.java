package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maciej on 2017-11-02.
 */

public class CardFunctions {

    private Context context;
    private SQLiteDatabase db;
    private ParagonDbHelper mDbHelper;
    private ArrayList<Integer> itemIds;
    private CardListAdapter cardListAdapter;
    private boolean resetFilters;
    private String query;
    private String chosenCategory;
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

    public void updateCard(String item_id, ContentValues cv){

        Cursor cursor = null;

        try{
            mDbHelper = new ParagonDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            String[] projection = {
                    CardContract.Card_Categories._ID,
                    CardContract.Card_Categories.CATEGORY_NAME
            };

            String selection = CardContract.Card_Categories.CATEGORY_NAME + " = ?";
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            String selectedParagon = CardContract.Card._ID + " = ?";
            String[] args = new String[]{
                    item_id
            };

            cursor = db.query(
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
                db.update(CardContract.Card.TABLE_NAME, cv, selectedParagon, args); //dodanie rekordu do bazy danych
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(CardContract.Card_Categories.CATEGORY_NAME, cv.get("category").toString().toLowerCase());
                db.insert(CardContract.Card_Categories.TABLE_NAME, null, newCategoryValue);
                db.update(CardContract.Card.TABLE_NAME, cv, selectedParagon, args);
            }
        }finally {
            cursor.close();
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

    public ArrayList<Integer> getItemIds(){
        return this.itemIds;
    }

    public void filterList(Bundle extras){

        String reset = extras.getString("Reset");
        chosenCategory = extras.getString("Chosen_Category");

        if(reset.equals("false")){
            resetFilters = false;
                if(chosenCategory.equals("Brak Kategorii")){
                    query = null;
                }else{
                    query = "SELECT * FROM " + CardContract.Card.TABLE_NAME + " WHERE " +
                            CardContract.Card.CATEGORY + " = '" + chosenCategory + "'";
                }
        }else{
            resetFilters = true;
        }
    }

    public boolean getResetFilters(){
        return this.resetFilters;
    }

    public String getQuery(){
        return this.query;
    }

    public void search(ListView lv, String query){

        mDbHelper = new ParagonDbHelper(context);
        db = mDbHelper.getReadableDatabase();

        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "expiration",
                "img"
        };

        String itemNameContent;

        Cursor c;
        int title_index;
        String idsToGet = "(";
        ArrayList<Integer> matchingIds = new ArrayList<Integer>();
        Matcher matchName;
        Pattern pattern = Pattern.compile(query.toLowerCase());

        c = db.query(true, CardContract.Card.TABLE_NAME, cols,null, null, null, null, null, null);

        try {
            while (c.moveToNext()) {

                title_index = c.getColumnIndex(CardContract.Card.NAME);
                itemNameContent = c.getString(title_index).toLowerCase();
                matchName = pattern.matcher(itemNameContent);
                if(matchName.find()){
                    matchingIds.add(c.getInt(c.getColumnIndex(CardContract.Card._ID)));
                }
            }
        } finally {
            c.close();
            db.close();
        }

        for(int i = 0; i < matchingIds.size(); i++){
            if(i == matchingIds.size() - 1){
                idsToGet += matchingIds.get(i) + " )";
            }else{
                idsToGet += matchingIds.get(i) + ", ";
            }
        }

        String refinedQuery = "SELECT * FROM " + CardContract.Card.TABLE_NAME + " WHERE " +
                CardContract.Card._ID + " IN " + idsToGet;

        populateList(lv, refinedQuery);


    }

}
