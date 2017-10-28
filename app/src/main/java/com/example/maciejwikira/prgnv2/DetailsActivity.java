package com.example.maciejwikira.prgnv2;

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
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    private ParagonDbHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String selection;
    private String[] selectionArgs;
    private String[] cols;

    private TextView nameTextView;
    private TextView categoryTextView;
    private TextView dateTextView;
    private TextView valueTextView;
    private ImageView paragonPhotoDetailsView;

    private String bitmapPath;
    private Bitmap paragonPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDbHelper = new ParagonDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        cols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        Intent intent = getIntent();
        final String id = intent.getStringExtra("item_id");

        selection = ParagonContract.Paragon._ID + " = ?";

        selectionArgs = new String[]{
                id
        };

        c = db.query(ParagonContract.Paragon.TABLE_NAME, cols,selection, selectionArgs, null, null, null);

        c.moveToFirst();

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        nameTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.NAME)));

        categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        categoryTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.CATEGORY)));

        dateTextView = (TextView)findViewById(R.id.dateTextView);
        dateTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.DATE)));

        valueTextView = (TextView)findViewById(R.id.valueTextView);
        valueTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.VALUE)));

        paragonPhotoDetailsView = (ImageView)findViewById(R.id.paragonPhotoDetailsView);
        bitmapPath = c.getString(c.getColumnIndex(ParagonContract.Paragon.IMAGE_PATH));
        paragonPhoto = BitmapFactory.decodeFile(bitmapPath);
        paragonPhotoDetailsView.setImageBitmap(paragonPhoto);

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
                intent.putExtra("item_id", id);
                startActivity(intent);
            }
        });

        Button favoriteButton = (Button)findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO implement favorite paragons
            }
        });

        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(DetailsActivity.this, R.style.myDialog));
                builder.setMessage("Czy na pewno chcesz skasować ten paragon ?")
                        .setTitle("Kasowanie paragonu.");
                builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.delete(ParagonContract.Paragon.TABLE_NAME, selection, selectionArgs);
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
    protected void onResume(){
        c = db.query(ParagonContract.Paragon.TABLE_NAME, cols,selection, selectionArgs, null, null, null);

        c.moveToFirst();
        nameTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.NAME)));
        categoryTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.CATEGORY)));
        dateTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.DATE)));
        valueTextView.setText(c.getString(c.getColumnIndex(ParagonContract.Paragon.VALUE)));
        bitmapPath = c.getString(c.getColumnIndex(ParagonContract.Paragon.IMAGE_PATH));
        paragonPhoto = BitmapFactory.decodeFile(bitmapPath);
        paragonPhotoDetailsView.setImageBitmap(paragonPhoto);

        super.onResume();
    }

    @Override
    protected void onDestroy(){
        c.close();
        db.close();
        super.onDestroy();
    }
}
