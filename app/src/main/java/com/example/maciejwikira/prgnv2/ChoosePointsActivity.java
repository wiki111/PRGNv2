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

public class ChoosePointsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_choose_points);

        init();
    }

    private void init(){
        bitmapSet = false;
        uri = null;
        path = null;

        frameLayout = (FrameLayout)findViewById(R.id.frame_layout);
        cpv = (ChoosePointsView) findViewById(R.id.choosePointsView);
        sourceImageView = (ImageView)findViewById(R.id.sourceImageView);
        sourceFrame = (FrameLayout)findViewById(R.id.source_frame);
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
       sourceImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
           @Override
           public void onGlobalLayout() {
               int height = sourceImageView.getHeight();
               int width = sourceImageView.getWidth();
               int x = sourceImageView.getLeft();
               int y = sourceImageView.getTop();

               padding = (int) getResources().getDimension(R.dimen.scan_padding);

               Map<Integer, PointF> points = new HashMap<>();
               points.put(0, new PointF(0 ,0));
               points.put(1, new PointF(width , 0));
               points.put(2, new PointF(0, height));
               points.put(3, new PointF(width, height));
               cpv.setPoints(points);
               cpv.setVisibility(View.VISIBLE);

               FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width + 2 * padding, height + 2 * padding);
               layoutParams.gravity = Gravity.CENTER;
               cpv.setLayoutParams(layoutParams);
               if(bitmapSet){
                   cpv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
           }
       });


        getPointsBtn = (Button)findViewById(R.id.getPointsBtn);
        getPointsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<Integer, PointF> points = cpv.getPoints();
                float xRatio = (float) original.getWidth() / (sourceImageView.getWidth() +  padding - 8);
                float yRatio = (float) original.getHeight() / (sourceImageView.getHeight() +  padding - 8);

                float x1 = (points.get(0).x ) * xRatio;
                float x2 = (points.get(1).x ) * xRatio;
                float x3 = (points.get(2).x ) * xRatio;
                float x4 = (points.get(3).x ) * xRatio;
                float y1 = (points.get(0).y ) * yRatio;
                float y2 = (points.get(1).y ) * yRatio;
                float y3 = (points.get(2).y ) * yRatio;
                float y4 = (points.get(3).y ) * yRatio;

                Mat originalImage = Highgui.imread(originalPath);
                Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);

                ArrayList<Point> rect = new ArrayList<Point>();

                Point tl = new Point((double) x1, (double) y1);
                Point tr = new Point((double) x2, (double) y2);
                Point bl = new Point((double) x3, (double) y3);
                Point br = new Point((double) x4, (double) y4);

                rect.add(tl);
                rect.add(tr);
                rect.add(bl);
                rect.add(br);

                /* kod testowy
                    for( Point point : rect){
                        Core.circle(originalImage, point, 16, new Scalar(255,0,0), 10, 8, 0);
                    }

                    Bitmap origin = Bitmap.createBitmap(originalImage.cols(),
                            originalImage.rows(),Bitmap.Config.RGB_565);
                    Utils.matToBitmap(originalImage, origin);
                */

                Double widthA = Math.sqrt(Math.pow((br.x - bl.x), 2) + Math.pow((br.y - bl.y), 2));
                Double widthB = Math.sqrt(Math.pow((tr.x - tl.x), 2) + Math.pow((tr.y - tl.y), 2));
                Double maxWidth = getMax(widthA, widthB);

                Double heightA = Math.sqrt(Math.pow((tr.x - br.x), 2) + Math.pow((tr.y - br.y), 2));
                Double heightB = Math.sqrt(Math.pow((tl.x - bl.x), 2) + Math.pow((tl.y - bl.y), 2));
                Double maxHeight = getMax(heightA, heightB);

                Mat src = Converters.vector_Point2f_to_Mat(rect);
                Mat dst = Converters.vector_Point2f_to_Mat(Arrays.asList(new Point[]{
                        new Point(0,0),
                        new Point(maxWidth, 0),
                        new Point(0, maxHeight),
                        new Point(maxWidth, maxHeight)
                }));

                Size size = new Size(maxWidth, maxHeight);

                Mat corrected = new Mat(size, originalImage.type());
                Mat transformation = Imgproc.getPerspectiveTransform(src, dst);
                Imgproc.warpPerspective(originalImage, corrected, transformation, corrected.size());

                Bitmap bitMap = Bitmap.createBitmap(corrected.cols(),
                        corrected.rows(),Bitmap.Config.RGB_565);
                Utils.matToBitmap(corrected, bitMap);

                saveImage(bitMap);

                Intent intent = new Intent();
                Bundle extras = new Bundle();
                extras.putString(Constants.IMAGE_PATH, path);
                extras.putString(Constants.IMAGE_URI, uri.toString());
                intent.putExtras(extras);
                setResult(NewRecordActivity.REQUEST_GET_RECEIPT, intent);
                finish();
            }
        });
    }

    private Bitmap getBitmap(){
        Bundle extras = getIntent().getExtras();
        originalPath = extras.getString(Constants.IMAGE_PATH);
        Bitmap image = BitmapFactory.decodeFile(originalPath);
        return image;
    }

    private void setBitmap(Bitmap image){
        Bitmap scaled = scaledBitmap(image, sourceFrame.getWidth(), sourceFrame.getHeight());
        sourceImageView.setImageBitmap(scaled);
       }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height){
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0,0,bitmap.getWidth(), bitmap.getHeight()), new RectF(0,0,width,height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Double getMax(Double a, Double b){
        if(a >= b){
            return a;
        }else {
            return b;
        }
    }

    private void saveImage(Bitmap bitmap){

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Receipts");
        if(!folder.exists()){
            if(folder.mkdir()){
                Log.d("Paragon App : ", "Successfully created the parent dir:" + folder.getName());
            }else {
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        path = file.getAbsolutePath();
        uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", file);
    }

}
