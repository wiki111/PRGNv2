package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

// Aktywność wyświetla szczegóły wpisu.
public class DetailsActivity extends AppCompatActivity {

    private ReceiptDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;

    private boolean showReceipts;

    // ID elementu, którego szczegóły są wyświetlane.
    private String id;
    private String bitmapPath;

    // Kolumny odpowiedniej tabeli, z których należy pobrać dane.
    private String[] projection;

    // Klauzula WHERE dla zapytania do bazy danych.
    private String selection;

    // Argumenty dla klauzuli WHERE.
    private String[] selectionArgs;

    // Nazwa odpowiedniej tabeli.
    private String itemTable;

    private TextView nameTextView;
    private TextView categoryTextView;
    private TextView dateTextView;
    private TextView valueTextView;
    private TextView descriptionTextView;

    private ImageView receiptPhotoDetailsView;
    private ImageView favoritedIconView;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Inicjalizacja połączenia z bazą danych
        mDbHelper = new ReceiptDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();


        // Pobranie ID elementu do wyświetlenia i trybu w którym działa
        // aplikacja (wyświetlanie kart lub paragonów).
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        id = extras.getString("item_id");
        showReceipts = extras.getBoolean(MainViewActivity.CARDS_OR_RECEIPTS);

        // W zależności od aktywnego trybu ustawiane są odpowiednie zmienne i wykonywane
        // jest zapytanie do bazy danych.
        setShowReceipts(showReceipts);

        c.moveToFirst();

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        dateTextView = (TextView)findViewById(R.id.dateTextView);
        valueTextView = (TextView)findViewById(R.id.valueTextView);
        descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
        receiptPhotoDetailsView = (ImageView)findViewById(R.id.paragonPhotoDetailsView);
        favoritedIconView = (ImageView)findViewById(R.id.favoritedIconView);

        // Ustawienie zawartości elementów interfejsu.
        setViewContents();

        // Ustawienie nasłuchiwania na zdarzenie kliknięcia zdjęcia. Gdy to nastąpi, obraz jest
        // wyświetlany na całym ekranie w osobnej aktywności.
        receiptPhotoDetailsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayPhotoActivity.class);
                Bundle extras = new Bundle();
                extras.putString("BitmapPath", bitmapPath);
                extras.putBoolean(MainViewActivity.CARDS_OR_RECEIPTS, showReceipts);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        // Przejście do edycji wpisu.
        Button goToEditButton = (Button)findViewById(R.id.goToEditButton);
        goToEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);

                Bundle extras = new Bundle();
                extras.putBoolean(MainViewActivity.CARDS_OR_RECEIPTS, showReceipts);
                extras.putString("item_id", id);
                extras.putBoolean(Constants.UPDATE, true);

                // Przekazanie danych wpisu.
                extras.putStringArrayList(Constants.ITEM_DATA, buildDataArray());

                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        // Dodanie wpisu do ulubionych.
        Button favoriteButton = (Button)findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(CardContract.Card.FAVORITED, "yes");

                db.update(itemTable, contentValues, selection, selectionArgs);

                toast = Toast.makeText(getApplicationContext(), "Dodano do ulubionych.", Toast.LENGTH_SHORT);
                toast.show();

                favoritedIconView.setVisibility(View.VISIBLE);
            }
        });

        // Usunięcie wpisu.
        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Wyświetlenie okna z prośbą o potwierdzenie.
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetailsActivity.this, R.style.myDialog));

                if(showReceipts == true)
                    builder.setMessage("Czy na pewno chcesz skasować ten paragon ?").setTitle("Kasowanie paragonu.");
                else
                    builder.setMessage("Czy na pewno chcesz skasować tą kartę ?").setTitle("Kasowanie karty.");

                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                            db.delete(itemTable, selection, selectionArgs);
                            finish();
                    }
                });

                builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Usunięcie wpisu z ulubionych.
        Button unfavoriteButton = (Button)findViewById(R.id.unfavoriteButton);
        unfavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(CardContract.Card.FAVORITED, "no");

                db.update(itemTable, contentValues, selection, selectionArgs);

                toast = Toast.makeText(getApplicationContext(), "Usunięto z ulubionych", Toast.LENGTH_SHORT);
                toast.show();

                favoritedIconView.setVisibility(View.INVISIBLE);
            }
        });

    }

    // Ustawienie nazwy tabeli, treści klauzuli WHERE i jej parametrów w zależności
    // od trybu aplikacji, oraz wykonanie zapytania do bazy danych i zapisanie
    // wyników w kursorze.
    public void setShowReceipts(boolean show){
        if(show){
            showReceipts = true;
            projection = Constants.receiptTableCols;
            selection = ReceiptContract.Receipt._ID + " = ?";
            itemTable = ReceiptContract.Receipt.TABLE_NAME;
        }else{
            showReceipts = false;
            projection = Constants.cardTableCols;
            selection = CardContract.Card._ID + " = ?";
            itemTable = CardContract.Card.TABLE_NAME;
        }

        selectionArgs = new String[]{ id };
        c = db.query(itemTable, projection, selection, selectionArgs, null, null, null);

    }

    // Przy wznowieniu aktywności dane wyświetlanego wpisu są ponownie pobierane i
    // zawartość elementów interfejsu jest aktualizowana.
    @Override
    protected void onResume(){
        c = db.query(itemTable, projection, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        setViewContents();
        super.onResume();
    }

    // Metoda ustawia zawartość poszczególnych elementów interfejsu użytkownika
    // w zależności od aktywnego trybu aplikacji.
    private void setViewContents(){
        if(showReceipts == true){
            nameTextView.setText("Nazwa : " + c.getString(c.getColumnIndex(ReceiptContract.Receipt.NAME)));
            categoryTextView.setText("Kategoria : " + c.getString(c.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
            dateTextView.setText("Data : " + c.getString(c.getColumnIndex(ReceiptContract.Receipt.DATE)));
            valueTextView.setText("Wartość : " +c.getString(c.getColumnIndex(ReceiptContract.Receipt.VALUE)));
            descriptionTextView.setText("Opis : " + c.getString(c.getColumnIndex(ReceiptContract.Receipt.DESCRIPTION)));
            bitmapPath = c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH));
            Glide.with(this).load(bitmapPath).into(receiptPhotoDetailsView);
            if(!c.getString(c.getColumnIndex(ReceiptContract.Receipt.FAVORITED)).equals("yes")){
                favoritedIconView.setVisibility(View.GONE);
            }else{
                favoritedIconView.setVisibility(View.VISIBLE);
            }
        }else {
            nameTextView.setText("Nazwa : " + c.getString(c.getColumnIndex(CardContract.Card.NAME)));
            categoryTextView.setText("Kategoria : " + c.getString(c.getColumnIndex(CardContract.Card.CATEGORY)));
            dateTextView.setText("Data : " + c.getString(c.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            descriptionTextView.setText("Opis : " + c.getString(c.getColumnIndex(CardContract.Card.DESCRIPTION)));
            bitmapPath = c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH));
            Glide.with(this).load(bitmapPath).into(receiptPhotoDetailsView);
            valueTextView.setVisibility(View.INVISIBLE);
            if(!c.getString(c.getColumnIndex(CardContract.Card.FAVORITED)).equals("yes")){
                favoritedIconView.setVisibility(View.GONE);
            }else{
                favoritedIconView.setVisibility(View.VISIBLE);
            }
        }

    }

    // Metoda zwraca listę danych wyświetlanego wpisu.
    private ArrayList<String> buildDataArray(){
        ArrayList<String> data = new ArrayList<>();
        if(showReceipts){
            data.add(0, c.getString(c.getColumnIndex(ReceiptContract.Receipt.NAME)));
            data.add(1, c.getString(c.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
            data.add(2, c.getString(c.getColumnIndex(ReceiptContract.Receipt.VALUE)));
            data.add(3, c.getString(c.getColumnIndex(ReceiptContract.Receipt.DATE)));
            data.add(4, c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH)));
            data.add(5, c.getString(c.getColumnIndex(ReceiptContract.Receipt.CONTENT)));
            data.add(6, c.getString(c.getColumnIndex(ReceiptContract.Receipt.FAVORITED)));
            data.add(7, c.getString(c.getColumnIndex(ReceiptContract.Receipt.DESCRIPTION)));
            data.add(8, c.getString(c.getColumnIndex(ReceiptContract.Receipt._ID)));
        }else{
            data.add(0, c.getString(c.getColumnIndex(CardContract.Card.NAME)));
            data.add(1, c.getString(c.getColumnIndex(CardContract.Card.CATEGORY)));
            data.add(2, c.getString(c.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            data.add(3, c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
            data.add(4, c.getString(c.getColumnIndex(CardContract.Card.FAVORITED)));
            data.add(5, c.getString(c.getColumnIndex(CardContract.Card.DESCRIPTION)));
            data.add(6, c.getString(c.getColumnIndex(CardContract.Card._ID)));
        }
        return data;
    }

    // Przy zakończeniu działania aktywności zamykany jest kursor oraz połączenie z bazą danych.
    @Override
    protected void onDestroy(){

        c.close();
        db.close();
        super.onDestroy();
    }

}
