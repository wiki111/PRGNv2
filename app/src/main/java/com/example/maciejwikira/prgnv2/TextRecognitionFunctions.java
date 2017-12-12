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


// Klasa realizuje rozpoznawanie i pobieranie tekstu z podanego obrazu
public class TextRecognitionFunctions {

    //Deklaracje zmiennych
    private Context context;

    private Bitmap activeBitmap;
    private String imgToSave;
    private String prgnText;
    private boolean valueFound;
    private boolean dateFound;
    private String paragonValue;
    private String paragonDate;
    private Matcher matchValue, matchDate;
    //Wzory do wyszukiwania wartości zakupu :
    private Pattern wholeValue2 = Pattern.compile("(s\\w*a|su\\w*|\\w*uma|\\w*ma)(\\s*)(pln|\\w*ln|pl\\w*)(\\s*)\\d+(,|\\.)\\d+");
    private Pattern theValue = Pattern.compile("([0-9]+(,|\\.)[0-9]+)");
    //Wzór do wyszukiwania daty :
    private Pattern theDate = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    // Kontruktor
    public TextRecognitionFunctions(Context context){
        // Inicjalizacja podstawowych zmiennych
        this.context = context;
        paragonValue = null;
        paragonDate = null;
        prgnText = "";
    }

    // Metoda typu GET pozwala na pobranie ścieżki do przetwarzanego obrazu
    public String getImgToSave(){
        return this.imgToSave;
    }

    // Metoda typu GET pozwala na pobranie tekstu sczytanego z paragonu
    public String getPrgnText(){
        return this.prgnText;
    }

    // Metoda typu GET pozwalająca na pobranie znalezionej wartości paragonu
    public String getReceiptValue(){
        if(paragonValue != null)
            return this.paragonValue;
        else
            return "";
    }
    // Metoda typu GET pozwalająca na pobranie znalezionej daty paragonu
    public String getReceiptDate(){
        if(paragonDate != null)
            return this.paragonDate;
        else
            return "";
    }

    // Metoda wyszukuje tekst w podanym obrazie
    public void searchInBitmap(Bitmap bitmap){

        // Inicjalizacja obiektu rozpoznającego tekst
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Inicjalizacja obszaru rozpoznawania
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        // Zapisanie sczytanego tekstu
        SparseArray<TextBlock> items = textRecognizer.detect(frame);
        prgnText += "\n";
        for(int i = 0; i < items.size(); i++){
            TextBlock item = items.valueAt(i);

            //wyszukiwanie wartości w sczytanym tekście
            //checkForValue(item.getValue().toLowerCase());

            //wyszukiwanie daty w sczytanym tekście
            //checkForDate(item.getValue().toLowerCase());

            prgnText += item.getValue().toLowerCase();

        }

        checkForValue(prgnText);
        checkForDate(prgnText);
    }

    private void checkForValue(String text){
        matchValue = wholeValue2.matcher(text);
        if(matchValue.find() && valueFound == false){
            valueFound = true;
            String temp = matchValue.group().substring(0);
            matchValue = theValue.matcher(temp);
            matchValue.find();
            paragonValue = matchValue.group().substring(0);
        }
    }

    private void checkForDate(String text) {
        matchDate = theDate.matcher(text);
        if (matchDate.find() && dateFound == false) {   //jeśli znaleziono wystąpienie i nie znaleziono jeszcze daty
            dateFound = true;   //ustaw zmienną sygnalizującą znalezienie daty
            paragonDate = matchDate.group().substring(0); //zapisz znalezioną datę
        }
    }

    // Metoda typu SET pozwalająca na ustawienie ścieżki przetwarzanego obrazu
    public void setImgToSave(String realPath){
        imgToSave = realPath;
    }
}
