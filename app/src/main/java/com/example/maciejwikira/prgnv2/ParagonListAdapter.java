package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej on 2017-10-21.
 */

public class ParagonListAdapter extends ArrayAdapter{

    private ArrayList<Paragon> items;
    private LayoutInflater inflater;

    public ParagonListAdapter(Context context, int textViewResourceId, ArrayList<Paragon> objects) {
        super(context, textViewResourceId, objects);
        this.items = objects;
    }

    @Override
    public int getCount(){
        int count = items.size();
        return count;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View v;
        if (convertView == null) {
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = inflater.inflate(R.layout.paragon_list_item, null);
        }else
           v = convertView;


        ImageView imageView = (ImageView) v.findViewById(R.id.photoView);
        TextView nameView = (TextView) v.findViewById(R.id.nameTextView);
        TextView categoryView = (TextView) v.findViewById(R.id.categoryTextView);
        TextView dateView = (TextView) v.findViewById(R.id.dateView);
        TextView valueView = (TextView) v.findViewById(R.id.valueTextView);

        Bitmap image = BitmapFactory.decodeFile(items.get(position).getImg());
        imageView.setImageBitmap(image);

        nameView.setText("Nazwa: " + items.get(position).getName());
        categoryView.setText("Kategoria: " + items.get(position).getCategory());
        dateView.setText("Data: " + items.get(position).getDate());
        valueView.setText("Wartość: " + items.get(position).getValue());

        return v;
    }
}
