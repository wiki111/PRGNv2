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

    private ParagonDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mDbHelper = new ParagonDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        Intent intent = getIntent();
        final String item_id = intent.getStringExtra("item_id");

        String selection = ParagonContract.Paragon._ID + " = ?";

        String[] selectionArgs = new String[]{
                item_id
        };

        c = db.query(ParagonContract.Paragon.TABLE_NAME, cols,selection, selectionArgs, null, null, null);

        c.moveToFirst();

        final EditText nameEdit = (EditText)findViewById(R.id.nameEdit);
        nameEdit.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.NAME)));

        final EditText categoryEdit = (EditText)findViewById(R.id.categoryEdit);
        categoryEdit.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.CATEGORY)));

        final EditText dateEdit = (EditText)findViewById(R.id.dateEdit);
        dateEdit.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.DATE)));

        final EditText valueEdit = (EditText)findViewById(R.id.valueEdit);
        valueEdit.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.VALUE)));

        ImageView paragonPhotoView = (ImageView)findViewById(R.id.paragonPhotoView);
        Bitmap paragonPhoto = BitmapFactory.decodeFile(c.getString(c.getColumnIndex(ParagonContract.Paragon.IMAGE_PATH)));
        paragonPhotoView.setImageBitmap(paragonPhoto);

        final ParagonFunctions paragonFunctions = new ParagonFunctions(getApplicationContext());

        Button updateButton = (Button)findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(EditActivity.this, R.style.myDialog));
                builder.setMessage("Czy na pewno chcesz zmienić ten paragon ?").setTitle("Edycja paragonu.");
                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ContentValues cv = new ContentValues();
                        cv.put(ParagonContract.Paragon.NAME, nameEdit.getText().toString());
                        cv.put(ParagonContract.Paragon.CATEGORY, categoryEdit.getText().toString().toLowerCase());
                        cv.put(ParagonContract.Paragon.DATE, dateEdit.getText().toString());
                        cv.put(ParagonContract.Paragon.VALUE, valueEdit.getText().toString());
                        cv.put(ParagonContract.Paragon.IMAGE_PATH, c.getString(c.getColumnIndex(ParagonContract.Paragon.IMAGE_PATH)));
                        cv.put(ParagonContract.Paragon.CONTENT, c.getString(c.getColumnIndex(ParagonContract.Paragon.CONTENT)));
                        cv.put(ParagonContract.Paragon.FAVORITED, c.getString(c.getColumnIndex(ParagonContract.Paragon.FAVORITED)));
                        paragonFunctions.updateParagon(item_id, cv);
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
