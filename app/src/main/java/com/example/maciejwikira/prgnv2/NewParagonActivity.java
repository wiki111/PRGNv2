package com.example.maciejwikira.prgnv2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class NewParagonActivity extends AppCompatActivity {

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
    private CardFunctions cardFunctions;
    private TextRecognitionFunctions textRecognitionFunctions;
    private boolean showParagons;

    private BaseLoaderCallback mLoaderCallback = new
            BaseLoaderCallback(this) {
                @Override
                //This is the callback method called once the OpenCV
                // manager is connected
                public void onManagerConnected(int status) {
                    switch (status) {
                        //Once the OpenCV manager is successfully connected we can enable the
                        // camera interaction with the defined OpenCV camera view
                        case LoaderCallbackInterface.SUCCESS:
                        {
                            Log.i("PRGN : ", "OpenCV loaded successfully");
                        } break;
                        default:
                        {
                            super.onManagerConnected(status);
                        } break;
                    }
                }
            };

    @Override
    public void onResume(){
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this,
                mLoaderCallback);
    }

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
        cardFunctions = new CardFunctions(context);
        textRecognitionFunctions = new TextRecognitionFunctions(context);
        //Obsługa kliknięcia przycisku dodającego dane do bazy
        addToDBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showParagons == true){
                    ContentValues cv = new ContentValues();
                    cv.put("name", nameField.getText().toString()); //nazwa wpisu
                    cv.put("category", categoryField.getText().toString().toLowerCase()); //kategoria wpisu
                    cv.put("date", dateField.getText().toString()); //data paragonu
                    cv.put("value", Double.parseDouble(valueField.getText().toString().replaceAll("," , "\\.")));   //wartość paragonu
                    cv.put("img", textRecognitionFunctions.getImgToSave());   //ścieżka absolutna do zdjęcia paragonu
                    cv.put("text", textRecognitionFunctions.getPrgnText());
                    cv.put("favorited", "no");
                    paragonFunctions.addParagon(cv);
                }else{
                    ContentValues cv = new ContentValues();
                    cv.put("name", nameField.getText().toString()); //nazwa wpisu
                    cv.put("category", categoryField.getText().toString().toLowerCase()); //kategoria wpisu
                    cv.put("expiration", dateField.getText().toString()); //data paragonu
                    cv.put("img", getRealPathFromURI(activeUri));
                    cv.put("favorited", "no");
                    cardFunctions.addCard(cv);
                }
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if(extras.getBoolean(MainViewActivity.CARDS_OR_PARAGONS)){
            showParagons = true;
        }else {
            showParagons = false;
            TextView valueView = (TextView)findViewById(R.id.textView8);
            TextView dateView = (TextView)findViewById(R.id.textView7);
            dateView.setText("Data wygaśnięcia");
            valueView.setVisibility(View.INVISIBLE);
            valueField.setVisibility(View.INVISIBLE);
        }

        if(showParagons == true)
            addToDBBtn.setText("Dodaj paragon");
        else
            addToDBBtn.setText("Dodaj kartę");

        if(extras.getString(MainViewActivity.CAMERA_OR_MEDIA).equals("cam")){
            takePhoto();
        }else if(extras.getString(MainViewActivity.CAMERA_OR_MEDIA).equals("media")) {
            openGallery();
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
            activeUri = saveImage(activeUri);
            //searchForValues(getRealPathFromURI(activeUri), context);
            if(showParagons == true){
                textRecognitionFunctions.searchForValues(activeUri,mCurrentPhotoPath, context);
            }
        }else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            activeUri = saveImage(mUri);
            if(showParagons == true){
                Context context = getApplicationContext();
                textRecognitionFunctions.searchForValues(activeUri, mCurrentPhotoPath, context);
            }
        }

        if(activeUri == null){
            finish();
        }

        if(showParagons == true) {
            valueField.setText(textRecognitionFunctions.getParagonValue());
            dateField.setText(textRecognitionFunctions.getParagonDate());
        }
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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private Uri saveImage(Uri activeUri){
        Uri uri;
        Bitmap bm;

        bm = BitmapFactory.decodeFile(getRealPathFromURI(activeUri));

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Paragons");
        if (!folder.exists()) {
            if(folder.mkdir()){
                Log.d("Paragon App : ", "Successfully created the parent dir:" + folder.getName());
            }else{
                Log.d("Paragon App : ", "Failed to create the parent dir:" + folder.getName());
            }
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(folder, fname);

        if(file.exists()){
            file.delete();
        }
        try{
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCurrentPhotoPath = file.getAbsolutePath();
        uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", file);

        return uri;
    }

}
