package com.example.maciejwikira.prgnv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
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
    Aktywność pozwala na wskazanie konturów paragonu na zdjęciu,
    odpowiedniu przycina obraz, oraz wykonuje korektę perspektywy.
 */
public class ChoosePointsActivity extends AppCompatActivity {

    // Widok wyświetlający ruchomy czworokąt, wskazujący kontur paragonu.
    private ChoosePointsView cpv;

    private FrameLayout sourceFrame;
    private ImageView sourceImageView;

    // Oryginalny (nieprzetworzony) obraz.
    private Bitmap original;

    // Ścieżka do oryginalnego obrazu.
    private String originalPath;

    // Ścieżka do zmodyfikowanego obrazu
    private String path;

    // Uri zmodyfikowanego obrazu.
    private Uri uri;

    private Button getPointsBtn;
    private int padding;
    private boolean bitmapSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_points);

        // Wywołanie metody inicjalizującej.
        init();
    }

    // Metoda incjalizująca. Wykonuje ładowanie i wyświetlenie obrazu,
    // oraz czworokąta wyznaczającego kontur paragonu.
    private void init(){

        bitmapSet = false;
        uri = null;
        path = null;
        cpv = (ChoosePointsView) findViewById(R.id.choosePointsView);
        sourceImageView = (ImageView)findViewById(R.id.sourceImageView);
        sourceFrame = (FrameLayout)findViewById(R.id.source_frame);

        // Załadowanie i wyświetlenie obrazu w oddzielnym wątku.
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

        // Po załadowaniu bitmapy ustawiane są punkty wyznaczające czworokąt.
       sourceImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override
           public void onGlobalLayout() {
               if(bitmapSet){

                   int height = sourceImageView.getHeight();
                   int width = sourceImageView.getWidth();

                   padding = (int) getResources().getDimension(R.dimen.scan_padding);

                   // Ustawienie punktów wyznaczających czworokąt.
                   Map<Integer, PointF> points = new HashMap<>();
                   points.put(0, new PointF(0 ,0));
                   points.put(1, new PointF(width, 0));
                   points.put(2, new PointF(0, height));
                   points.put(3, new PointF(width, height));
                   cpv.setPoints(points);

                   // Ustawienie parametrów widoku wyświetlającego czworokąt.
                   FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width + 2 * padding, height + 2 * padding);
                   layoutParams.gravity = Gravity.CENTER;
                   cpv.setLayoutParams(layoutParams);

                   // Wyświetlenie czworokąta.
                   cpv.setVisibility(View.VISIBLE);

                   // Zakończenie nasłuchiwania na zdarzenia.
                   cpv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
           }
       });

        getPointsBtn = (Button)findViewById(R.id.getPointsBtn);
        getPointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Zapisanie punktów wierzchołków czworokąta.
                Map<Integer, PointF> points = cpv.getPoints();

                // Zapisanie wymiarów wyświetlanej bitmapy.
                Drawable drawable = sourceImageView.getDrawable();
                int bitmapWidth = drawable.getIntrinsicWidth();
                int bitmapHeight = drawable.getIntrinsicHeight();

                // Zapisanie oryginalnego obrazu jako macierzy.
                Mat originalImage = Highgui.imread(originalPath);
                Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);

                // Zapisanie stosunku wymiarów oryginalnej bitmapy do wymiarów wyświetlanego obrazu.
                float xRatio = (float) originalImage.cols() / (bitmapWidth - padding / 2);
                float yRatio = (float) originalImage.rows() / (bitmapHeight - padding / 2);

                // Mapowanie punktów na oryginalny obraz.
                float x1 = (points.get(0).x - padding / 4 ) * xRatio;
                float x2 = (points.get(1).x - padding / 4 ) * xRatio;
                float x3 = (points.get(2).x - padding / 4 ) * xRatio;
                float x4 = (points.get(3).x - padding / 4 ) * xRatio;
                float y1 = (points.get(0).y - padding / 4 ) * yRatio;
                float y2 = (points.get(1).y - padding / 4 ) * yRatio;
                float y3 = (points.get(2).y - padding / 4 ) * yRatio;
                float y4 = (points.get(3).y - padding / 4 ) * yRatio;

                // Zapisanie punktów do listy.
                ArrayList<Point> rect = new ArrayList<Point>();
                Point tl = new Point((double) x1, (double) y1);
                Point tr = new Point((double) x2, (double) y2);
                Point bl = new Point((double) x3, (double) y3);
                Point br = new Point((double) x4, (double) y4);
                rect.add(tl);
                rect.add(tr);
                rect.add(bl);
                rect.add(br);

                // Obliczenie długości najszerszego boku czworokąta.
                Double widthA = Math.sqrt(Math.pow((br.x - bl.x), 2) + Math.pow((br.y - bl.y), 2));
                Double widthB = Math.sqrt(Math.pow((tr.x - tl.x), 2) + Math.pow((tr.y - tl.y), 2));
                Double maxWidth = getMax(widthA, widthB);

                // Obliczenie długości najwyższego boku czworokąta.
                Double heightA = Math.sqrt(Math.pow((tr.x - br.x), 2) + Math.pow((tr.y - br.y), 2));
                Double heightB = Math.sqrt(Math.pow((tl.x - bl.x), 2) + Math.pow((tl.y - bl.y), 2));
                Double maxHeight = getMax(heightA, heightB);

                // Macierz wskazanych punktów.
                Mat src = Converters.vector_Point2f_to_Mat(rect);
                // Macierz docelowych punktów.
                Mat dst = Converters.vector_Point2f_to_Mat(Arrays.asList(new Point[]{
                        new Point(0,0),
                        new Point(maxWidth, 0),
                        new Point(0, maxHeight),
                        new Point(maxWidth, maxHeight)
                }));

                // Rozmiar poprawionego obrazu.
                Size size = new Size(maxWidth, maxHeight);

                // Macierz przechowująca poprawiony obraz.
                Mat corrected = new Mat(size, originalImage.type());

                // Mapowanie wskazanych punktów na docelowe.
                Mat transformation = Imgproc.getPerspectiveTransform(src, dst);

                // Korekcja perspektywy i zapisanie poprawionego obrazu do macierzy.
                Imgproc.warpPerspective(originalImage, corrected, transformation, corrected.size());

                // Zamiana macierzy w bitmapę.
                Bitmap bitMap = Bitmap.createBitmap(corrected.cols(),
                        corrected.rows(),Bitmap.Config.RGB_565);
                Utils.matToBitmap(corrected, bitMap);

                // Zapisanie bitmapy w pamięci urządzenia
                saveImage(bitMap);

                // Przekazanie informacji zwrotnej zawierającej ścieżkę do poprawionego
                // obrazu oraz jego Uri.
                Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);
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

    // Metoda zwraca w formie bitmapy obraz, którego ścieżka została przekazana
    // do aktywności.
    private Bitmap getBitmap(){
        Bundle extras = getIntent().getExtras();
        originalPath = extras.getString(Constants.IMAGE_PATH);
        Bitmap image = BitmapFactory.decodeFile(originalPath);
        return image;
    }

    // Metoda wyświetla przeskalowaną bitmapę na interfejsie.
    private void setBitmap(Bitmap image){
        // Skalowanie bitmapy.
        Bitmap scaled = scaledBitmap(image, sourceFrame.getWidth(), sourceFrame.getHeight());
        // Ustawienie bitmapy.
        sourceImageView.setImageBitmap(scaled);
       }

    // Metoda skaluje bitmapę do podanej wysokości i szerokości.
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height){
        Matrix m = new Matrix();
        // Ustawienie mapowania pikseli oryginalnej bitmapy do bitmapy o nowych wymiarach.
        m.setRectToRect(new RectF(0,0,bitmap.getWidth(), bitmap.getHeight()), new RectF(0,0,width,height), Matrix.ScaleToFit.CENTER);
        // Zwrócenie bitmapy o nowych wymiarach.
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    // Metoda zwraca większą spośród dwóch podanych wartości.
    private Double getMax(Double a, Double b){
        if(a >= b){
            return a;
        }else {
            return b;
        }
    }

    // Metoda zapisuje bitmapę w pamięci urządzenia.
    private void saveImage(Bitmap bitmap){

        // Zapisanie uchwytu do lokalizacji docelowej.
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Receipts");

        // Stworzenie odpowiedniej lokalizacji, jeśli nie istnieje.
        if(!folder.exists()){
            if(folder.mkdir()){
                Log.d("Receipt App : ", "Successfully created the parent dir:" + folder.getName());
            }else {
                Log.d("Receipt App : ", "Failed to create the parent dir:" + folder.getName());
            }
        }

        // Generowanie losowej nazwy pliku.
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
            // Zapisanie bitmapy do pliku.
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
