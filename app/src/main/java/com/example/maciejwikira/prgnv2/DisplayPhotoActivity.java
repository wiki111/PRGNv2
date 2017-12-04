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
        //Bitmap bmp = BitmapFactory.decodeFile(bundle.getString("BitmapPath"));
        Boolean showReceipts = bundle.getBoolean(MainViewActivity.CARDS_OR_RECEIPTS);

        // Karty wyświetlane są w horyzontalnej orientacji ekranu.
        if(!showReceipts){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        image.setImageBitmap(loadScaledBitmap(bundle.getString("BitmapPath")));

    }

    public Bitmap loadScaledBitmap(String path){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateSampleSize(options, image.getMaxWidth(), image.getMaxHeight());

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    // Metoda oblicza próbkowanie używane do załadowania wersji bitmapy o
    // porządanych rozmiarach.
    public int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Obliczanie optymalnej wartości próbkowania tak, aby uzyskać
            // rozmiar obrazu najbardziej zbliżony do porządanego.
            while((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth){
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
