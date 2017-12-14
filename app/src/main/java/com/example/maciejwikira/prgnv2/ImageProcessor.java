package com.example.maciejwikira.prgnv2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ArrayAdapter;

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

    // Ścieżka do obrazu.
    private String path;

    // Uri obrazu.
    private Uri uri;

    // Ścieżka zmodyfikowanego obrazu.
    private String imgToSave;

    // Tekst rozpoznany na obrazie.
    private String textFromImage;

    // Rozpoznana wartość paragonu.
    private String receiptValue;

    // Rozpoznana data paragonu.
    private String receiptDate;

    private ArrayList<String> imgs;

    private String itemId;

    public ImageProcessor(){
        super("ImageProcessor");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){

        //Pobranie ścieżki i uri obrazu do przetworzenia.
        Bundle extras = workIntent.getExtras();
        itemId = extras.getString(Constants.ITEM_ID);
        imgs = (ArrayList<String>) extras.get(Constants.IMAGE_PATH);

        for (String path : imgs) {
            processImage(path);
        }

        ContentValues foundData = new ContentValues();
        if(receiptValue == null){
            foundData.put(ReceiptContract.Receipt.VALUE, "");
        }else{
            foundData.put(ReceiptContract.Receipt.VALUE, receiptValue);
        }

        if(receiptDate == null){
            foundData.put(ReceiptContract.Receipt.DATE, "");
        }else{
            foundData.put(ReceiptContract.Receipt.DATE, receiptDate);
        }

        foundData.put(ReceiptContract.Receipt.CONTENT, textFromImage);

        String updateSelection = ReceiptContract.Receipt._ID + " = ?";

        ReceiptDbHelper mDbHelper = new ReceiptDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String[] item_id = {itemId};

        try{
            db.update(ReceiptContract.Receipt.TABLE_NAME, foundData, updateSelection, item_id);
        }catch (Exception e){

        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_receipt)
                        .setContentTitle("Przetwarzanie paragonu")
                        .setContentText("Znaleziono nowe dane paragonu, który został ostatnio dodany !");

        Intent resultIntent = new Intent(this, DetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(MainViewActivity.CARDS_OR_RECEIPTS, true);
        bundle.putString("item_id", itemId);
        resultIntent.putExtras(bundle);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotId, mBuilder.build());

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    private void processImage(String path){

        Bitmap testBitmap;

        // Deklaracja obiektu klasy obsługującej rozpoznawanie tekstu na obrazie.
        TextRecognitionFunctions textRecognitionFunctions = new TextRecognitionFunctions(this);

        // Zapisanie obrazu w postaci macierzy.
        Mat originalImage = Highgui.imread(path);

        testBitmap = Bitmap.createBitmap(originalImage.cols(), originalImage.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(originalImage, testBitmap);

        // Przetworzenie odwzorowania kolorów z przestrzeni BGR na RGB.
        Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);



        // Deklaracja macierzy w której zostanie zapisany zmodyfikowany obraz.
        Mat modifiedImage = new Mat();

        // Przetworzenie obrazu do skali szarości i zapisanie w nowej macierzy.
        Imgproc.cvtColor(originalImage, modifiedImage, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(modifiedImage, testBitmap);

        // Zastosowanie rozmycia Gaussa.
        Imgproc.GaussianBlur(modifiedImage, modifiedImage, new Size(7,7), 0, 0);

        Utils.matToBitmap(modifiedImage, testBitmap);

        // Zastosowanie progowania.
        Imgproc.adaptiveThreshold(
                modifiedImage,
                modifiedImage,
                255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY,
                31,
                7
        );

        Utils.matToBitmap(modifiedImage, testBitmap);

        // Skopiowanie obrazu po przetworzeniu do nowej macierzy.
        Mat imageForOcr = modifiedImage.clone();

        // Odwrócenie kolorów przetworzonego obrazu.
        Core.bitwise_not(modifiedImage, modifiedImage);

        Utils.matToBitmap(modifiedImage, testBitmap);

        // Redukcja macierzy do jednowymiarowego wektora - uzyskanie horyzontalnej projekcji
        // obrazu binarnego - histogramu.
        Mat horProj = new Mat();
        Core.reduce(modifiedImage, horProj, 1, Core.REDUCE_AVG);

        testBitmap = Bitmap.createBitmap(horProj.cols(), horProj.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(horProj, testBitmap);

        // Zastosowanie progowania na histogramie w celu zamortyzowania szumu.
        double threshold = 0;
        Mat hist = new Mat();
        Imgproc.threshold(horProj, hist, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);

        Utils.matToBitmap(hist, testBitmap);

        // Lista współrzędnych Y rzędów macierzy które nie zawierają tekstu.
        ArrayList<Integer> ycoords = new ArrayList<>();

        // Zmienna przechowująca wartość licznika y.
        int y = 0;

        // Zmienna przechowująca wartość licznika count.
        int count = 0;

        boolean isSpace = false;

        // Wyszukiwanie linii tekstu w obrazie na podstawie histogramu i zapisywanie
        // współrzędnych Y linii nie zawierających tekstu do listy.
        for(int i = 0; i < modifiedImage.rows(); ++i){

            if(!isSpace){

                if(hist.get(i,0)[0] > 0){
                    isSpace = true;
                    count = 1;
                    y = i;
                }

            }else{

                if(!(hist.get(i,0)[0] > 0)){

                    isSpace = false;
                    ycoords.add(y/count);

                }else{

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

        Imgproc.cvtColor(modifiedImage, modifiedImage, Imgproc.COLOR_GRAY2RGB);

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

            Core.rectangle(modifiedImage, p1, p4, new Scalar(0,255,0), 5);

            testBitmap = Bitmap.createBitmap(modifiedImage.cols(), modifiedImage.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(modifiedImage, testBitmap);

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

        // Pobranie danych z obiektu rozpoznającego tekst na obrazie
        textFromImage += textRecognitionFunctions.getPrgnText();
        if(textRecognitionFunctions.getReceiptValue() != null)
            receiptValue = textRecognitionFunctions.getReceiptValue();

        if(textRecognitionFunctions.getReceiptDate() != null)
            receiptDate = textRecognitionFunctions.getReceiptDate();





    }

}
