package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maciej on 2017-10-30.
 */

public class ParagonFunctions {

    private SQLiteDatabase db;
    private ParagonDbHelper mDbHelper;
    private ParagonListAdapter paragonListAdapter;
    private Context context;
    private boolean resetFilters;
    private Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
    private String query;
    private Matcher matcherFrom;
    private Matcher matcherTo;
    private String chosenCategory;
    private String chosenFromDate;
    private String chosenToDate;
    private ArrayList<Integer> itemIds;


    public ParagonFunctions(Context context){
        this.context = context;
        itemIds = new ArrayList<>();
        resetFilters = true;
        query = null;
    }

    public void addParagon(ContentValues cv){
        try{
            mDbHelper = new ParagonDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            String[] projection = {
                    ParagonContract.Categories._ID,
                    ParagonContract.Categories.CATEGORY_NAME
            };

            String selection = ParagonContract.Categories.CATEGORY_NAME + " = ?";
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            Cursor cursor = db.query(
                    ParagonContract.Categories.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            if((cursor != null) && (cursor.getCount() > 0)){
                db.insert(ParagonContract.Paragon.TABLE_NAME, null, cv); //dodanie rekordu do bazy danych
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(ParagonContract.Categories.CATEGORY_NAME,  cv.get("category").toString().toLowerCase() );
                db.insert(ParagonContract.Categories.TABLE_NAME, null, newCategoryValue);
                db.insert(ParagonContract.Paragon.TABLE_NAME, null, cv); //dodanie rekordu do bazy danych
            }

            cursor.close();

            //Wyświetlenie potwierdzenia pomyślnego wykonania operacji
            Toast toast = Toast.makeText(context, "Huraaa! Paragon dodano pomyślnie." , Toast.LENGTH_LONG);
            toast.show();
        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            Toast toast = Toast.makeText(context, "Ups, coś poszło nie tak... Może spróbuj jeszcze raz ?" + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            db.close();
        }
    }

    public void updateParagon(String item_id, ContentValues cv){

        Cursor cursor = null;

        try{
            mDbHelper = new ParagonDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            String[] projection = {
                    ParagonContract.Categories._ID,
                    ParagonContract.Categories.CATEGORY_NAME
            };

            String selection = ParagonContract.Categories.CATEGORY_NAME + " = ?";
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            String selectedParagon = ParagonContract.Paragon._ID + " = ?";
            String[] args = new String[]{
                    item_id
            };

            cursor = db.query(
                    ParagonContract.Categories.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            if((cursor != null) && (cursor.getCount() > 0)){
                db.update(ParagonContract.Paragon.TABLE_NAME, cv, selectedParagon, args); //dodanie rekordu do bazy danych
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(ParagonContract.Categories.CATEGORY_NAME, cv.get("category").toString().toLowerCase());
                db.insert(ParagonContract.Categories.TABLE_NAME, null, newCategoryValue);
                db.update(ParagonContract.Paragon.TABLE_NAME, cv, selectedParagon, args);
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
                "value",
                "date",
                "img",
                "text",
                "favorited"
        };

        Cursor c;

        if(query == null){
            c = db.query(true, ParagonContract.Paragon.TABLE_NAME, cols,null, null, null, null, null, null);
        }else{
            c = db.rawQuery(query, null);
        }

        try{
            itemIds.clear();

            //fill array with paragon objects created from database data
            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(ParagonContract.Paragon._ID)));
            }

            String[] from = new String[]{
                    ParagonContract.Paragon.NAME,
                    ParagonContract.Paragon.CATEGORY,
                    ParagonContract.Paragon.DATE,
                    ParagonContract.Paragon.VALUE,
                    ParagonContract.Paragon.IMAGE_PATH,
                    ParagonContract.Paragon.CONTENT,
                    ParagonContract.Paragon.FAVORITED
            };

            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.photoView
            };

            c.moveToFirst();

            //finally get and set adapter
            paragonListAdapter = new ParagonListAdapter(context, R.layout.paragon_list_item, c, from, to, 0);
            lv.setAdapter(paragonListAdapter);

        }finally {
            db.close();
        }

    }

    public void search(ListView lv, String query){

        mDbHelper = new ParagonDbHelper(context);
        db = mDbHelper.getReadableDatabase();

        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        String dbQuery = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " + ParagonContract.Paragon.NAME +
                " LIKE '" + query + "' OR " + ParagonContract.Paragon.CONTENT + " LIKE '%" + query + "%'";

        Cursor c;

        c = db.rawQuery(dbQuery, null);

        try {
            itemIds.clear();

            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(ParagonContract.Paragon._ID)));
            }

            String[] from = new String[]{
                    ParagonContract.Paragon.NAME,
                    ParagonContract.Paragon.CATEGORY,
                    ParagonContract.Paragon.DATE,
                    ParagonContract.Paragon.VALUE,
                    ParagonContract.Paragon.IMAGE_PATH,
                    ParagonContract.Paragon.CONTENT,
                    ParagonContract.Paragon.FAVORITED
            };

            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.photoView
            };

            c.moveToFirst();

            //finally get and set adapter
            paragonListAdapter = new ParagonListAdapter(context, R.layout.paragon_list_item, c, from, to, 0);
            lv.setAdapter(paragonListAdapter);
        } finally {
            db.close();
        }
    }

    public void filterList(Bundle extras){

        String reset = extras.getString("Reset");
        chosenCategory = extras.getString("Chosen_Category");
        chosenFromDate = extras.getString("Chosen_From_Date");
        chosenToDate = extras.getString("Chosen_To_Date");

        if(reset.equals("false")){
            resetFilters = false;
            if(chosenFromDate.equals("YYYY-MM-DD") && chosenToDate.equals("YYYY-MM-DD")){
                if(chosenCategory.equals("Brak Kategorii")){
                    query = null;
                }else{
                    query = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " +
                            ParagonContract.Paragon.CATEGORY + " = '" + chosenCategory + "'";
                }
            }else{
                matcherFrom = datePattern.matcher(chosenFromDate);
                matcherTo = datePattern.matcher(chosenToDate);
                if(matcherFrom.find() && matcherTo.find()){
                    String from = "'" + chosenFromDate + "'";
                    String to = "'" + chosenToDate + "'";
                    if(!chosenCategory.equals("Brak Kategorii")){
                        query = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " + ParagonContract.Paragon.CATEGORY + " = '" + chosenCategory + "' AND " + ParagonContract.Paragon.DATE + " >= " + from
                                + " AND " + ParagonContract.Paragon.DATE + " <= " + to;
                    }else{
                        query = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " + ParagonContract.Paragon.DATE + " >= " + from
                                + " AND " + ParagonContract.Paragon.DATE + " <= " + to;
                    }
                }else{
                    Toast tst = Toast.makeText(context, "Nieprawidłowa data - spróbuj jeszcze raz.", Toast.LENGTH_LONG);
                    tst.show();
                    if(!chosenCategory.equals("Brak Kategorii")){
                        query = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " +
                                ParagonContract.Paragon.CATEGORY + " = '" + chosenCategory + "'";
                    }else{
                        query = null;
                    }
                }
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

    public ArrayList<Integer> getItemIds(){
        return this.itemIds;
    }

}
