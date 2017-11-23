package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Maciej on 2017-11-18.
 */

/*
    Niestandardowy widok wyświetlający ruchomy czworokąt, za pomocą którego użytkownik wskazuje
    kontur paragonu na zdjęciu.
 */
public class ChoosePointsView extends FrameLayout {

    // Deklaracje zmiennych
    private Context context;
    private Paint paint;

    //Deklaracje punktów narożników czworokąta
    private ImageView pointer1;
    private ImageView pointer2;
    private ImageView pointer3;
    private ImageView pointer4;

    //Deklaracje punktów znajdujacych się pośrodku boków prostokąta
    private ImageView midPointer13;
    private ImageView midPointer12;
    private ImageView midPointer34;
    private ImageView midPointer24;

    private ChoosePointsView chooseImageView;


    public ChoosePointsView(Context context){
       super(context);
       this.context = context;
       init();
    }

    public ChoosePointsView(Context context, AttributeSet attrs){
       super(context, attrs);
       this.context = context;
       init();
    }

    public ChoosePointsView(Context context, AttributeSet attrs, int defStyleAttr){
       super(context, attrs, defStyleAttr);
       this.context = context;
       init();
    }

    // Metoda inicjalizująca
    private void init(){
        //Zapisanie uchwytu do bieżącego widoku
        chooseImageView = this;

        // Ustawienie początkowych pozycji punktów
        pointer1 = getImageView(0,0);
        pointer2 = getImageView(getWidth(), 0);
        pointer3 = getImageView(0, getHeight());
        pointer4 = getImageView(getWidth(), getHeight());
        midPointer13 = getImageView(0, getHeight() / 2);
        midPointer12 = getImageView(0, getWidth() / 2);
        midPointer34 = getImageView(0, getHeight() / 2);
        midPointer24 = getImageView(0, getHeight() / 2);

        // Ustawienie nasłuchiwania na zdarzenia przesunięcia środkowych punktów
        midPointer13.setOnTouchListener(new MidPointTouchListenerImpl(pointer1, pointer3));
        midPointer12.setOnTouchListener(new MidPointTouchListenerImpl(pointer1, pointer2));
        midPointer34.setOnTouchListener(new MidPointTouchListenerImpl(pointer3, pointer4));
        midPointer24.setOnTouchListener(new MidPointTouchListenerImpl(pointer2, pointer4));

        // Dodanie punktów do widoku
        addView(pointer1);
        addView(pointer2);
        addView(midPointer13);
        addView(midPointer12);
        addView(midPointer34);
        addView(midPointer24);
        addView(pointer3);
        addView(pointer4);

        // Incjalizacja obiektu przechowującego parametry rysowania obiektów
        initPaint();
   }

   // Metoda dołącza widok jako dziecko bieżącego widoku i ustawia jego parametry
    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    // Metoda inicjalizuje obiekt przechowujący parametry rysowania obiektów
    private void initPaint() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.blue));
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
    }

    // Metoda typu get zwracająca listę uporządkowanych punktów narożników czworokąta wskazanego
    // przez użytkownika
    public Map<Integer, PointF> getPoints() {

        List<PointF> points = new ArrayList<PointF>();
        points.add(new PointF(pointer1.getX(), pointer1.getY()));
        points.add(new PointF(pointer2.getX(), pointer2.getY()));
        points.add(new PointF(pointer3.getX(), pointer3.getY()));
        points.add(new PointF(pointer4.getX(), pointer4.getY()));

        return getOrderedPoints(points);
    }

    // Metoda zwraca uporządkowane punkty narożników w kolejności : górny lewy, górny prawy, dolny
    // lewy, dolny prawy
    public Map<Integer, PointF> getOrderedPoints(List<PointF> points) {

        // Obliczenie centralnego punktu czworokąta
        PointF centerPoint = new PointF();
        int size = points.size();
        for (PointF pointF : points) {
            centerPoint.x += pointF.x / size;
            centerPoint.y += pointF.y / size;
        }

        // Określenie położenia punktów na podstawie pozycji względem punktu centralnego i wpisanie
        // ich w określonej kolejności do struktury danych typu Map (przechowującej klucz i wartość).
        Map<Integer, PointF> orderedPoints = new HashMap<>();
        for (PointF pointF : points) {
            int index = -1;
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0;
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1;
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2;
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3;
            }
            orderedPoints.put(index, pointF);
        }
        return orderedPoints;
    }

    // Ustawienie pozycji punktów
    public void setPoints(Map<Integer, PointF> pointFMap) {
        if (pointFMap.size() == 4) {
            setPointsCoordinates(pointFMap);
        }
    }

    // Metoda ustawia współrzędne punktów na podstawie punktów wybranych przez użytkownika
    private void setPointsCoordinates(Map<Integer, PointF> pointFMap) {
        pointer1.setX(pointFMap.get(0).x);
        pointer1.setY(pointFMap.get(0).y);

        pointer2.setX(pointFMap.get(1).x);
        pointer2.setY(pointFMap.get(1).y);

        pointer3.setX(pointFMap.get(2).x);
        pointer3.setY(pointFMap.get(2).y);

        pointer4.setX(pointFMap.get(3).x);
        pointer4.setY(pointFMap.get(3).y);
    }

    // Metoda rysuje i wyświetla obiekty definiujące czworokąt na określonych pozycjach.
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawLine(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), paint);
        canvas.drawLine(pointer1.getX() + (pointer1.getWidth() / 2), pointer1.getY() + (pointer1.getHeight() / 2), pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), paint);
        canvas.drawLine(pointer2.getX() + (pointer2.getWidth() / 2), pointer2.getY() + (pointer2.getHeight() / 2), pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), paint);
        canvas.drawLine(pointer3.getX() + (pointer3.getWidth() / 2), pointer3.getY() + (pointer3.getHeight() / 2), pointer4.getX() + (pointer4.getWidth() / 2), pointer4.getY() + (pointer4.getHeight() / 2), paint);
        midPointer13.setX(pointer3.getX() - ((pointer3.getX() - pointer1.getX()) / 2));
        midPointer13.setY(pointer3.getY() - ((pointer3.getY() - pointer1.getY()) / 2));
        midPointer24.setX(pointer4.getX() - ((pointer4.getX() - pointer2.getX()) / 2));
        midPointer24.setY(pointer4.getY() - ((pointer4.getY() - pointer2.getY()) / 2));
        midPointer34.setX(pointer4.getX() - ((pointer4.getX() - pointer3.getX()) / 2));
        midPointer34.setY(pointer4.getY() - ((pointer4.getY() - pointer3.getY()) / 2));
        midPointer12.setX(pointer2.getX() - ((pointer2.getX() - pointer1.getX()) / 2));
        midPointer12.setY(pointer2.getY() - ((pointer2.getY() - pointer1.getY()) / 2));
    }

    // Metoda tworzy element wyświetlający punkt narożnika, lub środkowy boku czworokąta na podanych
    // współrzędnych x i y oraz zwraca uchwyt do niego.
    private ImageView getImageView(int x, int y){
        ImageView imageView = new ImageView(context);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(lp);
        imageView.setImageResource(R.drawable.circle);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setOnTouchListener(new TouchListenerImpl());
        return imageView;
    }

    // Klasa implementująca nasłuchiwanie na zdarzenie dotknięcia i przesunięcia przez użytkownika
    // jednego z punktów środkowych boków czworokąta.
    private class MidPointTouchListenerImpl implements OnTouchListener {

        // Deklaracja zmiennych
        PointF DownPT = new PointF();
        PointF StartPT = new PointF();
        private ImageView mainPointer1;
        private ImageView mainPointer2;

        // Metoda obslugująca zdarzenie dotknięcia i przesunięcia obiektu
        public MidPointTouchListenerImpl(ImageView mainPointer1, ImageView mainPointer2) {
            this.mainPointer1 = mainPointer1;
            this.mainPointer2 = mainPointer2;
        }

        // Metoda wywoływana gdy zarejestrowane zostanie zdarzenie dotknięcia obiektu na interfejsie
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Pobranie i zapisanie ID akcji
            int eid = event.getAction();

            // W zależności od rodzaju akcji :
            switch (eid) {
                // Jeśli jest to akcja przesunięcia
                case MotionEvent.ACTION_MOVE:

                    // Zapisanie współrzędnych nowego wskazanego punktu względem punktu początkowego
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);

                    // Jeżeli różnica między współrzędnymi punktów końcowych linii w osi X jest
                    // większa niż różnica w osi Y zmień pozycję obydwu punktów w osi Y w zależności
                    // od wartości przesunięcia
                    if (Math.abs(mainPointer1.getX() - mainPointer2.getX()) > Math.abs(mainPointer1.getY() - mainPointer2.getY())) {

                        // Jeżeli przesunięcie nie przekracza granic widoku zmień współrzędne punktów
                        if (((mainPointer2.getY() + mv.y + v.getHeight() < chooseImageView.getHeight()) && (mainPointer2.getY() + mv.y > 0))) {
                            v.setY((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setY((int) (mainPointer2.getY() + mv.y));
                        }
                        // Jeżeli przesunięcie nie przekracza granic widoku zmień współrzędne punktów
                        if (((mainPointer1.getY() + mv.y + v.getHeight() < chooseImageView.getHeight()) && (mainPointer1.getY() + mv.y > 0))) {
                            v.setY((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setY((int) (mainPointer1.getY() + mv.y));
                        }
                    // W przeciwnym wypadku zmień pozycję punktów w osi X.
                    } else {
                        // Jeżeli przesunięcie nie przekracza granic widoku zmień współrzędne punktów
                        if ((mainPointer2.getX() + mv.x + v.getWidth() < chooseImageView.getWidth()) && (mainPointer2.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setX((int) (mainPointer2.getX() + mv.x));
                        }
                        // Jeżeli przesunięcie nie przekracza granic widoku zmień współrzędne punktów
                        if ((mainPointer1.getX() + mv.x + v.getWidth() < chooseImageView.getWidth()) && (mainPointer1.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setX((int) (mainPointer1.getX() + mv.x));
                        }
                    }

                    break;
                // Gdy nastąpi przyciśnięcie zapisz współrzędne pozycji, w której nastąpiło, i ustaw
                // punkt aktualne współrzędne aktywnego obiektu jako nowe współrzędne początkowe.
                case MotionEvent.ACTION_DOWN:
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
                    break;

                // Gdy obiekt przestanie być przyciśnięty sprawdź czy wynikowy czworokąt jest
                // prawidłowy. Jeśli tak narysuj jego linie za pomocą koloru niebieskiego.
                // W przeciwnym razie użyj koloru pomarańczowego, by powiadomić użytkownika, że
                // wybrany kształt jest nieprawidłowy.
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.blue);
                    } else {
                        color = getResources().getColor(R.color.orange);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }

            // Narysuj punkty i czworokąt od nowa
            chooseImageView.invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    // Metoda sprawdza czy czworokąt ma właściwy kształt - jest tak, jeśli lista zawiera 4
    // punkty. Jeżeli zawiera więcej lub mniej, oznacza to że wskazany kształt nie jest
    // czworokątem.
    public boolean isValidShape(Map<Integer, PointF> pointFMap) {
        return pointFMap.size() == 4;
    }

    // Metoda obsługuje zdarzenia dotknięcia i przesunięcia obiektów punktów narożnych czworokąta.
    private class TouchListenerImpl implements OnTouchListener {

        PointF DownPT = new PointF();
        PointF StartPT = new PointF();

        // Metoda wywoływana po wykryciu dotknięcia
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Zapisanie ID wydarzenia
            int eid = event.getAction();

            switch (eid) {
                // Jeśli wydarzenie to przesunięcie punktu
                case MotionEvent.ACTION_MOVE:
                    // Zapisanie współrzędnych nowego wskazanego punktu względem punktu początkowego
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);

                    // Jeśli przesunięcie mieści się w granicach widoku zmień współrzędne odpowiedniego
                    // punktu i ustaw nowe współrzędne startowe.
                    if (((StartPT.x + mv.x + v.getWidth()) < chooseImageView.getWidth() && (StartPT.y + mv.y + v.getHeight() < chooseImageView.getHeight())) && ((StartPT.x + mv.x) > 0 && StartPT.y + mv.y > 0)) {
                        v.setX((int) (StartPT.x + mv.x));
                        v.setY((int) (StartPT.y + mv.y));
                        StartPT = new PointF(v.getX(), v.getY());
                    }
                    break;

                // Jeśli punkt został naciśnięty
                case MotionEvent.ACTION_DOWN:
                    // Zapisz aktualne współrzędne punktu
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    // Ustaw nowe współrzędne startowe.
                    StartPT = new PointF(v.getX(), v.getY());
                    break;

                // Gdy punkt zostanie puszczony sprawdź czy wynikowy czworokąt jest
                // prawidłowy. Jeśli tak narysuj jego linie za pomocą koloru niebieskiego.
                // W przeciwnym razie użyj koloru pomarańczowego, by powiadomić użytkownika, że
                // wybrany kształt jest nieprawidłowy.
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.blue);
                    } else {
                        color = getResources().getColor(R.color.orange);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }

            // Narysuj punkty i czworokąt od nowa
            chooseImageView.invalidate();
            return true;
        }

    }
}
