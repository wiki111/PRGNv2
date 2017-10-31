package com.example.maciejwikira.prgnv2;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maciej on 2017-10-31.
 */

public class TextRecognitionFunctions {

    private Context context;

    private Bitmap activeBitmap;
    private String imgToSave;
    private String prgnText;
    private boolean valueFound;
    private boolean dateFound;
    private String paragonValue;
    private String paragonDate;
    private Matcher match;
    //Wzory do wyszukiwania wartości zakupu :
    private Pattern wholeValue = Pattern.compile("suma pln");
    private Pattern wholeValue2 = Pattern.compile("suma pln \\d+,\\d+");
    private Pattern wholeValue3 = Pattern.compile("([0-9]+,[0-9]+ pln)(.*?)");
    private Pattern theValue = Pattern.compile("([0-9]+(,|\\.)[0-9]+)");
    //Wzór do wyszukiwania daty :
    private Pattern theDate = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    public TextRecognitionFunctions(Context context){
        this.context = context;
        paragonValue = null;
        paragonDate = null;
    }

    public void searchForValues(Uri activeUri,String realPath, Context context){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();    //inicjalizacja obiektu odpowiedzialnego za rozpoznawanie tekstu
        try {
            activeBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), activeUri); //pobranie obrazu na podstawie pozyskanego uri
            imgToSave = realPath;  //zapis ścieżki absolutnej do obrazu do zmiennej
            Frame frame = new Frame.Builder().setBitmap(activeBitmap).build();  //inicjalizacja obiektu przechowywującego obraz, z którego sczytywane są dane

            SparseArray<TextBlock> items = textRecognizer.detect(frame);    //sczytywanie danych z obrazu i zapis do tablicy

            prgnText = "";
            for(int i = 0; i < items.size(); i++){
                TextBlock item = items.valueAt(i);
                prgnText += item.getValue().toLowerCase();
            }

            if(valueFound == false) //jeśli nie znaleziono wartości
                paragonValue = searchForTheValue(items);   // wyszukaj wartość w odczytanych danych

            if(dateFound == false)  //jeśli nie znaleziono daty
                paragonDate = searchForTheDate(items); //wyszukaj datę w odczytanych danych

        } catch (IOException e) {
            e.printStackTrace();
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
                Toast toast = Toast.makeText(context, "Znaleziono wartosc ! : " +  val , Toast.LENGTH_SHORT);
                toast.show();

                return val; // zwrócenie znalezionej wartości

            }

        }

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
                Toast toast = Toast.makeText(context, "Znaleziono datę ! : " +  foundDate , Toast.LENGTH_SHORT);
                toast.show();

            }
        }

        return foundDate;    //zwróć znalezioną datę
    }

    public String getImgToSave(){
        return this.imgToSave;
    }

    public String getPrgnText(){
        return this.prgnText;
    }

    public String getParagonValue(){
        if(paragonValue != null)
            return this.paragonValue;
        else
            return "";
    }

    public String getParagonDate(){
        if(paragonDate != null)
            return this.paragonDate;
        else
            return "";
    }

}