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
 * Created by Maciej on 2017-10-30.
 */

// Klasa implementuje funkcje pozwalające na przetwarzanie i zapisywanie danych dotyczących paragonów
public class ReceiptFunctions {

    // Deklaracje zmiennych
    private SQLiteDatabase db;
    private ReceiptDbHelper mDbHelper;
    private ReceiptListAdapter receiptListAdapter;
    private Context context;
    private boolean resetFilters;
    private Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
    private String query;
    private Matcher matcherFrom;
    private Matcher matcherTo;
    private String chosenCategory;
    private String chosenFromDate;
    private String chosenToDate;
    private ArrayList<Integer> itemIds;
    private  Toast toast;


    // W konstruktorze następuje inicjalizacja listy przechowującej ID pobranych elementów
    // oraz zmiennych resetFilters i query.
    public ReceiptFunctions(Context context){
        this.context = context;
        itemIds = new ArrayList<>();
        // Lista nie jest domyślnie filtrowana
        resetFilters = true;
        // Domyślnie nie istnieje zapytanie do bazy danych -
        // wyświetlane są wszystkie wpisy.
        query = null;
    }

    // Metoda dodaje paragon do bazy danych
    public void addParagon(ContentValues cv){
        try{
            // Inicjalizacja połączenia z bazą danych
            mDbHelper = new ReceiptDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };

            // Inicjalizacja kursora i pobranie informacji z bazy danych
            Cursor cursor = db.query(
                    ReceiptContract.Categories.TABLE_NAME,
                    Constants.receiptCategoriesProjection,
                    Constants.receiptCategoriesSelection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            if((cursor != null) && (cursor.getCount() > 0)){
                db.insert(ReceiptContract.Receipt.TABLE_NAME, null, cv);
            }else{
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(
                        ReceiptContract.Categories.CATEGORY_NAME,
                        cv.get("category").toString().toLowerCase()
                );
                db.insert(
                        ReceiptContract.Categories.TABLE_NAME,
                        null,
                        newCategoryValue
                );
                db.insert(
                        ReceiptContract.Receipt.TABLE_NAME,
                        null,
                        cv
                );
            }

            cursor.close();

            //Wyświetlenie potwierdzenia pomyślnego wykonania operacji
            toast = Toast.makeText(context, R.string.toast_add_new_receipt_success , Toast.LENGTH_LONG);
            toast.show();
        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(context, R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            // Zamknięcie połączenia z bazą danych
            db.close();
        }
    }

    // Metoda pozwala na edycję danych istniejącego paragonu
    public void updateParagon(String item_id, ContentValues cv){

        // Inicjalizacja kursora
        Cursor cursor = null;

        try{
            // Inicjalizacja połączenia z bazą danych
            mDbHelper = new ReceiptDbHelper(context);
            db = mDbHelper.getWritableDatabase();

            // Deklaracja z których kolumn mają być pobrane dane
            String[] projection = {
                    ReceiptContract.Categories._ID,
                    ReceiptContract.Categories.CATEGORY_NAME
            };

            // Określenie klauzuli where i jej parametrów dla pobieranych kategorii
            String selection = ReceiptContract.Categories.CATEGORY_NAME + " = ?";
            String[] selectionArgs = { cv.get("category").toString().toLowerCase() };



            // Pobranie listy kategorii
            cursor = db.query(
                    ReceiptContract.Categories.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            // Określenie klauzuli where i jej parametrów dla pobieranego paragonu
            String selectedParagon = ReceiptContract.Receipt._ID + " = ?";
            String[] args = new String[]{
                    item_id
            };

            // Jeśli kategoria istnieje po prostu zapisz nowe dane
            if((cursor != null) && (cursor.getCount() > 0)){
                db.update(ReceiptContract.Receipt.TABLE_NAME, cv, selectedParagon, args); //dodanie rekordu do bazy danych
            }else{
                // W przeciwnym wypadku dodaj nową kategorię, a następnie zapisz nowe dane paragonu.
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(
                        ReceiptContract.Categories.CATEGORY_NAME,
                        cv.get("category").toString().toLowerCase()
                );
                db.insert(ReceiptContract.Categories.TABLE_NAME, null, newCategoryValue);
                db.update(ReceiptContract.Receipt.TABLE_NAME, cv, selectedParagon, args);
            }
        }finally {
            // Zamknij kursor i połączenie z bazą danych
            cursor.close();
            db.close();
        }
    }

    // Metoda zapełnia listę paragonów
    public void populateList(ListView lv, String query){

        // Inicjalizacja połączenia z bazą danych
        mDbHelper = new ReceiptDbHelper(context);
        db = mDbHelper.getWritableDatabase();

        //Deklaracja kolumn z których mają być pobrane dane
        String[] cols = new String[]{
                ReceiptContract.Receipt._ID,
                ReceiptContract.Receipt.NAME,
                ReceiptContract.Receipt.CATEGORY,
                ReceiptContract.Receipt.VALUE,
                ReceiptContract.Receipt.DATE,
                ReceiptContract.Receipt.IMAGE_PATH,
                ReceiptContract.Receipt.CONTENT,
                ReceiptContract.Receipt.FAVORITED
        };

        Cursor c;

        // Wykonanie zapytania do bazy danych. Jeśli w zmiennej query podane zostało zapytanie
        // inne niż standardowe wykonaj je.
        if(query == null){
            c = db.query(true, ReceiptContract.Receipt.TABLE_NAME, cols,null, null, null, null, null, null);
        }else{
            c = db.rawQuery(query, null);
        }

        try{
            // Wyczyść obecną listę id pobranych elementów
            itemIds.clear();

            // Zapisz id pobranych elementów do listy
            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(ReceiptContract.Receipt._ID)));
            }

            // Deklaracja z których kolumn mają być pobrane dane
            String[] from = new String[]{
                    ReceiptContract.Receipt.NAME,
                    ReceiptContract.Receipt.CATEGORY,
                    ReceiptContract.Receipt.DATE,
                    ReceiptContract.Receipt.VALUE,
                    ReceiptContract.Receipt.IMAGE_PATH,
                    ReceiptContract.Receipt.CONTENT,
                    ReceiptContract.Receipt.FAVORITED
            };

            // Deklaracja elementów interfejsu do których mają być przyporządkowane
            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.photoView
            };

            c.moveToFirst();

            // Inicjalizacja i ustawienie adaptera listy
            receiptListAdapter = new ReceiptListAdapter(context, R.layout.list_item, c, from, to, 0);
            lv.setAdapter(receiptListAdapter);

        }finally {
            // Zakończenie połączenia z bazą danych
            db.close();
        }

    }

    // Metoda pozwala na przeszukiwanie zapisanych danych paragonów
    public void search(ListView lv, String query){

        // Inicjalizacja połaczenia z bazą danych
        mDbHelper = new ReceiptDbHelper(context);
        db = mDbHelper.getReadableDatabase();

        // Definicja zapytania do bazy danych. Pobierane są tylko te wpisy, których nazwa lub
        // zapisana zawartość paragonu zawierają wartość podaną przez użytkownika
        String dbQuery = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " + ReceiptContract.Receipt.NAME +
                " LIKE '" + query + "' OR " + ReceiptContract.Receipt.CONTENT + " LIKE '%" + query + "%'";

        Cursor c;

        // Wykonanie zapytania
        c = db.rawQuery(dbQuery, null);

        try {
            // Wyczyszczenie listy zapisanych ID wpisów
            itemIds.clear();

            // Zapisanie ID pobranych wpisów do listy
            while(c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(ReceiptContract.Receipt._ID)));
            }

            // Deklaracja z których kolumn mają być pobrane dane
            String[] from = new String[]{
                    ReceiptContract.Receipt.NAME,
                    ReceiptContract.Receipt.CATEGORY,
                    ReceiptContract.Receipt.DATE,
                    ReceiptContract.Receipt.VALUE,
                    ReceiptContract.Receipt.IMAGE_PATH,
                    ReceiptContract.Receipt.CONTENT,
                    ReceiptContract.Receipt.FAVORITED
            };

            // Deklaracja elementów interfejsu do których mają być przyporządkowane
            int[] to = new int[]{
                    R.id.nameTextView,
                    R.id.categoryTextView,
                    R.id.dateView,
                    R.id.valTextView,
                    R.id.photoView
            };

            c.moveToFirst();

            //Zapełnienie listy wynikami wyszukiwania
            receiptListAdapter = new ReceiptListAdapter(context, R.layout.list_item, c, from, to, 0);
            lv.setAdapter(receiptListAdapter);
        } finally {
            db.close();
        }
    }

    // Metoda wykonuje filtrowanie listy wpisów
    public void filterList(Bundle extras){

        // Pobranie danych
        String reset = extras.getString("Reset");
        chosenCategory = extras.getString("Chosen_Category");
        chosenFromDate = extras.getString("Chosen_From_Date");
        chosenToDate = extras.getString("Chosen_To_Date");

        // Jeśli filtry nie zostały zresetowane
        if(reset.equals("false")){
            // Zapisz informację o ustawieniu filtrów
            resetFilters = false;
            // Jeśli nie została podana data ani kategoria nie definiuj nowego zapytania
            if(chosenFromDate.equals("YYYY-MM-DD") && chosenToDate.equals("YYYY-MM-DD")){
                if(chosenCategory.equals("Brak Kategorii")){
                    query = null;
                }else{
                    // Jeśli podana została kategoria  skonstruuj zapytanie, które spowoduje pobranie z bazy
                    // danych odpowiednich wpisów.
                    query = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " +
                            ReceiptContract.Receipt.CATEGORY + " = '" + chosenCategory + "'";
                }
                // Jeśli został podany przedział czasowy
            }else{
                // Inicjalizacja obiektów porównujących wartości
                matcherFrom = datePattern.matcher(chosenFromDate);
                matcherTo = datePattern.matcher(chosenToDate);
                // Jeśli podane daty są prawidłowe
                if(matcherFrom.find() && matcherTo.find()){
                    // Zapisz daty w odpowiednim formacie
                    String from = "'" + chosenFromDate + "'";
                    String to = "'" + chosenToDate + "'";
                    // Jeżeli nie została wybrana kategoria
                    if(!chosenCategory.equals("Brak Kategorii")){
                        // Skonstruuj zapytanie, które spowoduje pobranie wpisów mieszczących sie w
                        // w danym przedziale czasowym.
                        query = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " + ReceiptContract.Receipt.CATEGORY + " = '" + chosenCategory + "' AND " + ReceiptContract.Receipt.DATE + " >= " + from
                                + " AND " + ReceiptContract.Receipt.DATE + " <= " + to;
                    }else{
                        // Jeśli kategoria została podana uwzględnij ją w zapytaniu razem
                        // z przedziałem czasowym
                        query = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " + ReceiptContract.Receipt.DATE + " >= " + from
                                + " AND " + ReceiptContract.Receipt.DATE + " <= " + to;
                    }
                }else{
                    // Jeśli podana data jest nieprawidłowa poinformuj o tym użytkownika
                    Toast tst = Toast.makeText(context, "Nieprawidłowa data - spróbuj jeszcze raz.", Toast.LENGTH_LONG);
                    tst.show();
                    // jeżeli została wybrana kategoria
                    if(!chosenCategory.equals("Brak Kategorii")){
                        // Skonstruuj zapytanie, które spowoduje pobranie elementów z danej kategorii
                        query = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " +
                                ReceiptContract.Receipt.CATEGORY + " = '" + chosenCategory + "'";
                    }else{
                        // Jeżeli nie została podana kategoria ani przedział czasowy nie konstruuj
                        // nowego zapytania
                        query = null;
                    }
                }
            }
        }else{
            // Jeżeli filtry zostały zresetowane zachowaj o tym informację
            resetFilters = true;
        }
    }

    // Metoda typu GET pozwalająca na sprawdzenie, czy są aktywne filtry
    public boolean getResetFilters(){
        return this.resetFilters;
    }

    // Metoda typu GET pozwalająca na pobranie ostatniego zapisanego zapytania
    public String getQuery(){
        return this.query;
    }

    // Metoda typu GET pozwalająca na pobranie aktywnej listy ID paragonów
    public ArrayList<Integer> getItemIds(){
        return this.itemIds;
    }

}
