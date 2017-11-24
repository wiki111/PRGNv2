package com.example.maciejwikira.prgnv2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Aktywność pozwala na dodanie nowego wpisu do bazy danych
public class NewRecordActivity extends AppCompatActivity {

    // Deklaracja stałych identyfikatorów
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GET_RECEIPT = 2;

    //Deklaracja elementów interfejsu :
    private EditText nameField;
    private EditText categoryField;
    private EditText valueField;
    private EditText dateField;
    private Button addToDBBtn;
    private Uri activeUri;

    private String mCurrentPhotoPath;
    private Uri mUri;
    private ReceiptFunctions receiptFunctions;
    private CardFunctions cardFunctions;
    private TextRecognitionFunctions textRecognitionFunctions;
    private boolean showParagons;
    private Intent mServiceIntent;
    private ImageView pickedImageView;
    private ProgressBar progressBar;

    private String imgToSave;
    private String textFromImage;
    private String receiptValue;
    private String receiptDate;

    //funkcja onCreate - inicjalizacja obiektów interfejsu użytkownika i wywołanie funkcji aktywności
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicjalizajca biblioteki OpenCV i przekazanie informacji o rezultacie operacji
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        // Uzyskanie od użytkownika pozwolenia na zapis w pamieciu urządzenia
        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        // Ustawienie widoku aktywności
        setContentView(R.layout.activity_new_prgn);
        //Zapisanie kontekstu do zmiennej
        final Context context = getApplicationContext();
        //Inicjalizacja elementów interfejsu użytkownika
        nameField = (EditText)findViewById(R.id.nameField);
        categoryField = (EditText) findViewById(R.id.categoryField);
        valueField = (EditText) findViewById(R.id.valueField);
        dateField = (EditText) findViewById(R.id.dateField);
        addToDBBtn = (Button)findViewById(R.id.addToDBButton);
        pickedImageView = (ImageView)findViewById(R.id.pickedImageView);

        //Obsługa kliknięcia przycisku dodającego dane do bazy
        addToDBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showParagons == true){
                    ContentValues cv = new ContentValues();
                    cv.put("name", nameField.getText().toString());
                    cv.put("category", categoryField.getText().toString().toLowerCase());
                    cv.put("date", dateField.getText().toString());
                    cv.put("value", Double.parseDouble(valueField.getText().toString().replaceAll("," , "\\.")));
                    cv.put("img", imgToSave);
                    cv.put("text", textFromImage);
                    cv.put("favorited", "no");
                    receiptFunctions.addParagon(cv);
                }else{
                    ContentValues cv = new ContentValues();
                    cv.put("name", nameField.getText().toString());
                    cv.put("category", categoryField.getText().toString().toLowerCase());
                    cv.put("expiration", dateField.getText().toString());
                    cv.put("img", getRealPathFromURI(activeUri));
                    cv.put("favorited", "no");
                    cardFunctions.addCard(cv);
                }
            }
        });

        // Pobranie informacji o trybie aplikacji
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        // Ustawienie odpowiednich zmiennych i elementów interfejsu użytkownika w zależności od
        // trybu aplikacji
        if(extras.getBoolean(MainViewActivity.CARDS_OR_PARAGONS)){
            showParagons = true;
            addToDBBtn.setText("Dodaj paragon");
            // Incjalizacja obiektu przetwarzającego dane o paragonach
            receiptFunctions = new ReceiptFunctions(context);
            // Inicjalizacja obiektu rozpoznającego i sczytującego tekst z obrazu
            textRecognitionFunctions = new TextRecognitionFunctions(context);
        }else {
            showParagons = false;
            TextView valueView = (TextView)findViewById(R.id.valTextView);
            TextView dateView = (TextView)findViewById(R.id.dateTextView);

            // Zmiana nazw elementów interfejsu
            dateView.setText("Data wygaśnięcia");
            addToDBBtn.setText("Dodaj kartę");

            // Ukrycie niepotrzebnych elementow interfejsów
            valueView.setVisibility(View.INVISIBLE);
            valueField.setVisibility(View.INVISIBLE);

            // Inicjalizacja obiektu przetwarzającego dane o kartach
            cardFunctions = new CardFunctions(context);
        }

        // Rejestracja obiektu odbierającego zgłoszenia od obiektu przetwarzającego obraz
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        FixedImageReceiver fixedImageReceiver = new FixedImageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(fixedImageReceiver, intentFilter);

        // Uruchom wybór zdjęcia paragonu, lub kamerę w celu wykonania go.
        if(extras.getString(MainViewActivity.CAMERA_OR_MEDIA).equals("cam")){
            takePhoto();
        }else if(extras.getString(MainViewActivity.CAMERA_OR_MEDIA).equals("media")) {
            openGallery();
        }
    }

    // Metoda ustawia zezwolenie na zapis w pamięci zewnętrznej
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    //Funkcja otwierająca galerię w celu wbrania zdjęcia paragonu
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    // Metoda otwierająca kamerę
    private void takePhoto(){
        // Deklaracja intencji
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Stworzenie pliku w którym zostanie zapisane zdjęcie
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Jeśli plik został pomyślnie utworzony, pobierz Uri i przekaż je do aktywności
            // wykonującej zdjęcie
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Gdy zostanie wybrane zdjęcie z galerii
        if (resultCode == RESULT_OK && requestCode == 100) {

            // Pobierz uri pliku
            activeUri = data.getData();
            // Znajdź i zapisz rzeczywistą ścieżkę do pliku
            String path = getRealPathFromURI(activeUri);

            // Jeśli aplikacja jest w trybie paragonów
            if(showParagons == true){
                // Wywołaj aktywność pozwalającą na wybór konturu paragonu na zdjęciu
                Intent intent = new Intent(getApplicationContext(), ChoosePointsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.IMAGE_PATH, path);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_GET_RECEIPT);
            }else{
                // Wyświetl zdjęcia na interfejsie użytkownika
                Bitmap chosenImage = BitmapFactory.decodeFile(path);
                pickedImageView.setImageBitmap(chosenImage);
            }

            // Jeśli nastąpił powrót z aktywności wyboru zdjęcia
        }else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){

            // Jeśli aplikacja pracuje w trybie paragonów
            if(showParagons == true){
                // Wywołaj aktywność pozwalającą na zaznaczenie konturu paragonu
                Intent intent = new Intent(getApplicationContext(), ChoosePointsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.IMAGE_PATH, mCurrentPhotoPath);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_GET_RECEIPT);
            }else{
                // W przeciwnym wypadku zapisz Uri pliku zdjęcia i wyświetl zdjęcie na interfejsie
                activeUri = mUri;
                Bitmap chosenImage = BitmapFactory.decodeFile(mCurrentPhotoPath);
                pickedImageView.setImageBitmap(chosenImage);
            }

            // Jeśli nastąpił powrót z aktywności wyboru konturów
        }else if(requestCode == REQUEST_GET_RECEIPT){
            // Pobierz dane obrazu
            Bundle bundle = data.getExtras();
            // Zapisz uri pliku obrazu
            activeUri = Uri.parse(bundle.getString(Constants.IMAGE_URI));
            // Wyświetl obraz na interfejsie użytkownika
            Bitmap chosenImage = BitmapFactory.decodeFile(bundle.getString(Constants.IMAGE_PATH));
            pickedImageView.setImageBitmap(chosenImage);

            // uruchom przetwarzanie obrazu i wyszukiwanie tekstu w oddzielnym wątku
            processImage(this,  Uri.parse(bundle.getString(Constants.IMAGE_URI)), bundle.getString(Constants.IMAGE_PATH));
        }

        // Jeśli nie zostało wybrane ani wykonane zdjęcie zakończ działanie aktywności
        if(activeUri == null){
            finish();
        }
    }

    // Metoda wywołuje przetwarzanie obrazu i wyszukiwanie tekstu w oddzielnym wątku
    private void processImage(Context context, Uri uri, String path){

        // Przekaż odpowiednie dane (ścieżkę i uri obrazu)
        Bundle bundle = new Bundle();
        bundle.putString(Constants.IMAGE_PATH, path);
        bundle.putString(Constants.IMAGE_URI, uri.toString());

        // Uruchom przetwarzanie w nowym wątku
        mServiceIntent = new Intent(context, ImageProcessor.class);
        mServiceIntent.putExtras(bundle);
        context.startService(mServiceIntent);
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
        // Stworzenie nazwy pliku
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Stworzenie pliku w lokalizacji Pictures w pamięci urządzenia
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Zapisanie ścieżki absolutnej do pliku
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Klasa pozwala na odebranie zgłoszenia od obiektu przetwarzającego obraz w oddzielnym wątku
    // gdy zakończy pracę
    private class FixedImageReceiver extends BroadcastReceiver{

        private FixedImageReceiver(){}

        // Gdy odebrane zostanie zgłoszenie
        @Override
        public void onReceive(Context context, Intent intent) {
            // Pobierz i zapisz przekazane dane
            Bundle extras = intent.getExtras();
            imgToSave = extras.getString(Constants.IMAGE_PATH);
            textFromImage = extras.getString(Constants.RECEIPT_TEXT);
            receiptValue = extras.getString(Constants.RECEIPT_VAL);
            receiptDate = extras.getString(Constants.RECEIPT_DATE);

            // Ustaw wartosci odpowiednich pól interfejsu użytkownika
            valueField.setText(receiptValue);
            dateField.setText(receiptDate);

            // Usuń ikonę ładowania z interfejsu ...
            progressBar = (ProgressBar)findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            // ... i ustaw przycisk pozwalający na dodanie nowego wpisu jako widoczny
            addToDBBtn.setVisibility(View.VISIBLE);
        }
    }

}


