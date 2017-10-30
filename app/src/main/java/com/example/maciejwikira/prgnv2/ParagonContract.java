package com.example.maciejwikira.prgnv2;

import android.provider.BaseColumns;

/**
 * Created by Maciej on 2017-10-17.
 */

public final class ParagonContract {
    private ParagonContract(){}

    public static class Paragon implements BaseColumns {
        public static final String TABLE_NAME = "paragons";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String DATE = "date";
        public static final String VALUE = "value";
        public static final String IMAGE_PATH = "img";
        public static final String CONTENT = "text";
        public static final String FAVORITED = "favorited";

        public static final String SQL_CREATE_PARAGONS =
                "CREATE TABLE IF NOT EXISTS " + Paragon.TABLE_NAME + " (" +
                    Paragon._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Paragon.NAME + " TEXT," +
                    Paragon.CATEGORY + " TEXT," +
                    Paragon.DATE +  " DATE," +
                    Paragon.VALUE + " REAL," +
                    Paragon.IMAGE_PATH + " TEXT," +
                    Paragon.CONTENT + " TEXT," +
                    "FOREIGN KEY (" + Paragon.CATEGORY + ") REFERENCES categories(" + Categories.CATEGORY_NAME + "))";

        public static final String SQL_DELETE_PARAGONS = "DROP TABLE IF EXISTS " + Paragon.TABLE_NAME;
    }

    public static class Categories implements BaseColumns{
        public static final String TABLE_NAME = "categories";
        public static final String CATEGORY_NAME = "category_name";

        public static final String SQL_CREATE_CATEGORIES = "CREATE TABLE IF NOT EXISTS " + Categories.TABLE_NAME + " (" +
                Categories._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Categories.CATEGORY_NAME + " TEXT)";

        public static final String SQL_INSERT_EMPTY_CATEGORY = "INSERT INTO " + Categories.TABLE_NAME + " (" + Categories.CATEGORY_NAME + ") "
                + "VALUES ('Brak Kategorii')";

        public static final String SQL_DELETE_CATEGORIES = "DROP TABLE IF EXISTS " + Categories.TABLE_NAME;
    }
}
