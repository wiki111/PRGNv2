package com.example.maciejwikira.prgnv2;

import android.provider.BaseColumns;

/**
 * Created by Maciej on 2017-10-17.
 */

// Klasa definiuje kolumny tabeli dotyczących paragonów w bazie danych, oraz polecenia SQL pozwalające
// na stworzenie, inicjalizację i usuwanie tabel.
public final class ReceiptContract {
    private ReceiptContract(){}

    // Tabela danych paragonów
    public static class Receipt implements BaseColumns {
        public static final String TABLE_NAME = "paragons";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String DATE = "date";
        public static final String VALUE = "value";
        public static final String IMAGE_PATH = "img";
        public static final String CONTENT = "text";
        public static final String FAVORITED = "favorited";

        public static final String SQL_CREATE_PARAGONS =
                "CREATE TABLE IF NOT EXISTS " + Receipt.TABLE_NAME + " (" +
                    Receipt._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Receipt.NAME + " TEXT," +
                    Receipt.CATEGORY + " TEXT," +
                    Receipt.DATE +  " DATE," +
                    Receipt.VALUE + " REAL," +
                    Receipt.IMAGE_PATH + " TEXT," +
                    Receipt.CONTENT + " TEXT," +
                    Receipt.FAVORITED + " TEXT," +
                    "FOREIGN KEY (" + Receipt.CATEGORY + ") REFERENCES categories(" + Categories.CATEGORY_NAME + "))";

        public static final String SQL_DELETE_PARAGONS = "DROP TABLE IF EXISTS " + Receipt.TABLE_NAME;
    }

    // Tabela kategorii paragonów
    public static class Categories implements BaseColumns{
        public static final String TABLE_NAME = "categories";
        public static final String CATEGORY_NAME = "category_name";

        public static final String SQL_CREATE_CATEGORIES = "CREATE TABLE IF NOT EXISTS " +
                Categories.TABLE_NAME + " (" + Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Categories.CATEGORY_NAME + " TEXT)";

        public static final String SQL_INSERT_EMPTY_CATEGORY = "INSERT INTO " + Categories.TABLE_NAME
                + " (" + Categories.CATEGORY_NAME + ") " + "VALUES ('Brak Kategorii')";

        public static final String SQL_DELETE_CATEGORIES = "DROP TABLE IF EXISTS " + Categories.TABLE_NAME;
    }
}
