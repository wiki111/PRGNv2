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

public class EditActivity extends AppCompatActivity {

    private ReceiptDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private boolean showParagons;
    private String selection;
    private String[] selectionArgs;
    private String[] paragonCols;
    private String[] cardCols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mDbHelper = new ReceiptDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        paragonCols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text",
                "favorited"
        };

        cardCols = new String[]{
                "_id",
                "name",
                "category",
                "expiration",
                "img",
                "favorited"
        };


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        final String item_id = extras.getString("item_id");
        showParagons = extras.getBoolean(MainViewActivity.CARDS_OR_PARAGONS);

        if(showParagons == true) {
            selection = ReceiptContract.Paragon._ID + " = ?";
            selectionArgs = new String[]{ item_id };
            c = db.query(ReceiptContract.Paragon.TABLE_NAME, paragonCols, selection, selectionArgs, null, null, null);
        }else{
            selection = CardContract.Card._ID + " = ?";
            selectionArgs = new String[]{ item_id };
            c = db.query(CardContract.Card.TABLE_NAME, cardCols, selection, selectionArgs, null, null, null);
        }
        c.moveToFirst();
        c.moveToFirst();

        final EditText nameEdit = (EditText)findViewById(R.id.nameEdit);
        final EditText categoryEdit = (EditText)findViewById(R.id.categoryEdit);
        final EditText dateEdit = (EditText)findViewById(R.id.dateEdit);
        final EditText valueEdit = (EditText)findViewById(R.id.valueEdit);
        ImageView paragonPhotoView = (ImageView)findViewById(R.id.paragonPhotoView);

        if(showParagons == true) {
            nameEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.NAME)));
            categoryEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.CATEGORY)));
            dateEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.DATE)));
            valueEdit.setText(c.getString(c.getColumnIndex(ReceiptContract.Paragon.VALUE)));
            Bitmap paragonPhoto = BitmapFactory.decodeFile(c.getString(c.getColumnIndex(ReceiptContract.Paragon.IMAGE_PATH)));
            paragonPhotoView.setImageBitmap(paragonPhoto);

        }else {
            nameEdit.setText(c.getString(c.getColumnIndex(CardContract.Card.NAME)));
            categoryEdit.setText(c.getString(c.getColumnIndex(CardContract.Card.CATEGORY)));
            dateEdit.setText(c.getString(c.getColumnIndex(CardContract.Card.EXPIRATION_DATE)));
            Bitmap paragonPhoto = BitmapFactory.decodeFile(c.getString(c.getColumnIndex(CardContract.Card.IMAGE_PATH)));
            paragonPhotoView.setImageBitmap(paragonPhoto);

            valueEdit.setVisibility(View.INVISIBLE);
        }
        final ReceiptFunctions receiptFunctions = new ReceiptFunctions(getApplicationContext());
        final CardFunctions cardFunctions = new CardFunctions(getApplicationContext());

        Button updateButton = (Button)findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(EditActivity.this, R.style.myDialog));
                if(showParagons == true)
                    builder.setMessage("Czy na pewno chcesz zmienić ten paragon ?").setTitle("Edycja paragonu.");
                else
                    builder.setMessage("Czy na pewno chcesz zmienić tą kartę lojalnościową ?").setTitle("Edycja karty.");
                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        ContentValues cv = new ContentValues();

                        if(showParagons == true){
                            cv.put(ReceiptContract.Paragon.NAME, nameEdit.getText().toString());
                            cv.put(ReceiptContract.Paragon.CATEGORY, categoryEdit.getText().toString().toLowerCase());
                            cv.put(ReceiptContract.Paragon.DATE, dateEdit.getText().toString());
                            cv.put(ReceiptContract.Paragon.VALUE, valueEdit.getText().toString());
                            cv.put(ReceiptContract.Paragon.IMAGE_PATH, c.getString(c.getColumnIndex(ReceiptContract.Paragon.IMAGE_PATH)));
                            cv.put(ReceiptContract.Paragon.CONTENT, c.getString(c.getColumnIndex(ReceiptContract.Paragon.CONTENT)));
                            cv.put(ReceiptContract.Paragon.FAVORITED, c.getString(c.getColumnIndex(ReceiptContract.Paragon.FAVORITED)));
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
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        c.close();
        db.close();
    }

}
