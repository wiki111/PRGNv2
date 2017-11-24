package com.example.maciejwikira.prgnv2;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by Maciej on 2017-11-12.
 */

// Klasa realizuje przetwarzanie obrazu i ekstrakcję zawartości tekstowej.
public class ImageProcessor extends IntentService {

    // Deklaracje zmiennych
    private String path;
    private Uri uri;
    private String imgToSave;
    private String textFromImage;
    private String receiptValue;
    private String receiptDate;

    // Publiczny konstruktor
    public ImageProcessor(){
        super("ImageProcessor");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){

        //Pobranie ścieżki i uri obrazu do przetworzenia
        Bundle extras = workIntent.getExtras();
        path = extras.get(Constants.IMAGE_PATH).toString();
        uri = Uri.parse(extras.get(Constants.IMAGE_URI).toString());

        // Deklaracja klasy obsługującej rozpoznawanie tekstu na obrazie
        TextRecognitionFunctions textRecognitionFunctions = new TextRecognitionFunctions(this);

        // Zapisanie obrazu w postaci macierzy
        Mat originalImage = Highgui.imread(path);
        // Przetworzenie odwzorowania kolorów z przestrzeni BGR na RGB
        Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);

        // Deklaracja macierzy w której zostanie zapisany zmodyfikowany obraz
        Mat modifiedImage = new Mat();
        // Przetworzenie obrazu do skali szarości i zapisanie w nowej macierzy
        Imgproc.cvtColor(originalImage, modifiedImage, Imgproc.COLOR_RGB2GRAY);
        // Zastosowanie rozmycia Gaussa
        Imgproc.GaussianBlur(modifiedImage, modifiedImage, new Size(7,7), 0, 0);
        // Zastosowanie progowania
        Imgproc.adaptiveThreshold(modifiedImage, modifiedImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 31, 7);

        // Skopiowanie obrazu po przetworzeniu do nowej macierzy
        Mat imageForOcr = modifiedImage.clone();

        // Odwrócenie kolorów przetworzonego obrazu
        Core.bitwise_not(modifiedImage, modifiedImage);

        // Redukcja macierzy do jednowymiarowego wektora - uzyskanie horyzontalnej projekcji
        // obrazu binarnego - histogramu
        Mat horProj = new Mat();
        Core.reduce(modifiedImage, horProj, 1, Core.REDUCE_AVG);

        // Zastosowanie progowania na histogramie w celu zamortyzowania szumu
        double threshold = 0;
        Mat hist = new Mat();
        Imgproc.threshold(horProj, hist, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);

        // Lista współrzędnych Y rzędów macierzy które nie zawierają tekstu
        ArrayList<Integer> ycoords = new ArrayList<>();
        // Zmienna przechowująca wartość licznika y
        int y = 0;
        // Zmienna przechowująca wartość licznika count
        int count = 0;
        // Zmienna przechowująca informację o tym, czy dany rząd macierzy należy do linii tekstu
        boolean isSpace = false;

        // Wyszukiwanie linii tekstu w obrazie i zapisywanie współrzędnych Y linii nie zawierających
        // tekstu do listy.
        // Dla każdego rzędu macierzy przechowującej obraz
        for(int i = 0; i < modifiedImage.rows(); ++i){
            // Jeśli ostatni rząd nie należał do linii tekstu
            if(!isSpace){
                // Jeśli odpowiadający rzędowi element projekcji horyzontalnej obrazu po progowaniu
                // ma wartość większą niż zero, oznacza to, że rząd należy do linii tekstu.
                if(hist.get(i,0)[0] > 0){
                    isSpace = true;
                    count = 1;
                    y = i;
                }
            }else{
                // Jeśli ostatni rząd należał do linii tekstu sprawdź czy bieżący też należy
                if(!(hist.get(i,0)[0] > 0)){
                    // Jeśli nie należy ustaw zmienną informującą o przynależności do linii tekstu
                    // na false i dodaj początkową współrzędną Y linii tekstu do listy.
                    isSpace = false;
                    ycoords.add(y/count);
                }else{
                    // W przeciwnym razie zwiększ wartość liczników
                    y += i;
                    count++;
                }
            }
        }


        // Bitmapa zawierająca linię tekstu
        Bitmap lineMap;
        // Prostokąt ograniczający linię tekstu
        Rect line;
        // Macierz zawierająca dane linii tekstu
        Mat lineMat;
        // punkty początkowy i końcowy prostokąta
        Point p1,p4;

        // Wytnij każdą rozpoznaną linię tekstu i wprowadź ją do silnika OCR
        for(int i = 0; i < ycoords.size(); ++i){
            // Definicja punktu początkowego prostokąta ograniczającego linię tekstu
            if(i == 0){
                p1 = new Point(0, 0);
            }else{
                p1 = new Point(0, ycoords.get(i-1));
            }

            // Definicja punktu końcowego prostokąta ograniczającego linię tekstu
            p4 = new Point(modifiedImage.cols(), ycoords.get(i));

            // Zapisanie prostokąta ograniczającego linię tekstu
            line = new Rect(p1, p4);
            // Zapisanie fragmentu macierzy obrazu zawierającego linię tekstu
            lineMat = new Mat(imageForOcr, line);

            // Przetworzenie macierzy zawierającej linię tekstu na bitmapę
            lineMap = Bitmap.createBitmap(lineMat.cols(), lineMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(lineMat, lineMap);

            // Rozpoznawanie tekstu
            textRecognitionFunctions.searchInBitmap(lineMap);

        }

        // Ustawienie ścieżki obrazu który ma zostać zapisany w pamięci urządzenia
        textRecognitionFunctions.setImgToSave(path);

        // Pobranie danych z obiektu rozpoznającego tekst na obrazie
        imgToSave = textRecognitionFunctions.getImgToSave();
        textFromImage = textRecognitionFunctions.getPrgnText();
        receiptValue = textRecognitionFunctions.getReceiptValue();
        receiptDate = textRecognitionFunctions.getReceiptDate();

        // Stworzenie i przekazanie pakietu danych, które zwraca obiekt
        Bundle bundle = new Bundle();
        bundle.putString(Constants.IMAGE_PATH, imgToSave);
        bundle.putString(Constants.RECEIPT_TEXT, textFromImage);
        bundle.putString(Constants.RECEIPT_VAL, receiptValue);
        bundle.putString(Constants.RECEIPT_DATE, receiptDate);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
