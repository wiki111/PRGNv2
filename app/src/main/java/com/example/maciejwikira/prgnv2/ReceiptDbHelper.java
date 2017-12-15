package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Maciej on 2017-10-17.
 */

// Klasa pomocnicza wykonująca operacje na bazie danych i pozwalająca na tworzenie połączeń z nią.
public class ReceiptDbHelper extends SQLiteOpenHelper {

    // Deklaracja wersji i nazwy bazy danych.
    public static final int DATABASE_VERSION = 18;
    public static final String DATABASE_NAME = "ParagonApp.db";

    // Konstruktor
    public ReceiptDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Sekwencja poleceń tworząca i inicjalizująca tabele w bazie danych
    public void onCreate(SQLiteDatabase db){
        db.execSQL(ReceiptContract.Categories.SQL_CREATE_CATEGORIES);
        db.execSQL(ReceiptContract.Categories.SQL_INSERT_EMPTY_CATEGORY);
        db.execSQL(ReceiptContract.Receipt_Photos.SQL_CREATE_RECEIPT_PHOTOS);
        db.execSQL(ReceiptContract.Receipt.SQL_CREATE_RECEIPTS);
        db.execSQL(CardContract.Card_Categories.SQL_CREATE_CARD_CATEGORIES);
        db.execSQL(CardContract.Card_Categories.SQL_INSERT_EMPTY_CARD_CATEGORY);
        db.execSQL(CardContract.Card.SQL_CREATE_CARDS);
    }

    // Sekwencja poleceń wykonywana przy aktualizacji bazy danych
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(ReceiptContract.Categories.SQL_DELETE_CATEGORIES);
        db.execSQL(ReceiptContract.Receipt_Photos.SQL_DELETE_RECEIPT_PHOTOS);
        db.execSQL(ReceiptContract.Receipt.SQL_DELETE_RECEIPTS);
        db.execSQL(CardContract.Card_Categories.SQL_DELETE_CARD_CATEGORIES);
        db.execSQL(CardContract.Card.SQL_DELETE_CARDS);
        onCreate(db);
    }

}
