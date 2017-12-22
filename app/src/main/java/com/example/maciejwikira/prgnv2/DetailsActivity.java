package com.example.maciejwikira.prgnv2;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.provider.ContactsContract;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

// Aktywność wyświetla szczegóły wpisu.
public class DetailsActivity extends AppCompatActivity {

    private ReceiptDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor tableCursor, photoCursor;

    private boolean showReceipts;

    // ID elementu, którego szczegóły są wyświetlane.
    private String itemId;
    private String bitmapPath;

    // Kolumny odpowiedniej tabeli, z których należy pobrać dane.
    private String[] projection;

    // Klauzula WHERE dla zapytania do bazy danych.
    private String selection;

    // Argumenty dla klauzuli WHERE.
    private String[] selectionArgs;

    // Nazwa odpowiedniej tabeli.
    private String itemTable;

    private String photoTable;
    private String[] photoProjection;
    private String photoSelection;
    private String photoColumn;
    private String valueString;

    private TextView nameTextView;
    private TextView categoryTextView;
    private TextView dateTextView;
    private TextView valueTextView;
    private TextView descriptionTextView;
    private TextView warrantyTextView;

    private ImageView left;
    private ImageView right;

    private ImageView receiptPhotoDetailsView;
    private ImageView favoritedIconView;
    private ImageView itemImageView;

    private ViewPagerImageAdapter viewPagerImageAdapter;

    private ViewPager viewPager;

    private Toast toast;

    ArrayList<String> imgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(001);

        // Inicjalizacja połączenia z bazą danych
        mDbHelper = new ReceiptDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        viewPager = (ViewPager)findViewById(R.id.viewPager);

        // Pobranie ID elementu do wyświetlenia i trybu w którym działa
        // aplikacja (wyświetlanie kart lub paragonów).
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        itemId = extras.getString("item_id");
        showReceipts = extras.getBoolean(MainViewActivity.CARDS_OR_RECEIPTS);

        // W zależności od aktywnego trybu ustawiane są odpowiednie zmienne i wykonywane
        // jest zapytanie do bazy danych.
        setShowReceipts(showReceipts);

        imgs = new ArrayList<>();

        tableCursor.moveToFirst();

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        dateTextView = (TextView)findViewById(R.id.dateTextView);
        valueTextView = (TextView)findViewById(R.id.valueTextView);
        descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
        favoritedIconView = (ImageView)findViewById(R.id.favoritedIconView);
        warrantyTextView = (TextView)findViewById(R.id.warrantyTextView);
        itemImageView = (ImageView)findViewById(R.id.itemImageView);
        left = (ImageView)findViewById(R.id.left);
        right = (ImageView)findViewById(R.id.right);

        setViewPagerAdapter(imgs);

        // Ustawienie zawartości elementów interfejsu.
        setViewContents();

        // Ustawienie nasłuchiwania na zdarzenie kliknięcia zdjęcia. Gdy to nastąpi, obraz jest
        // wyświetlany na całym ekranie w osobnej aktywności.
        /*
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
        */

        // Przejście do edycji wpisu.
        ImageButton goToEditButton = (ImageButton)findViewById(R.id.goToEditButton);
        goToEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);

                Bundle extras = new Bundle();
                extras.putBoolean(MainViewActivity.CARDS_OR_RECEIPTS, showReceipts);
                extras.putString("item_id", itemId);
                extras.putBoolean(Constants.UPDATE, true);

                // Przekazanie danych wpisu.
                extras.putStringArrayList(Constants.ITEM_DATA, buildDataArray());

                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        // Dodanie wpisu do ulubionych.
        ImageButton favoriteButton = (ImageButton)findViewById(R.id.favoriteButton);
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
        ImageButton deleteButton = (ImageButton)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            delete();
            }
        });

        // Usunięcie wpisu z ulubionych.
        ImageButton unfavoriteButton = (ImageButton)findViewById(R.id.unfavoriteButton);
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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                right.setVisibility(View.VISIBLE);
                left.setVisibility(View.VISIBLE);

                if(position == viewPagerImageAdapter.getCount() - 1){
                    right.setVisibility(View.INVISIBLE);
                }else if(position == 0){
                    left.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void delete(){

        // Wyświetlenie okna z prośbą o potwierdzenie.
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetailsActivity.this, R.style.myDialog));

        if(showReceipts == true)
            builder.setMessage("Czy na pewno chcesz skasować ten paragon ?").setTitle("Kasowanie paragonu.");
        else
            builder.setMessage("Czy na pewno chcesz skasować tą kartę ?").setTitle("Kasowanie karty.");

        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Cursor c = db.query(itemTable, projection, selection, selectionArgs, null, null, null);
                c.moveToFirst();
                String path;

                selectionArgs = new String[]{ itemId };

                Cursor photoCursor = db.query(photoTable, photoProjection, photoSelection, selectionArgs, null, null, null);

                while(photoCursor.moveToNext()){
                    try{
                        new File(photoCursor.getString(photoCursor.getColumnIndex(photoColumn))).delete();
                    }catch (Exception e){
                        toast = Toast.makeText(getApplicationContext(), "Nie znaleziono pliku zdjęcia do skasowania.", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }

                db.delete(itemTable, selection, selectionArgs);
                db.delete(photoTable, photoSelection, selectionArgs);
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

    // Ustawienie nazwy tabeli, treści klauzuli WHERE i jej parametrów w zależności
    // od trybu aplikacji, oraz wykonanie zapytania do bazy danych i zapisanie
    // wyników w kursorze.
    public void setShowReceipts(boolean show){
        if(show){
            showReceipts = true;
            projection = Constants.receiptTableCols;
            photoProjection = Constants.receiptPhotosTableCols;
            selection = ReceiptContract.Receipt._ID + " = ?";
            photoSelection = ReceiptContract.Receipt_Photos.RECEIPT_ID + " = ?";
            itemTable = ReceiptContract.Receipt.TABLE_NAME;
            photoTable = ReceiptContract.Receipt_Photos.TABLE_NAME;
            photoColumn = ReceiptContract.Receipt_Photos.PHOTO_PATH;
        }else{
            showReceipts = false;
            projection = Constants.cardTableCols;
            photoProjection = Constants.cardPhotosTableCols;
            selection = CardContract.Card._ID + " = ?";
            photoSelection = CardContract.Card_Photos.CARD_ID + " = ?";
            itemTable = CardContract.Card.TABLE_NAME;
            photoTable = CardContract.Card_Photos.TABLE_NAME;
            photoColumn = CardContract.Card_Photos.PHOTO_PATH;
        }

        selectionArgs = new String[]{itemId};
        tableCursor = db.query(itemTable, projection, selection, selectionArgs, null, null, null);
        photoCursor = db.query(photoTable, photoProjection, photoSelection, selectionArgs, null, null, null);
    }

    // Przy wznowieniu aktywności dane wyświetlanego wpisu są ponownie pobierane i
    // zawartość elementów interfejsu jest aktualizowana.
    @Override
    protected void onResume(){
        tableCursor = db.query(itemTable, projection, selection, selectionArgs, null, null, null);
        tableCursor.moveToFirst();
        loadImages(itemId);
        setViewContents();
        super.onResume();
    }

    // Metoda ustawia zawartość poszczególnych elementów interfejsu użytkownika
    // w zależności od aktywnego trybu aplikacji.
    private void setViewContents(){
        if(showReceipts == true){
            nameTextView.setText("Nazwa : " + tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.NAME)));
            categoryTextView.setText("Kategoria : " + tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
            dateTextView.setText("Data : " + tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.DATE)));

            Double value = tableCursor.getDouble(tableCursor.getColumnIndex(ReceiptContract.Receipt.VALUE));
            valueString = value.toString().replaceAll(",", ".");

            valueTextView.setText("Wartość : " + valueString);
            descriptionTextView.setText("Opis : " + tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.DESCRIPTION)));
            warrantyTextView.setText("Czas gwarancji : " + tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.WARRANTY)));
            bitmapPath = tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH));
            Glide.with(this).load(bitmapPath).into(itemImageView);
            if(tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.FAVORITED)) != null){
                if(tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.FAVORITED)).equals("yes")){
                    favoritedIconView.setVisibility(View.VISIBLE);
                }else{
                    favoritedIconView.setVisibility(View.GONE);
                }
            }

        }else {
            nameTextView.setText("Nazwa : " + tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.NAME)));
            categoryTextView.setText("Kategoria : " + tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.CATEGORY)));
            dateTextView.setText("Data : " + tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            descriptionTextView.setText("Opis : " + tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.DESCRIPTION)));
            bitmapPath = tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.IMAGE_PATH));
            Glide.with(this).load(bitmapPath).into(itemImageView);
            valueTextView.setVisibility(View.GONE);
            warrantyTextView.setVisibility(View.GONE);
            if(tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.FAVORITED)) != null){
                if(tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.FAVORITED)).equals("yes")){
                    favoritedIconView.setVisibility(View.VISIBLE);
                }else{
                    favoritedIconView.setVisibility(View.GONE);
                }
            }
        }

    }

    // Metoda zwraca listę danych wyświetlanego wpisu.
    private ArrayList<String> buildDataArray(){
        ArrayList<String> data = new ArrayList<>();
        if(showReceipts){
            data.add(0, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.NAME)));
            data.add(1, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
            data.add(2, valueString);
            data.add(3, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.DATE)));
            data.add(4, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH)));
            data.add(5, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.CONTENT)));
            data.add(6, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.FAVORITED)));
            data.add(7, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.DESCRIPTION)));
            data.add(8, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt._ID)));
            data.add(9, tableCursor.getString(tableCursor.getColumnIndex(ReceiptContract.Receipt.WARRANTY)));
        }else{
            data.add(0, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.NAME)));
            data.add(1, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.CATEGORY)));
            data.add(2, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            data.add(3, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.IMAGE_PATH)));
            data.add(4, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.FAVORITED)));
            data.add(5, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card.DESCRIPTION)));
            data.add(6, tableCursor.getString(tableCursor.getColumnIndex(CardContract.Card._ID)));
        }
        return data;
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainViewActivity.class);
        startActivity(intent);
    }

    // Przy zakończeniu działania aktywności zamykany jest kursor oraz połączenie z bazą danych.
    @Override
    protected void onDestroy(){

        tableCursor.close();
        db.close();
        super.onDestroy();
    }

    private void loadImages(String itemId){
        String[] selectionArgs = new String[]{itemId};
        imgs.clear();
        Cursor photoCursor = db.query(photoTable, photoProjection, photoSelection, selectionArgs, null , null, null);
        while (photoCursor.moveToNext()){
            imgs.add(photoCursor.getString(photoCursor.getColumnIndex(photoColumn)));
        }
        setViewPagerAdapter(imgs);
    }

    private void setViewPagerAdapter(ArrayList<String> imgs){

        viewPagerImageAdapter = new ViewPagerImageAdapter(getApplicationContext(), imgs, showReceipts);
        viewPager.setAdapter(viewPagerImageAdapter);

        if(viewPagerImageAdapter.getCount() == 1){
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
        }else{
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.VISIBLE);
        }

    }
}
