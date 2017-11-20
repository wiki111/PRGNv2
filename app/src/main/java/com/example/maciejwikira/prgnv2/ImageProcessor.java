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

public class ImageProcessor extends IntentService {

    private String path;
    private Uri uri;

    private String imgToSave;
    private String textFromImage;
    private String receiptValue;
    private String receiptDate;

    public ImageProcessor(){
        super("ImageProcessor");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        Bundle extras = workIntent.getExtras();
        path = extras.get(Constants.IMAGE_PATH).toString();
        uri = Uri.parse(extras.get(Constants.IMAGE_URI).toString());
        TextRecognitionFunctions textRecognitionFunctions = new TextRecognitionFunctions(this);

        Mat originalImage = Highgui.imread(path);
        Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);

        Mat modifiedImage = new Mat();
        Imgproc.cvtColor(originalImage, modifiedImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(modifiedImage, modifiedImage, new Size(7,7), 0, 0);
        Imgproc.adaptiveThreshold(modifiedImage, modifiedImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 31, 7);

        Mat imageForOcr = modifiedImage.clone();

        Core.bitwise_not(modifiedImage, modifiedImage);

        RotatedRect rect = null;
        Mat pts = Mat.zeros(modifiedImage.size(), modifiedImage.type());
        Core.findNonZero(modifiedImage, pts);

        MatOfPoint mpts = new MatOfPoint(pts);
        MatOfPoint2f mpts2f = new MatOfPoint2f(mpts.toArray());

        if(mpts2f.rows() > 0){
            rect = Imgproc.minAreaRect(mpts2f);
        }

        if(rect.size.width > rect.size.height){
            Double temp;
            temp = rect.size.width;
            rect.size.width = rect.size.height;
            rect.size.height = temp;
            rect.angle += 90.f;
        }

        Point[] vertices = new Point[4];
        rect.points(vertices);

        for(int i = 0; i <  4; i++){
            Core.line(originalImage, vertices[i], vertices[(i+1)%4], new Scalar(0,255,0), 10);
        }

        Bitmap testMap = Bitmap.createBitmap(originalImage.cols(),
                originalImage.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(originalImage, testMap);

        Mat rotated = new Mat();
        Mat M = Imgproc.getRotationMatrix2D(rect.center, rect.angle, 1.0);
        Imgproc.warpAffine(modifiedImage, rotated, M, modifiedImage.size());

        Mat horProj = new Mat();
        Core.reduce(rotated, horProj, 1, Core.REDUCE_AVG);

        double threshold = 0;
        Mat hist = new Mat();
        Imgproc.threshold(horProj, hist, threshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);

        ArrayList<Integer> ycoords = new ArrayList<>();
        int y = 0;
        int count = 0;
        boolean isSpace = false;

        for(int i = 0; i < rotated.rows(); ++i){
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

        Mat result = new Mat();
        Imgproc.cvtColor(rotated, result, Imgproc.COLOR_GRAY2RGB);

        Bitmap lineMap;
        Rect line;
        Mat lineMat;
        Point p1,p4;

        for(int i = 0; i < ycoords.size(); ++i){
            //Core.line(result, new Point(0, ycoords.get(i)), new Point(result.cols(), ycoords.get(i)), new Scalar(0,255,0), 5);
            if(i == 0){
                p1 = new Point(0, 0);
            }else{
                p1 = new Point(0, ycoords.get(i-1));
            }

            p4 = new Point(result.cols(), ycoords.get(i));

            line = new Rect(p1, p4);
            lineMat = new Mat(imageForOcr, line);

            Core.rectangle(result, p1, p4, new Scalar(255,0,0), 5, 8, 0);

            lineMap = Bitmap.createBitmap(lineMat.cols(), lineMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(lineMat, lineMap);

            textRecognitionFunctions.searchInBitmap(lineMap);

        }

        Bitmap resultMap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(result, resultMap);

        testMap = Bitmap.createBitmap(imageForOcr.cols(),
                imageForOcr.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageForOcr, testMap);

        textRecognitionFunctions.setImgToSave(path);

        imgToSave = textRecognitionFunctions.getImgToSave();
        textFromImage = textRecognitionFunctions.getPrgnText();
        receiptValue = textRecognitionFunctions.getReceiptValue();
        receiptDate = textRecognitionFunctions.getReceiptDate();

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
