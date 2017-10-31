package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NewPrgnActivity extends AppCompatActivity {

    //Deklaracja elementów interfejsu :
    private EditText nameField;
    private EditText categoryField;
    private EditText valueField;
    private EditText dateField;
    private Button addToDBBtn;
    private Uri activeUri;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private Uri mUri;
    private ParagonFunctions paragonFunctions;
    private TextRecognitionFunctions textRecognitionFunctions;

    //funkcja onCreate - inicjalizacja obiektów interfejsu użytkownika i wywołanie funkcji aktywności
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_prgn);
        final Context context = getApplicationContext();    //zapisanie kontekstu do zmiennej
        //inicjalizacja elementów interfejsu użytkownika
        nameField = (EditText)findViewById(R.id.nameField);
        categoryField = (EditText) findViewById(R.id.categoryField);
        valueField = (EditText) findViewById(R.id.valueField);
        dateField = (EditText) findViewById(R.id.dateField);
        addToDBBtn = (Button)findViewById(R.id.addToDBButton);
        paragonFunctions = new ParagonFunctions(context);
        textRecognitionFunctions = new TextRecognitionFunctions(context);
        //Obsługa kliknięcia przycisku dodającego dane do bazy
        addToDBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();
                cv.put("name", nameField.getText().toString()); //nazwa wpisu
                cv.put("category", categoryField.getText().toString().toLowerCase()); //kategoria wpisu
                cv.put("date", dateField.getText().toString()); //data paragonu
                cv.put("value", Double.parseDouble(valueField.getText().toString().replaceAll("," , "\\.")));   //wartość paragonu
                cv.put("img", textRecognitionFunctions.getImgToSave());   //ścieżka absolutna do zdjęcia paragonu
                cv.put("text", textRecognitionFunctions.getPrgnText());
                paragonFunctions.addParagon(cv);
            }
        });
        Intent intent = getIntent();
        if(intent.getStringExtra(MainViewActivity.CAMERA_OR_MEDIA).equals("cam")){
            takePhoto();
        }else if(intent.getStringExtra(MainViewActivity.CAMERA_OR_MEDIA).equals("media")) {
            openGallery();  //wywołanie funkcji otwierającej galerię w celu wyborania zdjęcia paragonu
        }
    }

    //Funkcja otwierająca galerię w celu wbrania zdjęcia paragonu
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    private void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                mUri = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //Funkcja wywoływana przy powrocie z zewnętrznej aktywności - wyboru zdjęcia lub wskazywania obszaru
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // fragment kodu wykonywany, gdy następuje powrtót z aktywności wyboru zdjęcia paragonu
        if (resultCode == RESULT_OK && requestCode == 100) {
            Context context = getApplicationContext();
            activeUri = data.getData(); //pobierz uri obrazu z danych zwróconych przez aktywność i zapisz do zmiennej
            //searchForValues(getRealPathFromURI(activeUri), context);
            textRecognitionFunctions.searchForValues(activeUri,getRealPathFromURI(activeUri), context);
        }else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Context context = getApplicationContext();
            textRecognitionFunctions.searchForValues(mUri, mCurrentPhotoPath, context);
        }
        valueField.setText(textRecognitionFunctions.getParagonValue());
        dateField.setText(textRecognitionFunctions.getParagonDate());
    }

    //Pozyskanie absolutnej sciezki do pliku na podstawie Uri
    @SuppressWarnings("deprecation")
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //Stworzenie pliku w ktorym zapisany zostanie obraz przechwycony przez kamere
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

}
