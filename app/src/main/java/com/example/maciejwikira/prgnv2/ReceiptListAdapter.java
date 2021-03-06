package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.math.BigDecimal;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by Maciej on 2017-10-21.
 */

// Klasa mapuje dane o paragonach pobrane z kolumn tabeli w bazie danych na odpowiednie elementy
// interfejsu użytkownika.
public class ReceiptListAdapter extends SimpleCursorAdapter{

    // Obiekt tworzący widok z podanego zasobu
    private LayoutInflater inflater;
    // id zasobu zawierającego definicję widoku
    private int layout;

    public ReceiptListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.inflater = LayoutInflater.from(context);
        this.layout = layout;
    }


    // Każdy nowy element listy jest tworzony przez obiekt LayoutInflater z zasobu o podanym ID
    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent){
        return inflater.inflate(layout, null);
    }

    // Metoda mapuje dane do elementów interfejsu
    @Override
    public void bindView(View view, Context context, Cursor cursor){

        // Deklaracje elementów interfejsu
        ImageView imageView = (ImageView) view.findViewById(R.id.photoView);
        TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
        TextView categoryView = (TextView) view.findViewById(R.id.categoryTextView);
        TextView dateView = (TextView) view.findViewById(R.id.dateView);
        TextView valueView = (TextView) view.findViewById(R.id.valueTextView);

        //Wyświetlenie bitmapy na interfejsie użytkownika
        String path = mCursor.getString(mCursor.getColumnIndex(ReceiptContract.Receipt.IMAGE_PATH));
        if(path != null){
            Glide.with(mContext).load(path).into(imageView);
        }else{
            imageView.setImageResource(R.drawable.ic_receipt);
        }


        // Ustawienie zawartości pól interfejsu
        nameView.setText("Nazwa: " + cursor.getString(cursor.getColumnIndex(ReceiptContract.Receipt.NAME)));
        categoryView.setText("Kategoria: " + cursor.getString(cursor.getColumnIndex(ReceiptContract.Receipt.CATEGORY)));
        dateView.setText("Data: " + cursor.getString(cursor.getColumnIndex(ReceiptContract.Receipt.DATE)));

        Double value = cursor.getDouble(cursor.getColumnIndex(ReceiptContract.Receipt.VALUE));
        String valueString = value.toString().replaceAll(",", ".");

        valueView.setText("Wartość: " + valueString);
    }
}
