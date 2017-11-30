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
 * Created by Maciej on 2017-11-30.
 */

// Klasa implementuje funkcje pozwalające na zarządzanie danymi
// kart i paragonów przechowywanych przez aplikację.

public class DataHandler {

    private Context context;
    private SQLiteDatabase db;
    private ReceiptDbHelper mDbHelper;

    private CardListAdapter cardListAdapter;
    private ReceiptListAdapter receiptListAdapter;

    private ArrayList<Integer> itemIds;

    private boolean resetFilters;
    private boolean showReceipts;

    private String chosenCategory;
    private String chosenFromDate, chosenToDate;
    private Matcher matcherFrom, matcherTo;
    private Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
    private String query;

    private Toast toast;

    private String categoryColumn;
    private String[] projection;
    private String selection;
    private String categoriesTable;
    private String itemTable;
    private String updateSelection;
    private String[] tableCols;

    public DataHandler(Context context){
        this.context = context;
        resetFilters = true;
        chosenCategory = null;
        query = null;
        mDbHelper = new ReceiptDbHelper(context);
        itemIds = new ArrayList<>();
        setShowReceipts(true);
    }

    public void setShowReceipts(boolean show){
        if(show){
            showReceipts = true;
            categoryColumn = ReceiptContract.Receipt.CATEGORY;
            projection = Constants.receiptCategoriesProjection;
            selection = Constants.receiptCategoriesSelection;
            categoriesTable = ReceiptContract.Categories.TABLE_NAME;
            itemTable = ReceiptContract.Receipt.TABLE_NAME;
            updateSelection = ReceiptContract.Receipt._ID + " = ?";
            tableCols = Constants.receiptTableCols;
        }else{
            showReceipts = false;
            categoryColumn = CardContract.Card.CATEGORY;
            projection = Constants.cardCategoriesProjection;
            selection = Constants.cardCategoriesSelection;
            categoriesTable = CardContract.Card_Categories.TABLE_NAME;
            itemTable = CardContract.Card.TABLE_NAME;
            updateSelection = CardContract.Card._ID + " = ?";
            tableCols = Constants.cardTableCols;
        }
    }

    public void addItem(ContentValues itemData){
        try{
            db = mDbHelper.getWritableDatabase();
            checkCategory(db, itemData);
            db.insert(itemTable, null, itemData);
        }catch (Exception e) {
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(context, R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            db.close();
        }
    }

    public void updateItem(int list_position, ContentValues newItemData){
        try{
            db = mDbHelper.getWritableDatabase();
            checkCategory(db, newItemData);
            String[] item_id = {itemIds.get(list_position).toString()};
            db.update(itemTable, newItemData, updateSelection, item_id);
        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(context, R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            db.close();
        }
    }

    public void populateList(ListView lv, String query){
        try{
            db = mDbHelper.getWritableDatabase();
            Cursor c;

            if(query != null){
                c = db.rawQuery(query, null);
            }else if(query == null && this.query != null){
                c = db.rawQuery(this.query, null);
            }else{
                c = db.query(
                        true,
                        itemTable,
                        tableCols,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }

            itemIds.clear();

            while (c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(tableCols[0])));
            }

            c.moveToFirst();

            if(showReceipts){
                receiptListAdapter = new ReceiptListAdapter(
                        context,
                        R.layout.list_item,
                        c,
                        Constants.fromReceiptTable,
                        Constants.toReceiptTable,
                        0
                );
                lv.setAdapter(receiptListAdapter);
            }else{
                cardListAdapter = new CardListAdapter(
                        context,
                        R.layout.list_item,
                        c,
                        Constants.fromCardTable,
                        Constants.toCardTable,
                        0
                );
                lv.setAdapter(cardListAdapter);
            }

        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(context, R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            db.close();
        }
    }

    public void filterList(Bundle filterData){
        boolean reset = filterData.getBoolean("Reset");
        chosenCategory = filterData.getString("Chosen_Category");
        if(showReceipts){
            chosenFromDate = filterData.getString("Chosen_From_Date");
            chosenToDate = filterData.getString("Chosen_To_Date");
        }

        if(!reset){
            resetFilters = false;

            query = "SELECT * FROM " + itemTable + " WHERE ";
            String categoryPart = "";
            String datePart = "";
            boolean filterByCategory = false;
            boolean filterByDate = false;

            if(!chosenCategory.equals("")){
                filterByCategory = true;
                categoryPart = tableCols[2] + " = '" + chosenCategory + "'";
            }

            if(showReceipts){
                   if(!chosenFromDate.equals("YYYY-MM-DD")) {
                       filterByDate = true;
                       matcherFrom = datePattern.matcher(chosenFromDate);
                       matcherTo = datePattern.matcher(chosenToDate);
                       if (matcherFrom.find()) {
                           if (matcherTo.find()) {
                               datePart = ReceiptContract.Receipt.DATE +
                                       " >= " + "'" + chosenFromDate + "'" + " AND " +
                                       ReceiptContract.Receipt.DATE + " <= " +
                                       "'" + chosenToDate + "'";
                           } else {
                               datePart = ReceiptContract.Receipt.DATE +
                                       " >= " + "'" + chosenFromDate + "'";
                           }
                       }
                   }
            }

            if(filterByCategory){
                query = query + categoryPart;
            }

            if(filterByCategory && filterByDate){
                query = query + " AND " + datePart;
            }

            if(!filterByCategory && filterByDate){
                query = query + datePart;
            }
        }else{
            resetFilters = true;
        }
    }



    private boolean checkCategory( SQLiteDatabase db, ContentValues itemData){

        String categoryName = itemData.get(categoryColumn).toString().toLowerCase();
        String[] selectionArgs = {categoryName};

        Cursor cursor = db.query(
                categoriesTable,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.close();
            return true;
        }else{
            insertNewCategory(db, categoryName);
            cursor.close();
            return false;
        }

    }

    private void insertNewCategory(SQLiteDatabase db, String categoryName){
        ContentValues newCategoryValue = new ContentValues();
        newCategoryValue.put(
                categoryColumn,
                categoryName
        );
        db.insert(
                categoryColumn,
                null,
                newCategoryValue
        );
    }

}
