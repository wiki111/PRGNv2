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

        public static final String SQL_CREATE_PARAGONS = "CREATE TABLE IF NOT EXISTS " + Paragon.TABLE_NAME + " (" +
                Paragon._ID + " INTEGER PRIMARY KEY, " + Paragon.NAME + " TEXT," + Paragon.CATEGORY + " TEXT," +
                Paragon.DATE +  " DATE," + Paragon.VALUE + " REAL," + Paragon.IMAGE_PATH + " TEXT," +
                Paragon.CONTENT + " TEXT)";

        public static final String SQL_DELETE_PARAGONS = "DROP TABLE IF EXISTS " + Paragon.TABLE_NAME;
    }

}
