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

// Aktywność wykonuje bardzo proste zadanie  - wyświetla zdjęcie paragonu lub karty.
public class DisplayPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_photo);

        ImageView image = (ImageView)findViewById(R.id.imageView2);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Bitmap bmp = BitmapFactory.decodeFile(bundle.getString("BitmapPath"));
        Boolean showReceipts = bundle.getBoolean(MainViewActivity.CARDS_OR_RECEIPTS);

        if(!showReceipts){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }



        image.setImageBitmap(bmp);


    }
}
