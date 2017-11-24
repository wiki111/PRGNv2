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
import android.widget.EditText;
import android.widget.ImageView;

// Aktywność obsługuje edycję wpisu - paragonu lub karty
public class EditActivity extends AppCompatActivity {

    // Deklaracje zmiennych
    private ReceiptDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private boolean showReceipts;
    private String selection;
    private String[] selectionArgs;
    private String[] receiptCols;
    private String[] cardCols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Inicjalizacja połączenia z bazą danych
        mDbHelper = new ReceiptDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        // Kolumny tabeli paragonów z których zostaną pobrane dane
        receiptCols = new String[]{
                ReceiptContract.Receipt._ID,
                ReceiptContract.Receipt.NAME,
                ReceiptContract.Receipt.CATEGORY,
                ReceiptContract.Receipt.VALUE,
                ReceiptContract.Receipt.DATE,
                ReceiptContract.Receipt.IMAGE_PATH,
                ReceiptContract.Receipt.CONTENT,
                ReceiptContract.Receipt.FAVORITED
        };

        // Kolumny tabeli kart z których zostaną pobrane dane
        cardCols = new String[]{
                CardContract.Card._ID,
                CardContract.Card.NAME,
                CardContract.Card.CATEGORY,
                CardContract.Card.EXPIRATION_DATE,
                CardContract.Card.IMAGE_PATH,
                CardContract.Card.FAVORITED
        };


        // Pobranie id elementu i trybu aplikacji (tryb paragonów lub kart)
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String item_id = extras.getString("item_id");
        showReceipts = extras.getBoolean(MainViewActivity.CARDS_OR_PARAGONS);

        // Wykonanie zapytania do odpowiedniej tabeli bazy danych w zależności od trybu aplikacji
        if(showReceipts == true) {
            selection = ReceiptContract.Receipt._ID + " = ?";
            selectionArgs = new String[]{ item_id };
            c = db.query(ReceiptContract.Receipt.TABLE_NAME, receiptCols, selection, selectionArgs, null, null, null);
        }else{
            selection = CardContract.Card._ID + " = ?";
            selectionArgs = new String[]{ item_id };
            c = db.query(CardContract.Card.TABLE_NAME, cardCols, selection, selectionArgs, null, null, null);
        }

        // Przesunięcie pozycji kursora na pierwszy element
        c.moveToFirst();

        // Deklaracje elementów interfejsu
        final EditText nameEdit = (EditText)findViewById(R.id.nameEdit);
        final EditText categoryEdit = (EditText)findViewById(R.id.categoryEdit);
        final EditText dateEdit = (EditText)findViewById(R.id.dateEdit);
        final EditText valueEdit = (EditText)findViewById(R.id.valueEdit);
        ImageView paragonPhotoView = (ImageView)findViewById(R.id.paragonPhotoView);

        // Ustawienie zawartości pól i widoków interfejsu użytkownika
        if(showReceipts == true) {
            nameEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.NAME)));
            categoryEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
            dateEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.DATE)));
            valueEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.VALUE)));
            Bitmap paragonPhoto = BitmapFactory.decodeFile(c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH)));
            paragonPhotoView.setImageBitmap(paragonPhoto);

        }else {
            nameEdit.setText(c.getString(c.getColumnIndex(CardContract.Card.NAME)));
            categoryEdit.setText(c.getString(c.getColumnIndex(CardContract.Card.CATEGORY)));
            dateEdit.setText(c.getString(c.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            Bitmap paragonPhoto = BitmapFactory.decodeFile(c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
            paragonPhotoView.setImageBitmap(paragonPhoto);

            valueEdit.setVisibility(View.INVISIBLE);
        }

        // Deklaracja klas obsługujących przetwarzanie danych związanych z kartami i
        // paragonami.
        final ReceiptFunctions receiptFunctions = new ReceiptFunctions(getApplicationContext());
        final CardFunctions cardFunctions = new CardFunctions(getApplicationContext());

        // Przycisk powoduje nadpisanie danych o konkretnym elemencie w bazie danych
        Button updateButton = (Button)findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Ostrzeżenie - użytkownik proszony jest o potwierdzenie operacji
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(EditActivity.this, R.style.myDialog));
                if(showReceipts == true)
                    builder.setMessage("Czy na pewno chcesz zmienić ten paragon ?").setTitle("Edycja paragonu.");
                else
                    builder.setMessage("Czy na pewno chcesz zmienić tą kartę lojalnościową ?").setTitle("Edycja karty.");
                // Jeśli użytkownik potwierdzi operację wpis w bazie danych jest aktualizowany za pomocą wartości znajdujących się
                // obecnie w odpowiednich polach interfejsu.
                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        ContentValues cv = new ContentValues();

                        if(showReceipts == true){
                            cv.put(ReceiptContract.Receipt.NAME, nameEdit.getText().toString());
                            cv.put(ReceiptContract.Receipt.CATEGORY, categoryEdit.getText().toString().toLowerCase());
                            cv.put(ReceiptContract.Receipt.DATE, dateEdit.getText().toString());
                            cv.put(ReceiptContract.Receipt.VALUE, valueEdit.getText().toString());
                            cv.put(ReceiptContract.Receipt.IMAGE_PATH, c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH)));
                            cv.put(ReceiptContract.Receipt.CONTENT, c.getString(c.getColumnIndex(ReceiptContract.Receipt.CONTENT)));
                            cv.put(ReceiptContract.Receipt.FAVORITED, c.getString(c.getColumnIndex(ReceiptContract.Receipt.FAVORITED)));
                            receiptFunctions.updateParagon(item_id, cv);
                        }else{
                            cv.put(CardContract.Card.NAME, nameEdit.getText().toString());
                            cv.put(CardContract.Card.CATEGORY, categoryEdit.getText().toString().toLowerCase());
                            cv.put(CardContract.Card.EXPIRATION_DATE, dateEdit.getText().toString());
                            cv.put(CardContract.Card.IMAGE_PATH, c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
                            cv.put(CardContract.Card.FAVORITED, c.getString(c.getColumnIndex(CardContract.Card.FAVORITED)));
                            cardFunctions.updateCard(item_id, cv);
                        }

                        finish();
                    }
                });

                builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // W przeciwnym wypadku nic się nie dzieje
                    }
                });

                AlertDialog dialog = builder.create();
                // Wyświetlenie okna z prośbą o potwierdzenie operacji.
                dialog.show();
            }
        });

    }

    @Override
    protected void onDestroy(){
        // Przy zniszczeniu aktywności zamknij kursor i połączenie z bazą danych
        c.close();
        db.close();
        super.onDestroy();
    }

}
