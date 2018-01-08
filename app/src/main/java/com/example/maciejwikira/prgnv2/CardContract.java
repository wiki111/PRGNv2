package com.example.maciejwikira.prgnv2;

import android.provider.BaseColumns;

/**
 * Created by Maciej on 2017-11-02.
 */

/*
    Klasa definiuje stałe używane do obsługi tabeli bazy danych zawierających informacje
    dotyczące kart lojalnościowych.
 */
public final class CardContract {

    private CardContract(){}

    // Klasa definiująca pola i polecenia tabeli kart lojalnościowych.
    public static class Card implements BaseColumns {

        // Nazwa głównej tabeli i jej kolumn.
        public static final String TABLE_NAME = "cards";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String EXPIRATION_DATE = "expiration";
        public static final String IMAGE_PATH = "img";
        public static final String FAVORITED = "favorited";
        public static final String DESCRIPTION = "description";

        // Polecenie tworzące tabelę w bazie danych.
        public static final String SQL_CREATE_CARDS =
                "CREATE TABLE IF NOT EXISTS " + Card.TABLE_NAME + " (" +
                        Card._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        Card.NAME + " TEXT, " +
                        Card.CATEGORY + " TEXT, " +
                        Card.EXPIRATION_DATE + " DATE, " +
                        Card.IMAGE_PATH + " TEXT, " +
                        Card.FAVORITED + " TEXT, " +
                        Card.DESCRIPTION + " TEXT, " +
                        "FOREIGN KEY (" + Card.CATEGORY + ") REFERENCES card_categories(" +
                            Card_Categories.CATEGORY_NAME + "))";

        // Polecenie usuwające tabelę z bazy danych.
        public static final String SQL_DELETE_CARDS = "DROP TABLE IF EXISTS " + Card.TABLE_NAME;
    }


    // Klasa definiująca pola i polecenia tabeli kategorii kart lojalnościowych.
    public static class Card_Categories implements BaseColumns{

        // Nazwa tabeli kategorii kart i nazwy kolumn.
        public static final String TABLE_NAME = "card_categories";
        public static final String CATEGORY_NAME = "category_name";

        // Polecenie tworzące tabelę w bazie danych.
        public static final String SQL_CREATE_CARD_CATEGORIES = "CREATE TABLE IF NOT EXISTS " +
                Card_Categories.TABLE_NAME + " (" + Card_Categories._ID + " INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, " + Card_Categories.CATEGORY_NAME + " TEXT)";

        // Polecenie wstawiające bazową kategorię do tabeli.
        public static final String SQL_INSERT_EMPTY_CARD_CATEGORY = "INSERT INTO " +
                Card_Categories.TABLE_NAME + " (" + Card_Categories.CATEGORY_NAME + ") " + "VALUES " +
                "('')";

        public static final String INSERT_CATEGORIES = "INSERT INTO " +
                Card_Categories.TABLE_NAME + " (" + Card_Categories.CATEGORY_NAME + ") " + "VALUES " +
                "('Bonusy'), ('Rabaty'), ('Promocje'), ('Oferty specjalne'), ('Vouchery')";


        // Polecenie usuwające tabelę.
        public static final String SQL_DELETE_CARD_CATEGORIES = "DROP TABLE IF EXISTS " +
                Card_Categories.TABLE_NAME;

    }

    public static class Card_Photos implements BaseColumns{
        public static final String TABLE_NAME = "card_photos";
        public static final String PHOTO_PATH = "photo_path";
        public static final String CARD_ID = "card_id";

        public static final String SQL_CREATE_CARD_PHOTOS =
                "CREATE TABLE IF NOT EXISTS " + Card_Photos.TABLE_NAME + " (" +
                        Card_Photos._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        Card_Photos.PHOTO_PATH + " TEXT, " +
                        Card_Photos.CARD_ID + " INTEGER, " +
                        "FOREIGN KEY (" +
                        Card_Photos.CARD_ID + ") REFERENCES " +
                        Card.TABLE_NAME +
                        " (" + Card._ID + "))";

        public static final String SQL_DELETE_CARD_PHOTOS =
                "DROP TABLE IF EXISTS " + Card_Photos.TABLE_NAME;

    }

}
