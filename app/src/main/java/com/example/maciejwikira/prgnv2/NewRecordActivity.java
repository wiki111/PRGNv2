package com.example.maciejwikira.prgnv2;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Aktywność pozwala na dodanie nowego wpisu do bazy danych lub edycję wpisu.
public class NewRecordActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GET_RECEIPT = 2;

    //Deklaracja elementów interfejsu
    private EditText nameField;
    private Spinner catSpinner;
    private EditText valueField;
    private EditText dateField;
    private EditText dscField;
    private Button addToDbBtn;
    private Button addReceiptPhoto;
    private Button itemImageAddBtn;
    private SeekBar warrantySeekBar;
    private TextView warrantyTextView;
    private ImageView itemPhotoView;

    private Uri activeUri;

    private String itemImagePath;

    private ViewPager viewPager;
    private ViewPagerImageAdapter viewPagerImageAdapter;


    private String mCurrentPhotoPath;
    private Uri mUri;
    private boolean showReceipts;
    private Intent mServiceIntent;

    private ArrayList<String> imgsToSave;
    private String textFromImage;
    private String receiptValue;
    private String receiptDate;

    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase db;
    private Cursor catCursor;
    private ArrayList<String> categories;
    private String chosenCategory;
    private ArrayAdapter<String> catAdapter;


    private Toast toast;

    private String categoryColumn;
    private String[] projection;
    private String selection;
    private String categoriesTable;
    private String itemTable;
    private String isFavorited;
    private String updateSelection;
    private String itemId;
    private String photosTable;

    private boolean update;
    private boolean changingItemImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        initOpenCV();
        requestWritePerm();

        final Context context = getApplicationContext();

        //Inicjalizacja elementów interfejsu
        catSpinner = (Spinner)findViewById(R.id.catSpinner);

        nameField = (EditText)findViewById(R.id.nameField);
        valueField = (EditText) findViewById(R.id.valueField);
        dateField = (EditText) findViewById(R.id.dateField);
        dscField = (EditText)findViewById(R.id.dscField);
        addToDbBtn = (Button)findViewById(R.id.addToDBButton);
        addReceiptPhoto = (Button)findViewById(R.id.addParagonPhotoBtn);
        warrantySeekBar = (SeekBar) findViewById(R.id.warrantySeekBar);
        warrantyTextView = (TextView) findViewById(R.id.warrantyTextView);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        itemImageAddBtn = (Button)findViewById(R.id.itemImageAddBtn);
        itemPhotoView = (ImageView)findViewById(R.id.itemPhotoView);

        dscField.setText(" ");
        imgsToSave = new ArrayList<>();
        categories = new ArrayList<>();
        update = false;
        changingItemImage = false;
        itemImagePath = null;

        warrantySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                warrantyTextView.setText("Czas gwarancji : " + progress + " miesięcy");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        addReceiptPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePhotoPopup(v);

            }
        });

        addToDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showReceipts){
                    addReceipt();
                    processImage(context);
                }else{
                    addCard();
                }
            }
        });

        itemImageAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePhotoPopup(v);
                changingItemImage = true;
            }
        });

        viewPagerImageAdapter = new ViewPagerImageAdapter(context, imgsToSave, showReceipts);
        viewPager.setAdapter(viewPagerImageAdapter);

        mDbHelper = new ReceiptDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        /*
        addToDBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues cv = new ContentValues();

                if(showReceipts == true){
                    //cv.put(ReceiptContract.Receipt.NAME, nameField.getText().toString());
                    //cv.put(ReceiptContract.Receipt.CATEGORY, chosenCategory.toLowerCase());
                    //cv.put(ReceiptContract.Receipt.DATE, dateField.getText().toString().toLowerCase());
                    //cv.put(ReceiptContract.Receipt.VALUE, Double.parseDouble(valueField.getText().toString().replaceAll("," , "\\.")));
                    //cv.put(ReceiptContract.Receipt.IMAGE_PATH, imgToSave);
                    //cv.put(ReceiptContract.Receipt.CONTENT, textFromImage);
                    //cv.put(ReceiptContract.Receipt.FAVORITED, isFavorited);
                    //cv.put(ReceiptContract.Receipt.DESCRIPTION, dscField.getText().toString());
                }else{
                    //cv.put(CardContract.Card.NAME, nameField.getText().toString());
                    //cv.put(CardContract.Card.CATEGORY, chosenCategory.toLowerCase());
                    //cv.put(CardContract.Card.EXPIRATION_DATE, dateField.getText().toString());
                    //cv.put(CardContract.Card.IMAGE_PATH, imgToSave);
                    //cv.put(CardContract.Card.FAVORITED, isFavorited);
                    //cv.put(CardContract.Card.DESCRIPTION, dscField.getText().toString());
                }

                if(update){
                    updateItem(itemId, cv);
                }else{
                    addItem(cv);
                }
            }
        }); */

        handleIntent(getIntent());



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 100) {

            if(data != null){
                activeUri = data.getData();
            }

            if(showReceipts == true){
                if(changingItemImage){
                    itemImagePath = getRealPathFromURI(activeUri);
                    Glide.with(this).load(itemImagePath).into(itemPhotoView);
                    changingItemImage = false;
                }else {
                    runChoosePoints(getRealPathFromURI(activeUri));
                }

            }else{
                /*
                File photoFile;

                try {
                    photoFile = createImageFile();
                    Bitmap bmp = BitmapFactory.decodeFile(path);

                    FileOutputStream out = new FileOutputStream(photoFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    out.flush();
                    out.close();

                    path = mCurrentPhotoPath;
                    activeUri = Uri.fromFile(photoFile);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Glide.with(this).load(path).into(pickedImageView);
                */
            }
        }else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){

            activeUri = mUri;

            if(showReceipts == true){
                if(activeUri != null){
                    if(changingItemImage){
                        itemImagePath = mCurrentPhotoPath;
                        Glide.with(this).load(itemImagePath).into(itemPhotoView);
                        changingItemImage = false;
                    }else{
                        runChoosePoints(mCurrentPhotoPath);
                    }
                }
            }else{
                /*
                activeUri = mUri;
                imgToSave = mCurrentPhotoPath;
                String oldFilePath = imgToSave;
                File photoFile;

                try {
                    photoFile = createImageFile();
                    Bitmap bmp = BitmapFactory.decodeFile(imgToSave);

                    FileOutputStream out = new FileOutputStream(photoFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    out.flush();
                    out.close();

                    File fileToDelete = new File(oldFilePath);
                    fileToDelete.delete();

                    imgToSave = mCurrentPhotoPath;
                    activeUri = Uri.fromFile(photoFile);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Glide.with(this).load(mCurrentPhotoPath).into(pickedImageView);
                */
            }
        }else if(requestCode == REQUEST_GET_RECEIPT){
            if(data != null){
                Bundle bundle = data.getExtras();
                activeUri = Uri.parse(bundle.getString(Constants.IMAGE_URI));
                addImage(bundle.getString(Constants.IMAGE_PATH));
                //Glide.with(this).load(bundle.getString(Constants.IMAGE_PATH)).into(pickedImageView);

                /*processImage(
                        this,
                        Uri.parse(bundle.getString(Constants.IMAGE_URI)),
                        bundle.getString(Constants.IMAGE_PATH)
                );*/
            }
        }
    }

    // Metoda ustawia zezwolenie na zapis w pamięci zewnętrznej
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chosenCategory = categories.get(position).toString().toLowerCase();
        if(chosenCategory.equals("dodaj nową kategorię")){
            showNewCatPopup(view);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        chosenCategory = "brak kategorii";
    }

    @Override
    public void onDestroy(){
        db.close();
        super.onDestroy();
    }

    private void initOpenCV(){
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }
    }

    private void runChoosePoints(String path){
        Intent intent = new Intent(getApplicationContext(), ChoosePointsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.IMAGE_PATH, path);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_GET_RECEIPT);
    }

    private void requestWritePerm(){
        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void setShowReceipts(boolean show){
        if(show){
            showReceipts = true;
            categoryColumn = ReceiptContract.Receipt.CATEGORY;
            projection = Constants.receiptCategoriesProjection;
            selection = Constants.receiptCategoriesSelection;
            categoriesTable = ReceiptContract.Categories.TABLE_NAME;
            itemTable = ReceiptContract.Receipt.TABLE_NAME;
            updateSelection = ReceiptContract.Receipt._ID + " = ?";
            photosTable = ReceiptContract.Receipt_Photos.TABLE_NAME;
        }else{
            showReceipts = false;
            categoryColumn = CardContract.Card.CATEGORY;
            projection = Constants.cardCategoriesProjection;
            selection = Constants.cardCategoriesSelection;
            categoriesTable = CardContract.Card_Categories.TABLE_NAME;
            itemTable = CardContract.Card.TABLE_NAME;
            updateSelection = CardContract.Card._ID + " = ?";
            photosTable = null;
        }
    }

    private void addImage(String path){
        imgsToSave.add(path);
        viewPagerImageAdapter = new ViewPagerImageAdapter(getApplicationContext(), imgsToSave, showReceipts);
        viewPager.setAdapter(viewPagerImageAdapter);
    }

    private void addReceipt(){
        ContentValues cv = new ContentValues();
        cv.put(ReceiptContract.Receipt.NAME, nameField.getText().toString());
        cv.put(ReceiptContract.Receipt.CATEGORY, chosenCategory.toLowerCase());
        cv.put(ReceiptContract.Receipt.DATE, "");
        cv.put(ReceiptContract.Receipt.VALUE, "");
        cv.put(ReceiptContract.Receipt.IMAGE_PATH, itemImagePath);
        cv.put(ReceiptContract.Receipt.DESCRIPTION, dscField.getText().toString());
        cv.put(ReceiptContract.Receipt.WARRANTY, Integer.toString(warrantySeekBar.getProgress()));
        addItem(cv);
    }

    private void addCard(){
        //TODO implement adding cards
    }

    private void addItem(ContentValues itemData){
        try{
            checkCategory(db, itemData);
            Long id = db.insert(itemTable, null, itemData);
            itemId = Long.toString(id);

            ContentValues pathData = new ContentValues();

            for (String path : imgsToSave) {

                if(showReceipts){
                    pathData.put(ReceiptContract.Receipt_Photos.PHOTO_PATH, path);
                    pathData.put(ReceiptContract.Receipt_Photos.RECEIPT_ID, id.intValue());
                }

                db.insert(photosTable, null, pathData);
            }

            String confirmationText;
            if(showReceipts){
                confirmationText = Integer.toString(R.string.toast_add_new_receipt_success);
            }else{
                confirmationText = Integer.toString(R.string.toast_add_new_card_success);
            }

            toast = Toast.makeText(
                    getApplicationContext(),
                    confirmationText,
                    Toast.LENGTH_LONG
            );

            toast.show();

        }catch (Exception e) {

            toast = Toast.makeText(
                    getApplicationContext(),
                    R.string.toast_add_new_failure + e.toString(),
                    Toast.LENGTH_LONG
            );

            toast.show();

        }
    }

    private Cursor getCatCursor(Boolean showReceipts){

        if(showReceipts){
            return db.query(
                    true,
                    ReceiptContract.Categories.TABLE_NAME,
                    Constants.receiptCategoriesProjection,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }else{
            return db.query(
                    true,
                    CardContract.Card_Categories.TABLE_NAME,
                    Constants.cardCategoriesProjection,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }


    }

    private void getCategoriesFromDb(){
        catCursor = getCatCursor(showReceipts);

        while(catCursor.moveToNext()){
            categories.add(
                    catCursor.getString(
                            catCursor.getColumnIndex(
                                    CardContract.Card_Categories.CATEGORY_NAME
                            )
                    )
            );
        }
        categories.add(categories.size(), "Dodaj nową kategorię");
        catAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.category_spinner_item,
                categories
        );
        catSpinner.setAdapter(catAdapter);
        catSpinner.setOnItemSelectedListener(this);
    }

    //Funkcja otwierająca galerię w celu wbrania zdjęcia paragonu
    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    // Metoda otwierająca kamerę
    private void takePhoto(){
        // Deklaracja intencji
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Stworzenie pliku w którym zostanie zapisane zdjęcie
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Jeśli plik został pomyślnie utworzony, pobierz Uri i przekaż je do aktywności
            // wykonującej zdjęcie
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                mUri = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void processImage(Context context){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ITEM_ID, itemId);
        bundle.putStringArrayList(Constants.IMAGE_PATH, imgsToSave);
        mServiceIntent = new Intent(context, ImageProcessor.class);
        mServiceIntent.putExtras(bundle);
        context.startService(mServiceIntent);

        Intent result = new Intent(context, MainViewActivity.class);
        Bundle extras = new Bundle();
        extras.putBoolean(Constants.RECEIPT_PROCESSING, true);
        result.putExtras(extras);
        setResult(Constants.RESULT_PROCESSING, result);
        finish();
    }

    //Pozyskanie absolutnej sciezki do pliku na podstawie Uri
    @SuppressWarnings("deprecation")
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName;

        if(showReceipts == true){
             imageFileName = "receipt_" + timeStamp;
        }else{
            imageFileName = "card_" + timeStamp;
        }

        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                ) + "/Paragon_App_Photos"
        );
        storageDir.mkdirs();

        File image = new File(storageDir, imageFileName + ".jpg");

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void showNewCatPopup(View view){

        ConstraintLayout mainLayout = (ConstraintLayout)findViewById(R.id.newRecMainLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.new_category_popup, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        final EditText newCatName = (EditText)popupView.findViewById(R.id.newCatName);

        Button addNewCatBtn = (Button)popupView.findViewById(R.id.addNewCatBtn);
        addNewCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenCategory = newCatName.getText().toString().toLowerCase();
                categories.add(1, newCatName.getText().toString().toLowerCase());
                catAdapter.notifyDataSetChanged();
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(projection[1],  chosenCategory);
                try{
                    checkCategory(db, newCategoryValue);
                    toast = Toast.makeText(getApplicationContext(), R.string.toast_add_category_successful, Toast.LENGTH_SHORT);
                    toast.show();
                    setCategoryText(chosenCategory.toLowerCase());
                    popupWindow.dismiss();
                }catch (Exception e){
                    toast = Toast.makeText(getApplicationContext(), R.string.toast_add_new_failure, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public void showChangePhotoPopup(View view){

        ConstraintLayout mainLayout = (ConstraintLayout)findViewById(R.id.newRecMainLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.change_photo_popup, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        Button fromGallery = (Button)popupView.findViewById(R.id.fromGallery);
        fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                popupWindow.dismiss();
            }
        });

        Button fromCamera = (Button)popupView.findViewById(R.id.fromCamera);
        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                popupWindow.dismiss();
            }
        });
    }

    /*
        private class FixedImageReceiver extends BroadcastReceiver{
        private FixedImageReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            ContentValues newItemData = new ContentValues();

            if(showReceipts){
                newItemData.put(ReceiptContract.Receipt.CONTENT, extras.getString(Constants.RECEIPT_TEXT));
                newItemData.put(ReceiptContract.Receipt.VALUE, extras.getString(Constants.RECEIPT_VAL));
                newItemData.put(ReceiptContract.Receipt.DATE, extras.getString(Constants.RECEIPT_DATE));
            }

            IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        FixedImageReceiver fixedImageReceiver = new FixedImageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(fixedImageReceiver, intentFilter);
        }
    }
    */

    private boolean checkCategory( SQLiteDatabase db, ContentValues itemData){

        String categoryName = itemData.get(categoryColumn).toString().toLowerCase();
        String[] selectionArgs = {categoryName};

        Cursor cursor = db.query(
                categoriesTable,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        if((cursor != null) && (cursor.getCount() > 0)){
            cursor.close();
            return true;
        }else{
            insertNewCategory(db, categoryName);
            cursor.close();
            return false;
        }

    }

    private void insertNewCategory(SQLiteDatabase db, String categoryName){
        ContentValues newCategoryValue = new ContentValues();
        newCategoryValue.put(
                projection[1],
                categoryName.toLowerCase()
        );
        db.insert(
                categoriesTable,
                null,
                newCategoryValue
        );
    }

    private void handleIntent(Intent intent){
        Bundle extras = intent.getExtras();
        setShowReceipts(extras.getBoolean(MainViewActivity.CARDS_OR_RECEIPTS));
        update = extras.getBoolean(Constants.UPDATE);
        getCategoriesFromDb();
        ArrayList<String> data = null;
        if(!update){
            if(extras.getString(MainViewActivity.CAMERA_OR_MEDIA).equals("cam")){
                takePhoto();
            }else if(extras.getString(MainViewActivity.CAMERA_OR_MEDIA).equals("media")) {
                openGallery();
            }
        }else{
            data = extras.getStringArrayList(Constants.ITEM_DATA);
        }

        setLayoutElementsContent(showReceipts, update, data);
    }

    private void setLayoutElementsContent(Boolean showReceipts, Boolean update, ArrayList<String> data){
        if(update){
            if(showReceipts){
                nameField.setText(data.get(0));
                setCategoryText(data.get(1));
                valueField.setText(data.get(2));
                dateField.setText(data.get(3));
                itemImagePath = data.get(4);
                textFromImage = data.get(5);
                isFavorited = data.get(6);
                dscField.setText(data.get(7));
                itemId = data.get(8);
                warrantySeekBar.setProgress(Integer.parseInt(data.get(9)));
                addToDbBtn.setText("Aktualizuj");
            }else{
                nameField.setText(data.get(0));
                setCategoryText(data.get(1));
                dateField.setText(data.get(2));
                itemImagePath = data.get(3);
                isFavorited = data.get(4);
                dscField.setText(data.get(5));
                addToDbBtn.setText("Aktualizuj");
            }
        }else{
            if(showReceipts){
                addToDbBtn.setText("Dodaj paragon");
                LinearLayout valRow = (LinearLayout) findViewById(R.id.valRow);
                LinearLayout dateRow = (LinearLayout) findViewById(R.id.dateRow);
                valRow.setVisibility(View.GONE);
                dateRow.setVisibility(View.GONE);
            }else {
                TextView dateView = (TextView)findViewById(R.id.dateTextView);
                dateView.setText("Data wygaśnięcia");
                addToDbBtn.setText("Dodaj kartę");
                LinearLayout valRow = (LinearLayout)findViewById(R.id.valRow);
                valRow.setVisibility(View.GONE);
            }
        }

    }

    private void setCategoryText(String text){
            for(int i = 0; i < catSpinner.getCount(); i++){
                if(catSpinner.getAdapter().getItem(i).toString().toLowerCase().contains(text.toLowerCase())){
                    catSpinner.setSelection(i);
                }
            }
    }

    private void updateItem(String id, ContentValues newItemData){
        try{
            db = mDbHelper.getWritableDatabase();
            checkCategory(db, newItemData);
            String[] item_id = {id};
            db.update(itemTable, newItemData, updateSelection, item_id);

            toast.makeText(getApplicationContext(), R.string.toast_update_successful, Toast.LENGTH_SHORT);
            toast.show();

        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(getApplicationContext(), R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }finally {
            db.close();
        }
    }

}


