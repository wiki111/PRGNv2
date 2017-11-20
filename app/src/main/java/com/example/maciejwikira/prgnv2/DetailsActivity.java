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

public class DetailsActivity extends AppCompatActivity {

    private ReceiptDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String selection;
    private String[] selectionArgs;
    private String[] paragonCols;
    private String[] cardCols;
    private String id;

    private TextView nameTextView;
    private TextView categoryTextView;
    private TextView dateTextView;
    private TextView valueTextView;
    private ImageView paragonPhotoDetailsView;

    private String bitmapPath;
    private Bitmap paragonPhoto;

    private boolean showParagons;

    private ReceiptFunctions pf;
    private CardFunctions cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDbHelper = new ReceiptDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        paragonCols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        cardCols = new String[]{
                "_id",
                "name",
                "category",
                "expiration",
                "img"
        };


        pf = new ReceiptFunctions(getApplicationContext());
        cf = new CardFunctions(getApplicationContext());

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        id = extras.getString("item_id");
        showParagons = extras.getBoolean(MainViewActivity.CARDS_OR_PARAGONS);


       if(showParagons == true) {
           selection = ReceiptContract.Paragon._ID + " = ?";
           selectionArgs = new String[]{ id };
           c = db.query(ReceiptContract.Paragon.TABLE_NAME, paragonCols, selection, selectionArgs, null, null, null);
       }else{
           selection = CardContract.Card._ID + " = ?";
           selectionArgs = new String[]{ id };
           c = db.query(CardContract.Card.TABLE_NAME, cardCols, selection, selectionArgs, null, null, null);
       }
        c.moveToFirst();

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        dateTextView = (TextView)findViewById(R.id.dateTextView);
        valueTextView = (TextView)findViewById(R.id.valueTextView);
        paragonPhotoDetailsView = (ImageView)findViewById(R.id.paragonPhotoDetailsView);

        setViewContents();

        paragonPhotoDetailsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DisplayPhotoActivity.class);
                intent.putExtra("BitmapPath", bitmapPath);
                startActivity(intent);
            }
        });

        Button goToEditButton = (Button)findViewById(R.id.goToEditButton);
        goToEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(MainViewActivity.CARDS_OR_PARAGONS, showParagons);
                extras.putString("item_id", id);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        Button favoriteButton = (Button)findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                Toast toast;

                if(showParagons == true) {
                    contentValues.put(ReceiptContract.Paragon.NAME, nameTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.DATE, dateTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.VALUE, valueTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.IMAGE_PATH, c.getString(c.getColumnIndex(ReceiptContract.Paragon.IMAGE_PATH)));
                    contentValues.put(ReceiptContract.Paragon.CONTENT, c.getString(c.getColumnIndex(ReceiptContract.Paragon.CONTENT)));
                    contentValues.put(ReceiptContract.Paragon.FAVORITED, "yes");
                    pf.updateParagon(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Paragon dodano do ulubionych", Toast.LENGTH_SHORT);
                }else{
                    contentValues.put(CardContract.Card.NAME, nameTextView.getText().toString());
                    contentValues.put(CardContract.Card.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(CardContract.Card.EXPIRATION_DATE, dateTextView.getText().toString());
                    contentValues.put(CardContract.Card.IMAGE_PATH, c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
                    contentValues.put(CardContract.Card.FAVORITED, "yes");
                    cf.updateCard(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Kartę dodano do ulubionych", Toast.LENGTH_SHORT);
                }

                toast.show();
            }
        });

        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetailsActivity.this, R.style.myDialog));
                if(showParagons == true)
                    builder.setMessage("Czy na pewno chcesz skasować ten paragon ?").setTitle("Kasowanie paragonu.");
                else
                    builder.setMessage("Czy na pewno chcesz skasować tą kartę ?").setTitle("Kasowanie karty.");


                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(showParagons == true)
                            db.delete(ReceiptContract.Paragon.TABLE_NAME, selection, selectionArgs);
                        else
                            db.delete(CardContract.Card.TABLE_NAME, selection, selectionArgs);
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

        Button unfavoriteButton = (Button)findViewById(R.id.unfavoriteButton);
        unfavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                Toast toast;

                if(showParagons == true) {
                    contentValues.put(ReceiptContract.Paragon.NAME, nameTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.DATE, dateTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.VALUE, valueTextView.getText().toString());
                    contentValues.put(ReceiptContract.Paragon.IMAGE_PATH, c.getString(c.getColumnIndex(ReceiptContract.Paragon.IMAGE_PATH)));
                    contentValues.put(ReceiptContract.Paragon.CONTENT, c.getString(c.getColumnIndex(ReceiptContract.Paragon.CONTENT)));
                    contentValues.put(ReceiptContract.Paragon.FAVORITED, "no");
                    pf.updateParagon(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Paragon usunięto z ulubionych", Toast.LENGTH_SHORT);
                }else{
                    contentValues.put(CardContract.Card.NAME, nameTextView.getText().toString());
                    contentValues.put(CardContract.Card.CATEGORY, categoryTextView.getText().toString());
                    contentValues.put(CardContract.Card.EXPIRATION_DATE, dateTextView.getText().toString());
                    contentValues.put(CardContract.Card.IMAGE_PATH, c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
                    contentValues.put(CardContract.Card.FAVORITED, "no");
                    cf.updateCard(id, contentValues);
                    toast = Toast.makeText(getApplicationContext(), "Kartę usunięto z ulubionych", Toast.LENGTH_SHORT);
                }

                toast.show();
            }
        });

    }

    @Override
    protected void onResume(){
        if(showParagons == true) {
            c = db.query(ReceiptContract.Paragon.TABLE_NAME, paragonCols, selection, selectionArgs, null, null, null);
        }else {
            c = db.query(CardContract.Card.TABLE_NAME, cardCols, selection, selectionArgs, null, null, null);
        }
        c.moveToFirst();

        setViewContents();

        super.onResume();
    }

    @Override
    protected void onDestroy(){
        c.close();
        db.close();
        super.onDestroy();
    }

    private void setViewContents(){
        if(showParagons == true){
            nameTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.NAME)));
            categoryTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.CATEGORY)));
            dateTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.DATE)));
            valueTextView.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.VALUE)));
            bitmapPath = c.getString(c.getColumnIndex(ReceiptContract.Paragon.IMAGE_PATH));
            paragonPhoto = BitmapFactory.decodeFile(bitmapPath);
            paragonPhotoDetailsView.setImageBitmap(paragonPhoto);
        }else {
            nameTextView.setText(c.getString(c.getColumnIndex(CardContract.Card.NAME)));
            categoryTextView.setText(c.getString(c.getColumnIndex(CardContract.Card.CATEGORY)));
            dateTextView.setText(c.getString(c.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            bitmapPath = c.getString(c.getColumnIndex(ReceiptContract.Paragon.IMAGE_PATH));
            paragonPhoto = BitmapFactory.decodeFile(bitmapPath);
            paragonPhotoDetailsView.setImageBitmap(paragonPhoto);

            valueTextView.setVisibility(View.INVISIBLE);
        }
    }
}
