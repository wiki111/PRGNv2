package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maciej on 2017-10-17.
 */

public class ParagonDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "ParagonApp.db";

    public ParagonDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(ParagonContract.Categories.SQL_CREATE_CATEGORIES);
        db.execSQL(ParagonContract.Paragon.SQL_CREATE_PARAGONS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onCreate(db);
    }


}
