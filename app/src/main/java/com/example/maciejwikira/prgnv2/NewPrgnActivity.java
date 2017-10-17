package com.example.maciejwikira.prgnv2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewPrgnActivity extends AppCompatActivity {

    //Deklaracja elementów interfejsu :
    private EditText nameField;
    private EditText categoryField;
    private EditText valueField;
    private EditText dateField;
    private Button addToDBBtn;

    //Deklaracje obiektów potrzebnych do przetworzenia obrazów :
    private Bitmap activeBitmap;
    private Uri activeUri;
    private String imgToSave;

    //Wskaźniki przechowujące informacje o tym, czy znaleziono datę i wartość paragonu :
    private boolean valueFound = false;
    private boolean dateFound = false;

    private Matcher match;  //obiekt umożliwiający wyszukiwanie wyrażenia w danym łańcuchu znaków

    //Wzory do wyszukiwania wartości zakupu :
    private Pattern wholeValue = Pattern.compile("suma pln");
    private Pattern wholeValue2 = Pattern.compile("suma pln \\d+,\\d+");
    private Pattern wholeValue3 = Pattern.compile("([0-9]+,[0-9]+ pln)(.*?)");
    private Pattern theValue = Pattern.compile("([0-9]+(,|\\.)[0-9]+)");

    //Wzór do wyszukiwania daty :
    private Pattern theDate = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    String prgnText;

    //funkcja onCreate - inicjalizacja obiektów interfejsu użytkownika i wywołanie funkcji aktywności
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_prgn);

        final Context context = getApplicationContext();    //zapisanie kontekstu do zmiennej

        final ParagonDbHelper mDbHelper = new ParagonDbHelper(context);

        //inicjalizacja elementów interfejsu użytkownika
        nameField = (EditText)findViewById(R.id.nameField);
        categoryField = (EditText) findViewById(R.id.categoryField);
        valueField = (EditText) findViewById(R.id.valueField);
        dateField = (EditText) findViewById(R.id.dateField);
        addToDBBtn = (Button)findViewById(R.id.addToDBButton);

        //Obsługa kliknięcia przycisku dodającego dane do bazy
        addToDBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    //Stworzenie obiektu przechowującego dane do zapisania w bazie danych aplikacji
                    ContentValues cv = new ContentValues();
                    cv.put("name", nameField.getText().toString()); //nazwa wpisu
                    cv.put("category", categoryField.getText().toString()); //kategoria wpisu
                    cv.put("date", dateField.getText().toString()); //data paragonu
                    cv.put("value", Double.parseDouble(valueField.getText().toString().replaceAll("," , "\\.")));   //wartość paragonu
                    cv.put("img", imgToSave);   //ścieżka absolutna do zdjęcia paragonu
                    cv.put("text", prgnText);

                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    db.insert(ParagonContract.Paragon.TABLE_NAME, null, cv); //dodanie rekordu do bazy danych

                    //Wyświetlenie potwierdzenia pomyślnego wykonania operacji
                    Toast toast = Toast.makeText(context, "Yay, everything went good !!! " , Toast.LENGTH_LONG);
                    toast.show();
                }catch (Exception e){
                    //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
                    Toast toast = Toast.makeText(context, "Smth went very, very wrong ... " + e.toString(), Toast.LENGTH_LONG);
                    toast.show();
                }
                //zamknięcie bazy danych
            }
        });

        openGallery();  //wywołanie funkcji otwierającej galerię w celu wyborania zdjęcia paragonu

    }

    //Funkcja otwierająca galerię w celu wbrania zdjęcia paragonu
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    //Funkcja wywoływana przy powrocie z zewnętrznej aktywności - wyboru zdjęcia lub wskazywania obszaru
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // fragment kodu wykonywany, gdy następuje powrtót z aktywności wyboru zdjęcia paragonu
        if (resultCode == RESULT_OK && requestCode == 100) {
            Context context = getApplicationContext();
            activeUri = data.getData(); //pobierz uri obrazu z danych zwróconych przez aktywność i zapisz do zmiennej

            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();    //inicjalizacja obiektu odpowiedzialnego za rozpoznawanie tekstu
            try {
                activeBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), activeUri); //pobranie obrazu na podstawie pozyskanego uri
                imgToSave = getRealPathFromURI(activeUri);  //zapis ścieżki absolutnej do obrazu do zmiennej
                Frame frame = new Frame.Builder().setBitmap(activeBitmap).build();  //inicjalizacja obiektu przechowywującego obraz, z którego sczytywane są dane

                SparseArray<TextBlock> items = textRecognizer.detect(frame);    //sczytywanie danych z obrazu i zapis do tablicy

                prgnText = "";
                for(int i = 0; i < items.size(); i++){
                    TextBlock item = items.valueAt(i);
                    prgnText += item.getValue().toLowerCase();
                }

                if(valueFound == false) //jeśli nie znaleziono wartości
                    valueField.setText(searchForTheValue(items));   // wyszukaj wartość w odczytanych danych

                if(dateFound == false)  //jeśli nie znaleziono daty
                    dateField.setText(searchForTheDate(items)); //wyszukaj datę w odczytanych danych

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { //Fragment kodu wykonywany przy powrocie z aktywności wyboru obszaru

            CropImage.ActivityResult result = CropImage.getActivityResult(data);    //pobranie danych zwróconych przez aktywność

            if (resultCode == RESULT_OK) {  //jeśli aktywność zakończyła się pomyślnie

                Uri resultUri = result.getUri();    //pobierz uri wskazanego na obrazie obszaru

                Context context = getApplicationContext();  //zapisz kontekst

                TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();    //inicjalizacja obiektu odpowiedzialnego za rozpoznawanie tekstu
                try {
                    Bitmap croppedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri); //pobranie obrazu na podstawie pozyskanego uri
                    Frame frame = new Frame.Builder().setBitmap(croppedBitmap).build(); //inicjalizacja obiektu przechowywującego obraz, z którego sczytywane są dane

                    SparseArray<TextBlock> items = textRecognizer.detect(frame);    //sczytywanie danych z obrazu i zapis do tablicy

                    if(valueFound == false) //jeśli nie znaleziono wartości
                        valueField.setText(searchForTheValue(items));   // wyszukaj wartość w odczytanych danych

                    if(dateFound == false) //jeśli nie znaleziono daty
                        dateField.setText(searchForTheDate(items)); //wyszukaj datę w odczytanych danych

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }

        }

    }

    //Funkcja wyszukująca wartość paragonu
    private String searchForTheValue(SparseArray<TextBlock> items){

        String val = "";    //inicjalizacja zmiennej przechowującej znalezioną wartość
        String foundVal;    //zmienna tymczasowa przechowująca wartość

        //Pętla przeszukująca każdą linię sczytanego z obrazu tekstu
        for (int i = 0; i < items.size(); ++i) {

            TextBlock item = items.valueAt(i);  //pobranie linii tekstu

            //Sprawdzanie, czy linia pasuje do danego wzorca
            match = wholeValue3.matcher(item.getValue().toLowerCase());
            if(match.find() && valueFound == false){    //jeśli znaleziono wyrażenie pasujące do wzorca i nie znaleziono żadnego wcześniej

                valueFound = true;  //ustaw zmienną sygnalizującą znalezienie poszukiwanej wartości
                foundVal = match.group().substring(0);  //zapisz pierwsze wystąpienie wyszukiwanej wartości do zmiennej

                //pozyskanie wartości zakupu jako liczby ze znalezionego wyrażenia
                match = theValue.matcher(foundVal);
                match.find();
                val = match.group().substring(0);


            }

            //Sprawdzanie, czy linia pasuje do danego wzorca
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

            //Sprawdzanie, czy linia pasuje do danego wzorca
            match = wholeValue2.matcher(item.getValue().toLowerCase());
            if(match.find() && valueFound == false){

                valueFound = true;
                foundVal = match.group().substring(0);

                match = theValue.matcher(foundVal);
                match.find();
                val = match.group().substring(0);

            }

            if(valueFound == true){ // jeśli znaleziono wartość

                //Wyświetlenie potwierdzenia odnalezienia wartości
                Toast toast = Toast.makeText(this, "Znaleziono wartosc ! : " +  val , Toast.LENGTH_SHORT);
                toast.show();

                return val; // zwrócenie znalezionej wartości

            }


        }
        /*
        if(valueFound == false){ // jeśli nie udało się znaleźć wartości

            //Wyświetl informację dla użytkownika
            Toast toast = Toast.makeText(this, "Wskaż miejsce gdzie powinna znajdować się wartość" , Toast.LENGTH_LONG);
            toast.show();

            //Uruchom aktywność wyboru obszaru obrazu
            CropImage.activity(activeUri)
                    .start(this);

        }
        */

        return val;
    }

    //Funkcja wyszukująca datę
    private String searchForTheDate(SparseArray<TextBlock> items){

        String foundDate = "";  //inicjalizacja zmiennej przechowującej datę

        for (int i = 0; i < items.size(); ++i) {    //sprawdzanie każdej linii w poszukiwaniu daty

            TextBlock item = items.valueAt(i);  // pobranie linii tekstu

            //Sprawdzenie czy w tekście znajduje się wyrażenie pasujące do wzorca
            match = theDate.matcher(item.getValue().toLowerCase());
            if (match.find() && dateFound == false) {   //jeśli znaleziono wystąpienie i nie znaleziono jeszcze daty

                dateFound = true;   //ustaw zmienną sygnalizującą znalezienie daty
                foundDate = match.group().substring(0); //zapisz znalezioną datę

                //Wyświetl potwierdzenie znalezienia daty
                Toast toast = Toast.makeText(this, "Znaleziono datę ! : " +  foundDate , Toast.LENGTH_SHORT);
                toast.show();

            }
        }

        /*
        if(dateFound == false){ //jeśli nie udało się znaleźć daty

            //Wyświetl informację dla użytkownika
            Toast toast = Toast.makeText(this, "Wskaż miejsce gdzie powinna znajdować się data", Toast.LENGTH_LONG);
            toast.show();

            //Uruchom aktywność wyboru obszaru obrazu
            CropImage.activity(activeUri)
                    .start(this);

        }
        */

       return foundDate;    //zwróć znalezioną datę
    }

    //Pozyskaj absolutną ścieżkę obrazu na podstawie uri
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

}
