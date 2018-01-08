package com.example.maciejwikira.prgnv2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

// Aktywność wyświetla zdjęcie paragonu lub karty.
public class DisplayPhotoActivity extends AppCompatActivity {

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_photo);

        image = (ImageView)findViewById(R.id.imageView2);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Boolean showReceipts = bundle.getBoolean(MainViewActivity.CARDS_OR_RECEIPTS);

        Glide.with(this).load(bundle.getString("BitmapPath")).into(image);

    }

}
