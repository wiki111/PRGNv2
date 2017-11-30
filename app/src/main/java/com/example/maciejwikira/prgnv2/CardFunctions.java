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


// Klasa implementuje metody pozwalające na przetwarzanie danych dotyczących kart
// lojalnościowych.

public class CardFunctions {

    // Deklaracje zmiennych
    private Context context;
    private SQLiteDatabase db;
    private ReceiptDbHelper mDbHelper;
    private CardListAdapter cardListAdapter;
    private Toast toast;

    // Lista numerów ID elementów pobranych ostatnio z bazy danych
    private ArrayList<Integer> itemIds;

    // Aktywność filtrowania listy
    private boolean resetFilters;

    // Aktywne zapytanie do bazy danych
    private String query;

    // Wybrana kategoria
    private String chosenCategory;

    // W konstruktorze inicjalizowana jest lista ID elementów oraz
    // zapisywany jest kontekst aplikacji.
    public CardFunctions(Context context){
        this.context = context;
        itemIds = new ArrayList<>();
        // Lista nie jest filtrowana
        resetFilters = true;
        // Domyślnie nie istnieje zapytanie do bazy danych -
        // wyświetlane są wszystkie wpisy.
        query = null;
    }

    // Metoda dodająca nowy wpis do tabeli kart lojalnościowych.
    // Parametr cv to dane wpisu.
    public void addCard(ContentValues cv){
        try{
            // Inicjalizacja bazy danych
            mDbHelper = new ReceiptDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            // Definicja parametrów dla klauzuli WHERE
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            // Inicjalizacja kursora i pobranie informacji z bazy danych
            Cursor cursor = db.query(
                    CardContract.Card_Categories.TABLE_NAME,
                    Constants.cardCategoriesProjection,
                    Constants.cardCategoriesSelection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            if((cursor != null) && (cursor.getCount() > 0)){
                db.insert(CardContract.Card.TABLE_NAME, null, cv);
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(
                        CardContract.Card_Categories.CATEGORY_NAME,
                        cv.get("category").toString().toLowerCase()
                );
                db.insert(
                        CardContract.Card_Categories.TABLE_NAME,
                        null,
                        newCategoryValue
                );
                db.insert(
                        CardContract.Card.TABLE_NAME,
                        null,
                        cv
                );
            }

            // Zamnknięcie kursora.
            cursor.close();

            //Wyświetlenie potwierdzenia pomyślnego wykonania operacji
            toast = Toast.makeText(context, R.string.toast_add_new_card_success, Toast.LENGTH_LONG);
            toast.show();
        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(context, R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            // Zakończenie połączenia z bazą danych.
            db.close();
        }
    }

    // Metoda aktualizujaca istniejący element bazy danych o numerze ID
    // podanym w parametrze item_id za pomocą danych zawartych w
    // parametrze cv.
    public void updateCard(String item_id, ContentValues cv){

        try{
            // Inicjalizacja bazy danych
            mDbHelper = new ReceiptDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            // Parametry dla klauzuli WHERE dla zapytania do tabeli kategorii kart
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            // Parametry dla klauzuli WHERE dotyczącej tabeli kart
            String[] args = new String[]{
                    item_id
            };

            // Pobranie danych z tabeli kategorii i zapisanie ich w kursorze
            Cursor cursor = db.query(
                    CardContract.Card_Categories.TABLE_NAME,
                    Constants.cardCategoriesProjection,
                    Constants.cardCategoriesSelection,
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
                db.update(
                        CardContract.Card.TABLE_NAME,
                        cv,
                        Constants.cardSelection,
                        args
                ); //dodanie rekordu do bazy danych
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(
                        CardContract.Card_Categories.CATEGORY_NAME,
                        cv.get("category").toString().toLowerCase()
                );
                db.insert(
                        CardContract.Card_Categories.TABLE_NAME,
                        null,
                        newCategoryValue
                );
                db.update(
                        CardContract.Card.TABLE_NAME,
                        cv,
                        Constants.cardSelection,
                        args
                );
            }

            // Zamknięcie kursora
            cursor.close();
        }finally {
            // Zamknięcie połączenia z bazą danych
            db.close();
        }
    }

    // Metoda wyświetlająca rekordy z tabeli kart na interfejsie użytkownika.
    // W parametrze query przekazywane jest opcjonalne zapytanie do bazy danych,
    // które definiuje warunki jakie muszą spełniać wpisy.
    public void populateList(ListView lv, String query){
        //inizjalizacja połączenia z bazą danych
        mDbHelper = new ReceiptDbHelper(context);
        db = mDbHelper.getWritableDatabase();

        // Deklaracja kursora
        Cursor c;

        // Jeżeli zapytanie jest puste (nie ma warunków, które muszą spełniać wpisy) pobierz wszystkie
        // rekordy z tabeli. W przeciwnym razie wykonaj zapytanie do bazy danych podane w argumencie
        // query.
        if(query != null){
            c = db.rawQuery(query, null);
        }else if(query == null && this.query != null){
            c = db.rawQuery(this.query, null);
        }else{
            c = db.query(
                    true,
                    CardContract.Card.TABLE_NAME,
                    Constants.cardTableCols,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        try{
            // Wyszyść listę ID pobranych rekordów
            itemIds.clear();

            // Dodaj ID każdego pobranego wpisu do listy
            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(CardContract.Card._ID)));
            }

            // Przesunięcie kursora na pozycję pierwszego elementu
            c.moveToFirst();

            // Ustawienie niestandardowego adaptera przyporządkowującego dane do odpowiednich elementów
            // interfejsu użytkownika i zapełniającego listę. Jako układ elementu wykorzystany jest
            // zasób definiujący układ dla paragonu, z drobnymi zmianami wprowadzanymi przez adapter.
            cardListAdapter = new CardListAdapter(
                    context,
                    R.layout.list_item,
                    c,
                    Constants.fromCardTable,
                    Constants.toCardTable,
                    0
            );
            lv.setAdapter(cardListAdapter);

        }finally {
            // Zamknięcie połączenia z bazą danych.
            db.close();
        }
    }

    // Metoda zwraca listę numerów ID pobranych elementów.
    public ArrayList<Integer> getItemIds(){
        return this.itemIds;
    }

    // Metoda filtrująca listę kart za pomocą wartości podanych w parametrze extras.
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
                    query =
                            "SELECT * FROM " +
                            CardContract.Card.TABLE_NAME +
                            " WHERE " +
                            CardContract.Card.CATEGORY +
                            " = '" + chosenCategory + "'";
                }
        }else{
            resetFilters = true;
        }
    }

    // Metoda pozwalająca na przeszukiwanie tabeli kart i wyświetlenie wpisów pasujących do zapytania
    public void search(ListView lv, String query){

        // Inicjalizacja połączeia z bazą danych
        mDbHelper = new ReceiptDbHelper(context);
        db = mDbHelper.getReadableDatabase();

        // Ustawienie zapytania do bazy danych
        String dbQuery =
                "SELECT * FROM " +
                CardContract.Card.TABLE_NAME +
                " WHERE " +
                CardContract.Card.NAME +
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

            // Przesunięcie pozycji kursora na pierwszy element
            c.moveToFirst();

            // Ustawienie adaptera mapującego dane na elementy interfejsu i zapełniającego listę.
            cardListAdapter = new CardListAdapter(
                    context,
                    R.layout.list_item,
                    c,
                    Constants.fromCardTable,
                    Constants.toCardTable,
                    0
            );
            lv.setAdapter(cardListAdapter);

        } finally {
            // Zamknięcie połączenia z bazą danych
            db.close();
        }

    }

}
