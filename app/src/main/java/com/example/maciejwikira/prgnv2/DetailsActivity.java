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

// Aktywność obsługuje wyświetlanie szczegółów wpisu
public class DetailsActivity extends AppCompatActivity {

    // Deklaracja zmiennych
    private ReceiptDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String selection;
    private String[] selectionArgs;
    private String[] receiptCols;
    private String[] cardCols;
    private String id;
    private TextView nameTextView;
    private TextView categoryTextView;
    private TextView dateTextView;
    private TextView valueTextView;
    private ImageView receiptPhotoDetailsView;
    private String bitmapPath;
    private Bitmap receiptPhoto;
    private boolean showReceipts;
    private ReceiptFunctions pf;
    private CardFunctions cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Inicjalizacja bazy danych
        mDbHelper = new ReceiptDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        // Kolumny tabeli paragonów z których ma zostać pobrana zawartość
        receiptCols = new String[]{
                ReceiptContract.Receipt._ID,
                ReceiptContract.Receipt.NAME,
                ReceiptContract.Receipt.CATEGORY,
                ReceiptContract.Receipt.VALUE,
                ReceiptContract.Receipt.DATE,
                ReceiptContract.Receipt.IMAGE_PATH,
                ReceiptContract.Receipt.CONTENT
        };

        // Kolumny tabeli kart z których ma zostać pobrana zawartość
        cardCols = new String[]{
                CardContract.Card._ID,
                CardContract.Card.NAME,
                CardContract.Card.CATEGORY,
                CardContract.Card.EXPIRATION_DATE,
                CardContract.Card.IMAGE_PATH
        };


        // Deklaracja klas obsługujących funkcje związanie z przetwarzaniem danych o
        // paragonach i kartach
        pf = new ReceiptFunctions(getApplicationContext());
        cf = new CardFunctions(getApplicationContext());

        // Pobranie ID elementu do wyświetlenia i trybu aplikacji
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        id = extras.getString("item_id");
        showReceipts = extras.getBoolean(MainViewActivity.CARDS_OR_PARAGONS);


        // Jeśli aplikacja jest w trybie paragonów wykonaj zapytanie które spowoduje zwrócenie
        // danych elementu o podanym ID z tabeli paragonów. W przeciwnym razie pobierz odpowiednie
        // dane z tabeli kart.
        if(showReceipts == true) {
           selection = ReceiptContract.Receipt._ID + " = ?";
           selectionArgs = new String[]{ id };
           c = db.query(ReceiptContract.Receipt.TABLE_NAME, receiptCols, selection, selectionArgs, null, null, null);
        }else{
           selection = CardContract.Card._ID + " = ?";
           selectionArgs = new String[]{ id };
           c = db.query(CardContract.Card.TABLE_NAME, cardCols, selection, selectionArgs, null, null, null);
        }

        // Przesun kursor na pierwszą pozycję
        c.moveToFirst();

        // Pobranie elementów interfejsu użytkownika
        nameTextView = (TextView) findViewById(R.id.nameTextView);
        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        dateTextView = (TextView)findViewById(R.id.dateTextView);
        valueTextView = (TextView)findViewById(R.id.valueTextView);
        receiptPhotoDetailsView = (ImageView)findViewById(R.id.paragonPhotoDetailsView);

        // Ustaw zawartość elementów interfejsu
        setViewContents();

        // Ustawienie nasłuchiwania na zdarzenie kliknięcia zdjęcia. Gdy to nastąpi, obraz jest
        // wyświetlany na całym ekranie w osobnej aktywności.
        receiptPhotoDetailsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayPhotoActivity.class);
                intent.putExtra("BitmapPath", bitmapPath);
                startActivity(intent);
            }
        });

        // Przycisk powoduje przejście do aktywności edycji paragonu
        Button goToEditButton = (Button)findViewById(R.id.goToEditButton);
        goToEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                Bundle extras = new Bundle();
                // Przekazanie trybu aplikacji, oraz id elementu
                extras.putBoolean(MainViewActivity.CARDS_OR_PARAGONS, showReceipts);
                extras.putString("item_id", id);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        // Przycisk odpowiadajacy za dodanie elementu do polubionych
        Button favoriteButton = (Button)findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                Toast toast;

                // Jeśli element jest paragonem, zaktualizuj wpis o danym ID w tabeli paragonów. W
                // przeciwnym razie wykonaj analogiczną operację na tabeli kart.
                if(showReceipts == true) {
                    contentValues.put(ReceiptContract.Receipt.NAME, nameTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.DATE, dateTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.VALUE, valueTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.IMAGE_PATH, c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH)));
                    contentValues.put(ReceiptContract.Receipt.CONTENT, c.getString(c.getColumnIndex(ReceiptContract.Receipt.CONTENT)));
                    contentValues.put(ReceiptContract.Receipt.FAVORITED, "yes");
                    pf.updateParagon(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Receipt dodano do ulubionych", Toast.LENGTH_SHORT);
                }else{
                    contentValues.put(CardContract.Card.NAME, nameTextView.getText().toString());
                    contentValues.put(CardContract.Card.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(CardContract.Card.EXPIRATION_DATE, dateTextView.getText().toString());
                    contentValues.put(CardContract.Card.IMAGE_PATH, c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
                    contentValues.put(CardContract.Card.FAVORITED, "yes");
                    cf.updateCard(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Kartę dodano do ulubionych", Toast.LENGTH_SHORT);
                }

                // Wyświetl potwierdzenie dodania do ulubionych
                toast.show();
            }
        });

        // Przycisk powoduje skasowanie elementu
        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Wyświetlenie dialogu z prośbą o potwierdzenie akcji przez użytkownika
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetailsActivity.this, R.style.myDialog));
                if(showReceipts == true)
                    builder.setMessage("Czy na pewno chcesz skasować ten paragon ?").setTitle("Kasowanie paragonu.");
                else
                    builder.setMessage("Czy na pewno chcesz skasować tą kartę ?").setTitle("Kasowanie karty.");


                // Jeśli użytkownik potwierdzi operację, skasuj wpis
                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(showReceipts == true)
                            db.delete(ReceiptContract.Receipt.TABLE_NAME, selection, selectionArgs);
                        else
                            db.delete(CardContract.Card.TABLE_NAME, selection, selectionArgs);
                        finish();
                    }
                });
                builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Nie rób nic.
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Przycisk powoduje usunięcie elementu z ulubionych
        Button unfavoriteButton = (Button)findViewById(R.id.unfavoriteButton);
        unfavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                Toast toast;

                if(showReceipts == true) {
                    contentValues.put(ReceiptContract.Receipt.NAME, nameTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.DATE, dateTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.VALUE, valueTextView.getText().toString());
                    contentValues.put(ReceiptContract.Receipt.IMAGE_PATH, c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH)));
                    contentValues.put(ReceiptContract.Receipt.CONTENT, c.getString(c.getColumnIndex(ReceiptContract.Receipt.CONTENT)));
                    contentValues.put(ReceiptContract.Receipt.FAVORITED, "no");
                    pf.updateParagon(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Receipt usunięto z ulubionych", Toast.LENGTH_SHORT);
                }else{
                    contentValues.put(CardContract.Card.NAME, nameTextView.getText().toString());
                    contentValues.put(CardContract.Card.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(CardContract.Card.EXPIRATION_DATE, dateTextView.getText().toString());
                    contentValues.put(CardContract.Card.IMAGE_PATH, c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
                    contentValues.put(CardContract.Card.FAVORITED, "no");
                    cf.updateCard(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Kartę usunięto z ulubionych", Toast.LENGTH_SHORT);
                }
                // Wyświetl potwierdzenie dla użytkownika
                toast.show();
            }
        });

    }

    @Override
    protected void onResume(){

        // Wyświetl odpowiedni element o danym ID - z tabeli paragonów lub kart
        if(showReceipts == true) {
            c = db.query(ReceiptContract.Receipt.TABLE_NAME, receiptCols, selection, selectionArgs, null, null, null);
        }else {
            c = db.query(CardContract.Card.TABLE_NAME, cardCols, selection, selectionArgs, null, null, null);
        }
        c.moveToFirst();

        // Ustaw zawartość elementów interfejsu użytkownika
        setViewContents();

        super.onResume();
    }

    @Override
    protected void onDestroy(){
        // Przy niszczeniu aktywności zamknij kursor i połączenie z bazą danych
        c.close();
        db.close();
        super.onDestroy();
    }

    // Metoda ustawia zawartość poszczególnych pól i widoków interfejsu użytkownika
    private void setViewContents(){
        if(showReceipts == true){
            nameTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.NAME)));
            categoryTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
            dateTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.DATE)));
            valueTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Receipt.VALUE)));
            bitmapPath = c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH));
            receiptPhoto = BitmapFactory.decodeFile(bitmapPath);
            receiptPhotoDetailsView.setImageBitmap(receiptPhoto);
        }else {
            nameTextView.setText(c.getString(c.getColumnIndex(CardContract.Card.NAME)));
            categoryTextView.setText(c.getString(c.getColumnIndex(CardContract.Card.CATEGORY)));
            dateTextView.setText(c.getString(c.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            bitmapPath = c.getString(c.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH));
            receiptPhoto = BitmapFactory.decodeFile(bitmapPath);
            receiptPhotoDetailsView.setImageBitmap(receiptPhoto);

            valueTextView.setVisibility(View.INVISIBLE);
        }
    }
}
