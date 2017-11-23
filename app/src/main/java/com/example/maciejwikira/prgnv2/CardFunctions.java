package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Maciej on 2017-11-02.
 */

/*
    Klasa zawiera metody używane do przetwarzania danych dotyczących kart lojalnościowych
 */
public class CardFunctions {

    // Deklaracje zmiennych
    private Context context;
    private SQLiteDatabase db;
    private ReceiptDbHelper mDbHelper;
    private ArrayList<Integer> itemIds;
    private CardListAdapter cardListAdapter;

    // Zmienna przechowująca informację o tym czy aktywne jest filtrowanie listy.
    private boolean resetFilters;

    // Zapytanie do bazy danych
    private String query;

    // Aktywna kategoria
    private String chosenCategory;

    // Publiczny konstruktor. Zapisuje kontekst w którym tworzona jest instancja klasy i inicjalizuje
    // listę numerów ID wpisów, które zostały pobrane z bazy danych
    public CardFunctions(Context context){
        this.context = context;
        itemIds = new ArrayList<Integer>();
    }

    // Metoda dodająca nowy wpis do tabeli kart lojalnościowych
    public void addCard(ContentValues cv){
        try{
            // Inicjalizacja bazy danych
            mDbHelper = new ReceiptDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            // Tablica typu String zawierająca nazwy kolumn w tabeli kategorii,
            // z których należy pobrać dane.
            String[] projection = {
                    CardContract.Card_Categories._ID,
                    CardContract.Card_Categories.CATEGORY_NAME
            };

            // Definicja klauzuli WHERE dla zapytania do bazy danych
            String selection = CardContract.Card_Categories.CATEGORY_NAME + " = ?";

            // Definicja parametrów dla klauzuli WHERE
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            // Inicjalizacja kursora i pobranie informacji z bazy danych
            Cursor cursor = db.query(
                    CardContract.Card_Categories.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            // Przesunięcie pozycji kursora na pierwszy element
            cursor.moveToFirst();

            // Jeżeli kategoria, która została przypisana karcie istnieje w bazie danych zapisz nowy
            // wpis do tabeli kart. W przeciwnym wypadku zapisz kategorię w tabeli kategorii, a
            // następnie dodaj rekord do tabeli kart.
            if((cursor != null) && (cursor.getCount() > 0)){
                db.insert(CardContract.Card.TABLE_NAME, null, cv);
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(CardContract.Card_Categories.CATEGORY_NAME,  cv.get("category").toString().toLowerCase() );
                db.insert(CardContract.Card_Categories.TABLE_NAME, null, newCategoryValue);
                db.insert(CardContract.Card.TABLE_NAME, null, cv);
            }

            // Zamnknięcie kursora.
            cursor.close();

            //Wyświetlenie potwierdzenia pomyślnego wykonania operacji
            Toast toast = Toast.makeText(context, "Huraaa! Kartę dodano pomyślnie." , Toast.LENGTH_LONG);
            toast.show();
        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            Toast toast = Toast.makeText(context, "Ups, coś poszło nie tak... Może spróbuj jeszcze raz ?" + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            // Zakończenie połączenia z bazą danych.
            db.close();
        }
    }

    // Metoda pozwala na zmianę informacji dotyczących karty istniejącej w bazie danych.
    // Parametry metody to ID elementu, który należy zmienić i obiekt klasy ContentValues
    // zawierający nowe dane.
    public void updateCard(String item_id, ContentValues cv){

        // Inicjalizacja kursora
        Cursor cursor = null;

        try{
            // Inicjalizacja bazy danych
            mDbHelper = new ReceiptDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            // Definicja kolumn z których dane należy pobrać
            String[] projection = {
                    CardContract.Card_Categories._ID,
                    CardContract.Card_Categories.CATEGORY_NAME
            };

            // Klauzula WHERE dla zapytania do tabeli kategorii kart
            String selection = CardContract.Card_Categories.CATEGORY_NAME + " = ?";
            // Parametry dla klauzuli WHERE dla zapytania do tabeli kategorii kart
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            // Klauzula WHERE dla zapytania dotyczącego tabeli kart
            String selectedReceipt = CardContract.Card._ID + " = ?";
            // Parametry dla klauzuli WHERE dotyczącej tabeli kart
            String[] args = new String[]{
                    item_id
            };

            // Pobranie danych z tabeli kategorii i zapisanie ich w kursorze
            cursor = db.query(
                    CardContract.Card_Categories.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            // Przesunięcie kursora na pierwszy element
            cursor.moveToFirst();

            // Jeżeli kategoria, która została przypisana karcie istnieje w bazie danych aktualizuj
            // wpis w tabeli kart. W przeciwnym wypadku zapisz nową kategorię w tabeli kategorii, a
            // następnie aktualizuj rekord w tabeli kart.
            if((cursor != null) && (cursor.getCount() > 0)){
                db.update(CardContract.Card.TABLE_NAME, cv, selectedReceipt, args); //dodanie rekordu do bazy danych
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(CardContract.Card_Categories.CATEGORY_NAME, cv.get("category").toString().toLowerCase());
                db.insert(CardContract.Card_Categories.TABLE_NAME, null, newCategoryValue);
                db.update(CardContract.Card.TABLE_NAME, cv, selectedReceipt, args);
            }
        }finally {
            // Zamknięcie kursora i połączenia z bazą danych
            cursor.close();
            db.close();
        }
    }

    // Metoda wyświetlająca rekordy z tabeli kart na interfejsie użytkownika.
    // Jako argumenty przyjmuje obiekt klasy ListView i zapytanie do bazy danych określające
    // jaki warunek muszą spełniać wpisy które zostaną wyświetlone.
    public void populateList(ListView lv, String query){
        //inizjalizacja połączenia z bazą danych
        mDbHelper = new ReceiptDbHelper(context);
        db = mDbHelper.getWritableDatabase();

        //Deklaracja kolumn z których dane należy pobrać
        String[] cols = new String[]{
                CardContract.Card._ID,
                CardContract.Card.NAME,
                CardContract.Card.CATEGORY,
                CardContract.Card.EXPIRATION_DATE,
                CardContract.Card.IMAGE_PATH
        };

        // Deklaracja kursora
        Cursor c;

        // Jeżeli zapytanie jest puste (nie ma warunków, które muszą spełniać wpisy) pobierz wszystkie
        // rekordy z tabeli. W przeciwnym razie wykonaj zapytanie do bazy danych podane w argumencie
        // query.
        if(query == null){
            c = db.query(true, CardContract.Card.TABLE_NAME, cols,null, null, null, null, null, null);
        }else{
            c = db.rawQuery(query, null);
        }

        try{
            // Wyszyść listę ID pobranych rekordów
            itemIds.clear();

            // Dodaj ID każdego pobranego wpisu do listy
            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(CardContract.Card._ID)));
            }

            // Deklaracja nazw kolumn, z których należy pobrać dane
            String[] from = new String[]{
                    CardContract.Card.NAME,
                    CardContract.Card.CATEGORY,
                    CardContract.Card.EXPIRATION_DATE,
                    CardContract.Card.IMAGE_PATH
            };

            // Deklaracja ID elementów interfejsu do których należy przekazać pobrane dane
            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.photoView
            };

            // Przesunięcie kursora na pozycję pierwszego elementu
            c.moveToFirst();

            // Ustawienie niestandardowego adaptera przyporządkowującego dane do odpowiednich elementów
            // interfejsu użytkownika i zapełniającego listę. Jako układ elementu wykorzystany jest
            // zasób definiujący układ dla paragonu, z drobnymi zmianami wprowadzanymi przez adapter.
            cardListAdapter = new CardListAdapter(context, R.layout.receipt_list_item, c, from, to, 0);
            lv.setAdapter(cardListAdapter);

        }finally {
            // Zamknięcie połączenia z bazą danych.
            db.close();
        }
    }

    // Metoda typu GET pozwalająca na pozyskanie z obiektu klasy listy ID rekordów pobranych z bazy
    // danych
    public ArrayList<Integer> getItemIds(){
        return this.itemIds;
    }

    // Metoda pozwalająca na filtrowanie zawartości listy kart lojalnościowych
    public void filterList(Bundle extras){

        // Pobranie danych z zestawu podanego jako argument funkcji.
        String reset = extras.getString("Reset");
        chosenCategory = extras.getString("Chosen_Category");

        // Jeżeli nie nastąpił reset, filtruj zawartość listy według kategorii wpisu i sygnalizuj
        // ustawienie filtrów poprzez ustawienie zmiennej resetFilters na false. W przeciwnym
        // wypadku ustaw zmienną na true, co sygnalizuje reset filtrów.
        if(reset.equals("false")){
            resetFilters = false;
                // jeśli podana kategoria to "Brak kategorii" zapytanie jest puste (wyświetlają
                // się wszystkie wpisy ). W przeciwnym wypadku zdefiniuj zapytanie, które spowoduje
                // pobranie z bazy danych tylko tych rekordów, których kategoria zgadza się z podaną
                // przez użytkownika
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

    // Metoda typu GET pozwalająca na pobranie informacji o tym, czy lista jest filtrowana.
    public boolean getResetFilters(){
        return this.resetFilters;
    }

    // Metoda typu GET pozwalająca na pobranie ustawionego zapytania do bazy danych.
    public String getQuery(){
        return this.query;
    }

    // Metoda pozwalająca na przeszukiwanie tabeli kart i wyświetlenie wpisów pasujących do zapytania
    public void search(ListView lv, String query){

        // Inicjalizacja połączeia z bazą danych
        mDbHelper = new ReceiptDbHelper(context);
        db = mDbHelper.getReadableDatabase();

        // Ustawienie zapytania do bazy danych
        String dbQuery = "SELECT * FROM " + CardContract.Card.TABLE_NAME + " WHERE " + CardContract.Card.NAME +
                " LIKE '%" + query + "%'";

        // Deklaracja i pobranie danych do kursora
        Cursor c = db.rawQuery(dbQuery, null);

        try {
            // Wyczyszczenie listy ID rekordów
            itemIds.clear();

            // Wpisanie ID pobranych rekordów na listę
            while (c.moveToNext()) {
                itemIds.add(c.getInt(c.getColumnIndex(CardContract.Card._ID)));
            }

            // Deklaracja kolumn z których należy pobrać dane
            String[] from = new String[]{
                    CardContract.Card.NAME,
                    CardContract.Card.CATEGORY,
                    CardContract.Card.EXPIRATION_DATE,
                    CardContract.Card.IMAGE_PATH
            };

            // Deklaracja elementów interfejsów do których mają być mapowane pobrane dane
            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.photoView
            };

            // Przesunięcie pozycji kursora na pierwszy element
            c.moveToFirst();

            // Ustawienie adaptera mapującego dane na elementy interfejsu i zapełniającego listę.
            cardListAdapter = new CardListAdapter(context, R.layout.receipt_list_item, c, from, to, 0);
            lv.setAdapter(cardListAdapter);

        } finally {
            // Zamknięcie połączenia z bazą danych
            db.close();
        }

    }

}
