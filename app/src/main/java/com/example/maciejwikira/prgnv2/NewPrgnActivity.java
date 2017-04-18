package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewPrgnActivity extends AppCompatActivity {

    private TextView txtView;

    private Bitmap activeBitmap;
    private Uri activeUri;

    private Matcher match;
    private boolean valueFound = false;

    private int REQUEST_CROP = 200;

    //Patterns :
    private Pattern wholeValue = Pattern.compile("suma pln");
    private Pattern wholeValue2 = Pattern.compile("suma pln \\d+,\\d+");
    private Pattern wholeValue3 = Pattern.compile("([0-9]+,[0-9]+ pln)(.*?)");
    private Pattern theValue = Pattern.compile("([0-9]+,[0-9]+)");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_prgn);

        txtView = (TextView)findViewById(R.id.textView);

        openGallery();


    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            Context context = getApplicationContext();
            activeUri = data.getData();

            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
            try {
                activeBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), activeUri);
                Frame frame = new Frame.Builder().setBitmap(activeBitmap).build();

                SparseArray<TextBlock> items = textRecognizer.detect(frame);

                txtView.append(" ZNALEZIONA ! ----> Suma PLN : " + searchForTheValue(items) + "\n");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_CROP){

            Context context = getApplicationContext();

            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
            try {
               Bitmap croppedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                Frame frame = new Frame.Builder().setBitmap(croppedBitmap).build();

                SparseArray<TextBlock> items = textRecognizer.detect(frame);

                txtView.append(" ZNALEZIONA ! ----> Suma PLN : " + searchForTheValue(items) + "\n");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                Context context = getApplicationContext();

                TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
                try {
                    Bitmap croppedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    Frame frame = new Frame.Builder().setBitmap(croppedBitmap).build();

                    SparseArray<TextBlock> items = textRecognizer.detect(frame);

                    txtView.append(" ZNALEZIONA ! ----> Suma PLN : " + searchForTheValue(items) + "\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }

        }

    }

    private String searchForTheValue(SparseArray<TextBlock> items){

        String val = "oops";
        String foundVal = "";

        for (int i = 0; i < items.size(); ++i) {

            TextBlock item = items.valueAt(i);

            //txtView.append("Checking item : " + item.getValue().toLowerCase() + "\n");

            match = wholeValue3.matcher(item.getValue().toLowerCase());
            if(match.find() && valueFound == false){

                valueFound = true;
                foundVal = match.group().substring(0);

                match = theValue.matcher(foundVal);
                match.find();
                val = match.group().substring(0);


            }

            match = wholeValue.matcher(item.getValue().toLowerCase());
            if(match.matches() && valueFound == false){

                valueFound = true;
                i = i+1;
                item = items.valueAt(i);

                match = theValue.matcher(item.getValue().toLowerCase());

                if(match.find()){
                    val = match.group().substring(0);
                }

            }

            match = wholeValue2.matcher(item.getValue().toLowerCase());
            if(match.find() && valueFound == false){

                valueFound = true;
                foundVal = match.group().substring(0);

                match = theValue.matcher(foundVal);
                match.find();
                val = match.group().substring(0);

            }

        }

        if(valueFound == false){

            // start picker to get image for cropping and then use the image in cropping activity
            //CropImage.activity()
              //      .setGuidelines(CropImageView.Guidelines.ON)
                //    .start(this);

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(activeUri)
                    .start(this);

            // for fragment (DO NOT use `getActivity()`)
            //CropImage.activity()
                   // .start(getContext(), this);

        }

        return val;
    }
}
