package com.example.maciejwikira.prgnv2;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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
        super("ImageFixerService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        Bundle extras = workIntent.getExtras();
        path = extras.get(Constants.IMAGE_PATH).toString();
        uri = Uri.parse(extras.get(Constants.IMAGE_URI).toString());

        Mat originalImage = Highgui.imread(path);
        Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_BGR2RGB);

        Mat modifiedImage = new Mat();
        Imgproc.cvtColor(originalImage, modifiedImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(modifiedImage, modifiedImage, new Size(7,7), 0, 0);
        Imgproc.adaptiveThreshold(modifiedImage, modifiedImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 41, 10);

        /* Kod testowy
        Bitmap testMap = Bitmap.createBitmap(modifiedImage.cols(),
                modifiedImage.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(modifiedImage, testMap);
        */

        TextRecognitionFunctions textRecognitionFunctions = new TextRecognitionFunctions(this);
        textRecognitionFunctions.searchForValues(uri, path, this);

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
