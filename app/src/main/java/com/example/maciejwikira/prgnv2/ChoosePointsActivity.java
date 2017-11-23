package com.example.maciejwikira.prgnv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
    Aktywność wyboru konturów paragonu na zdjęciu. Zdjęcie jest przycinane, oraz poprawiana jest
    perspektywa, tak by w efekcie obraz paragonu wyglądał jak skan.
 */
public class ChoosePointsActivity extends AppCompatActivity {

    // Deklaracje zmiennych
    private ChoosePointsView cpv;
    private FrameLayout sourceFrame;
    private Bitmap original;
    private String originalPath;
    private FrameLayout frameLayout;
    private ImageView sourceImageView;
    private Button getPointsBtn;
    private int padding;
    private String path;
    private Uri uri;
    private boolean bitmapSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ustawienie widoku i wywołanie metody incjalizującej
        setContentView(R.layout.activity_choose_points);
        init();
    }

    // Metoda incjalizująca
    private void init(){

        // incjalizacja zmiennych
        bitmapSet = false;
        uri = null;
        path = null;

        // incjalizacja elementów interfejsu
        frameLayout = (FrameLayout)findViewById(R.id.frame_layout);
        cpv = (ChoosePointsView) findViewById(R.id.choosePointsView);
        sourceImageView = (ImageView)findViewById(R.id.sourceImageView);
        sourceFrame = (FrameLayout)findViewById(R.id.source_frame);

        // Załadowanie bitmapy do widoku w oddzielnym wątku
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                original = getBitmap();
                if (original != null) {
                    setBitmap(original);
                    bitmapSet = true;
                }
            }
        });

        // Gdy utworzony zostanie widok ustawiane są punkty będące narożnikami ruchomego czworokąta,
        // pozwalającego na wskazanie przez użytkownika konturów paragonu na zdjęciu.
       sourceImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override
           public void onGlobalLayout() {
               // punkty ustawiane są po załadowaniu bitmapy
               if(bitmapSet){
                   // pobranie wysokości i szerokości elementu interfejsu na ktorym wyświetlana jest
                   // bitmapa
                   int height = sourceImageView.getHeight();
                   int width = sourceImageView.getWidth();

                   // Ustawienie odstępu
                   padding = (int) getResources().getDimension(R.dimen.scan_padding);

                   // Ustawienie punktów
                   Map<Integer, PointF> points = new HashMap<>();
                   points.put(0, new PointF(0 ,0));
                   points.put(1, new PointF(width , 0));
                   points.put(2, new PointF(0, height));
                   points.put(3, new PointF(width, height));
                   cpv.setPoints(points);

                   // Ustawienie parametrów widoku wyświetlającego czworokąt.
                   FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width + 2 * padding, height + 2 * padding);
                   layoutParams.gravity = Gravity.CENTER;
                   cpv.setLayoutParams(layoutParams);

                   // Ustawienie czworokąta jako widocznego
                   cpv.setVisibility(View.VISIBLE);

                   // Zakończenie nasłuchiwania na zdarzenia.
                   cpv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
           }
       });

        // Ustawienie przycisku zatwierdzającego wybór
        getPointsBtn = (Button)findViewById(R.id.getPointsBtn);
        // Gdy użytkownik naciśnie przycisk
        getPointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pobierz punkty z widoku
                Map<Integer, PointF> points = cpv.getPoints();

                // Zapisanie stosunków wymiarów oryginalnej bitmapy do wymiarów wyświetlanego obrazu
                float xRatio = (float) original.getWidth() / (sourceImageView.getWidth() +  padding - 8);
                float yRatio = (float) original.getHeight() / (sourceImageView.getHeight() +  padding - 8);

                // Odtworzenie pozycji wybranych punktów na oryginalnej bitmapie
                float x1 = (points.get(0).x ) * xRatio;
                float x2 = (points.get(1).x ) * xRatio;
                float x3 = (points.get(2).x ) * xRatio;
                float x4 = (points.get(3).x ) * xRatio;
                float y1 = (points.get(0).y ) * yRatio;
                float y2 = (points.get(1).y ) * yRatio;
                float y3 = (points.get(2).y ) * yRatio;
                float y4 = (points.get(3).y ) * yRatio;

                // Zapisanie obrazu jako macierzy do zmiennej typu Mat z biblioteki OpenCV
                Mat originalImage = Highgui.imread(originalPath);
                Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);

                // Zapisanie punktów do listy typu Point z biblioteki OpenCV
                ArrayList<Point> rect = new ArrayList<Point>();

                Point tl = new Point((double) x1, (double) y1);
                Point tr = new Point((double) x2, (double) y2);
                Point bl = new Point((double) x3, (double) y3);
                Point br = new Point((double) x4, (double) y4);

                rect.add(tl);
                rect.add(tr);
                rect.add(bl);
                rect.add(br);

                // Obliczenie długości najszerszego boku czworokąta
                Double widthA = Math.sqrt(Math.pow((br.x - bl.x), 2) + Math.pow((br.y - bl.y), 2));
                Double widthB = Math.sqrt(Math.pow((tr.x - tl.x), 2) + Math.pow((tr.y - tl.y), 2));
                Double maxWidth = getMax(widthA, widthB);

                // Obliczenie długości najwyższego boku czworokąta
                Double heightA = Math.sqrt(Math.pow((tr.x - br.x), 2) + Math.pow((tr.y - br.y), 2));
                Double heightB = Math.sqrt(Math.pow((tl.x - bl.x), 2) + Math.pow((tl.y - bl.y), 2));
                Double maxHeight = getMax(heightA, heightB);

                // Deklaracja macierzy zawierającej wskazane punkty czworokąta
                Mat src = Converters.vector_Point2f_to_Mat(rect);
                // Deklaracja macierzy zawierającej docelowe punkty czworokąta
                Mat dst = Converters.vector_Point2f_to_Mat(Arrays.asList(new Point[]{
                        new Point(0,0),
                        new Point(maxWidth, 0),
                        new Point(0, maxHeight),
                        new Point(maxWidth, maxHeight)
                }));

                // Deklaracja nowego rozmiaru obrazu
                Size size = new Size(maxWidth, maxHeight);

                // Stworzenie macierzy o odpowiednim rozmiarze i typie zgodnym z typem macierzy
                // przechowującej oryginalną bitmapę
                Mat corrected = new Mat(size, originalImage.type());
                // Obliczenie macierzy transformacji pierwotnych punktów na docelowe
                Mat transformation = Imgproc.getPerspectiveTransform(src, dst);
                // Korekcja perspektywy i stworzenie macierzy przechowującej poprawiony obraz paragonu
                Imgproc.warpPerspective(originalImage, corrected, transformation, corrected.size());

                // Zamiana macierzy w bitmapę
                Bitmap bitMap = Bitmap.createBitmap(corrected.cols(),
                        corrected.rows(),Bitmap.Config.RGB_565);
                Utils.matToBitmap(corrected, bitMap);

                // Zapisanie bitmapy w pamięci urządzenia
                saveImage(bitMap);

                // Przekazanie informacji zwrotnej zawierającej ścieżkę do poprawionego
                // obrazu oraz jego Uri przez aktywność.
                Intent intent = new Intent();
                Bundle extras = new Bundle();
                extras.putString(Constants.IMAGE_PATH, path);
                extras.putString(Constants.IMAGE_URI, uri.toString());
                intent.putExtras(extras);
                setResult(NewRecordActivity.REQUEST_GET_RECEIPT, intent);
                // Zakończenie działania aktywności.
                finish();
            }
        });
    }

    // Metoda pobiera bitmapę przekazaną przez aktywność wywołującą bieżącą aktywność i zwraca ją
    private Bitmap getBitmap(){
        Bundle extras = getIntent().getExtras();
        originalPath = extras.getString(Constants.IMAGE_PATH);
        Bitmap image = BitmapFactory.decodeFile(originalPath);
        return image;
    }

    // Metoda wyświetla przeskalowaną bitmapę w elemencie interfejsu
    private void setBitmap(Bitmap image){
        // Skalowanie bitmapy
        Bitmap scaled = scaledBitmap(image, sourceFrame.getWidth(), sourceFrame.getHeight());
        // Ustawienie bitmapy
        sourceImageView.setImageBitmap(scaled);
       }

    // Metoda skaluje bitmapę do podanej wysokości i szerokości
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height){
        // Deklaracja macierzy
        Matrix m = new Matrix();
        // Ustawienie mapowania pikseli oryginalnej bitmapy do bitmapy o nowych wymiarach
        m.setRectToRect(new RectF(0,0,bitmap.getWidth(), bitmap.getHeight()), new RectF(0,0,width,height), Matrix.ScaleToFit.CENTER);
        // Stworzenie bitmapy o nowych wymiarach i zwrócenie jej przez metodę
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    // Metoda zwraca większą spośród dwóch podanych wartości typu Double.
    private Double getMax(Double a, Double b){
        if(a >= b){
            return a;
        }else {
            return b;
        }
    }

    // Metoda pozwala na zapisanie bitmapy w pamięci urządzenia
    private void saveImage(Bitmap bitmap){

        // Zapisanie uchwytu do lokalizacji
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Receipts");
        // Jeśli lokalizacja nie istnieje, stwórz ją.
        if(!folder.exists()){
            if(folder.mkdir()){
                Log.d("Paragon App : ", "Successfully created the parent dir:" + folder.getName());
            }else {
                Log.d("Paragon App : ", "Failed to create the parent dir:" + folder.getName());
            }
        }

        // Generowanie losowej nazwy pliku
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(folder, fname);

        // Jeżeli plik o takiej nazwie istnieje, skasuj go.
        if(file.exists()){
            file.delete();
        }

        try{
            // Zapisanie bitmapy do pliku
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Pobranie i ustawienie aktualnej rzeczywistej ścieżki do pliku i uri zasobu.
        path = file.getAbsolutePath();
        uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", file);
    }

}
