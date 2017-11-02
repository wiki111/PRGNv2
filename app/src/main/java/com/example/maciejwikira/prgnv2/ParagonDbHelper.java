package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maciej on 2017-10-17.
 */

public class ParagonDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 12;
    public static final String DATABASE_NAME = "ParagonApp.db";

    public ParagonDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(ParagonContract.Categories.SQL_CREATE_CATEGORIES);
        db.execSQL(ParagonContract.Categories.SQL_INSERT_EMPTY_CATEGORY);
        db.execSQL(ParagonContract.Paragon.SQL_CREATE_PARAGONS);
        db.execSQL(CardContract.Card_Categories.SQL_CREATE_CARD_CATEGORIES);
        db.execSQL(CardContract.Card_Categories.SQL_INSERT_EMPTY_CARD_CATEGORY);
        db.execSQL(CardContract.Card.SQL_CREATE_CARDS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(ParagonContract.Categories.SQL_DELETE_CATEGORIES);
        db.execSQL(ParagonContract.Paragon.SQL_DELETE_PARAGONS);
        db.execSQL(CardContract.Card_Categories.SQL_DELETE_CARD_CATEGORIES);
        db.execSQL(CardContract.Card.SQL_DELETE_CARDS);
        onCreate(db);
    }


}
