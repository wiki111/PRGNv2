package com.example.maciejwikira.prgnv2;

import android.Manifest;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder;
import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.vision.text.Line;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// Aktywność pozwala na dodanie nowego wpisu do bazy danych lub edycję wpisu.
public class NewRecordActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerFragment.onDateSetListener{

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
    private Button deletePhoto;
    private ImageButton addCatBtn;
    private SeekBar warrantySeekBar;
    private TextView warrantyTextView;
    private ImageView itemPhotoView;
    private ImageView left, right;
    private LinearLayout valRow;
    private LinearLayout dateRow;
    private TextView dateView;
    private CheckBox processImgCB;
    private RelativeLayout dim;


    private Uri activeUri;

    private String itemImagePath;

    private ViewPager viewPager;
    private ViewPagerImageAdapter viewPagerImageAdapter;


    private String mCurrentPhotoPath;
    private Uri mUri;
    private boolean showReceipts;
    private Intent mServiceIntent;

    private ArrayList<String> imgsToSave;
    private ArrayList<String> loadedImgs;
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
    private String updatePhotoSelection;
    private String itemId;
    private String photosTable;
    private String[] photoProjection;
    private String photoSelection;
    private String photoColumn;

    private boolean update;
    private boolean changingItemImage;
    private boolean processingOn;
    private boolean errorOccured;

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
        valRow = (LinearLayout)findViewById(R.id.valRow);
        dateRow = (LinearLayout)findViewById(R.id.dateRow);
        dateView = (TextView)findViewById(R.id.dateTextView);
        processImgCB = (CheckBox)findViewById(R.id.processImageCB);
        deletePhoto = (Button)findViewById(R.id.deletePhoto);
        left = (ImageView)findViewById(R.id.left);
        right = (ImageView)findViewById(R.id.right);
        dim = (RelativeLayout) findViewById(R.id.dim);
        addCatBtn = (ImageButton)findViewById(R.id.addCatBtn);

        processImgCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    processingOn = true;
                }else{
                    processingOn = false;
                }
            }
        });

        dscField.setText(" ");
        imgsToSave = new ArrayList<>();
        loadedImgs = new ArrayList<>();
        categories = new ArrayList<>();
        update = false;
        changingItemImage = false;
        processingOn = true;
        errorOccured = false;
        itemImagePath = null;

        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arg = new Bundle();
                arg.putString("Field", "new");
                newFragment.setArguments(arg);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

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
                    errorOccured = false;
                    valRow.setBackgroundColor(getResources().getColor(R.color.white));
                    if(showReceipts){
                        addReceipt(update);
                    }else {
                        addCard(update);
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

        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    deleteWarning(loadedImgs.get(viewPager.getCurrentItem()), viewPager.getCurrentItem());
                }catch (Exception e){
                    toast = Toast.makeText(getApplicationContext(), "Co niby próbujesz usunąć ? Tu nie ma żadnego zdjęcia.", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });

        setViewPagerAdapter(imgsToSave);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                right.setVisibility(View.VISIBLE);
                left.setVisibility(View.VISIBLE);

                if(position == viewPagerImageAdapter.getCount() - 1){
                    right.setVisibility(View.INVISIBLE);
                }else if(position == 0){
                    left.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        addCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewCatPopup(v);
            }
        });

        mDbHelper = new ReceiptDbHelper(this);
        db = mDbHelper.getWritableDatabase();

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
                if(changingItemImage){
                    itemImagePath = getRealPathFromURI(activeUri);
                    Glide.with(this).load(itemImagePath).into(itemPhotoView);
                    changingItemImage = false;
                }else{
                    String path = saveImageInAppFolder(getRealPathFromURI(activeUri));
                    addImage(path);
                }

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
                if(changingItemImage){
                    itemImagePath = mCurrentPhotoPath;
                    Glide.with(this).load(itemImagePath).into(itemPhotoView);
                    changingItemImage = false;
                }else{
                    String path = saveImageInAppFolder(mCurrentPhotoPath);
                    addImage(path);
                }

            }
        }else if(requestCode == REQUEST_GET_RECEIPT){
            if(data != null){
                Bundle bundle = data.getExtras();
                activeUri = Uri.parse(bundle.getString(Constants.IMAGE_URI));
                addImage(bundle.getString(Constants.IMAGE_PATH));
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
            setCategoryText(" ");
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
            updatePhotoSelection = ReceiptContract.Receipt_Photos.RECEIPT_ID + " = ?";
            photosTable = ReceiptContract.Receipt_Photos.TABLE_NAME;
            photoProjection = Constants.receiptPhotosTableCols;
            photoSelection = ReceiptContract.Receipt_Photos.RECEIPT_ID + " = ?";
            photoColumn = ReceiptContract.Receipt_Photos.PHOTO_PATH;
        }else{
            showReceipts = false;
            categoryColumn = CardContract.Card.CATEGORY;
            projection = Constants.cardCategoriesProjection;
            selection = Constants.cardCategoriesSelection;
            categoriesTable = CardContract.Card_Categories.TABLE_NAME;
            itemTable = CardContract.Card.TABLE_NAME;
            updateSelection = CardContract.Card._ID + " = ?";
            photosTable = null;
            updatePhotoSelection = CardContract.Card_Photos.CARD_ID + " = ?";
            photosTable = CardContract.Card_Photos.TABLE_NAME;
            photoProjection = Constants.cardPhotosTableCols;
            photoSelection = CardContract.Card_Photos.CARD_ID + " = ?";
            photoColumn = CardContract.Card_Photos.PHOTO_PATH;
        }
    }

    private void addImage(String path){
        imgsToSave.add(path);
        loadedImgs.add(path);
        setViewPagerAdapter(loadedImgs);
    }

    private void addReceipt(Boolean update){
        ContentValues cv = new ContentValues();
        Double value;

        cv.put(
            ReceiptContract.Receipt.NAME,
            nameField.getText().toString());
        cv.put(
            ReceiptContract.Receipt.CATEGORY,
            chosenCategory.toLowerCase());
        cv.put(
            ReceiptContract.Receipt.DATE,
            dateField.getText().toString());

        if(update){
            String val =
                valueField
                .getText()
                .toString()
                .replaceAll(",", ".");
            value =  Double.parseDouble(val);

            int integerPlaces = val.indexOf('.');
            int decimalPlaces = val.length() - integerPlaces - 1;

            if(decimalPlaces > 2){
                errorOccured = true;
                valRow
                    .setBackgroundColor(
                        getResources()
                        .getColor(R.color.validation_problem_color));
                toast =
                    Toast.makeText(
                        getApplicationContext(),
                        "Podana wartość jest nieprawidłowa.",
                        Toast.LENGTH_LONG);
                toast.show();
            }else{
                cv.put(
                    ReceiptContract.Receipt.VALUE,
                    value);
            }
        }

        cv.put(
            ReceiptContract.Receipt.IMAGE_PATH,
            itemImagePath);
        cv.put(
            ReceiptContract.Receipt.DESCRIPTION,
            dscField.getText().toString());
        cv.put(
            ReceiptContract.Receipt.WARRANTY,
            Integer.toString(warrantySeekBar.getProgress()));

        if(!errorOccured){
            if(update){
                updateItem(itemId,cv);
                if(processingOn){
                    processImage(getApplicationContext());
                }
            }else {
                addItem(cv);
                processImage(getApplicationContext());
            }
        }
    }

    private void addCard(Boolean update){
        ContentValues cv = new ContentValues();
        cv.put(CardContract.Card.NAME, nameField.getText().toString());
        cv.put(CardContract.Card.CATEGORY, chosenCategory.toLowerCase());
        cv.put(CardContract.Card.EXPIRATION_DATE, dateField.getText().toString());
        cv.put(CardContract.Card.IMAGE_PATH, itemImagePath);
        cv.put(CardContract.Card.DESCRIPTION, dscField.getText().toString());
        if(update){
            updateItem(itemId, cv);
        }else{
            addItem(cv);
        }
    }

    private void addItem(ContentValues itemData){
        try{
            Long id = db.insert(itemTable, null, itemData);
            itemId = Long.toString(id);

            ContentValues pathData = new ContentValues();

            for (String path : imgsToSave) {

                    pathData.put(
                        photoProjection[1],
                        path);
                    pathData.put(
                        photoProjection[2],
                        id.intValue());

                db.insert(photosTable, null, pathData);
            }

            String confirmationText;
            if(showReceipts){
                confirmationText =
                    getString(R.string.toast_add_new_receipt_success);
            }else{
                confirmationText =
                    getString(R.string.toast_add_new_card_success);
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
        bundle.putStringArrayList(Constants.IMAGE_PATH, loadedImgs);
        mServiceIntent = new Intent(context, ImageProcessor.class);
        mServiceIntent.putExtras(bundle);
        context.startService(mServiceIntent);

        toast = Toast.makeText(getApplicationContext(), "Pozyskuję informacje ze zdjęcia ... ", Toast.LENGTH_LONG);
        toast.show();

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
        dim.setVisibility(View.VISIBLE);
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
                ContentValues newCategoryValue = new ContentValues();
                newCategoryValue.put(projection[1],  chosenCategory);
                try{
                    if(checkCategory(db, newCategoryValue)){
                        toast = Toast.makeText(getApplicationContext(), R.string.toast_add_category_successful, Toast.LENGTH_SHORT);
                        categories.add(1, newCatName.getText().toString().toLowerCase());
                        catAdapter.notifyDataSetChanged();
                        setCategoryText(chosenCategory.toLowerCase());

                    }else {
                        toast = Toast.makeText(getApplicationContext(), "Kategoria istnieje.", Toast.LENGTH_SHORT);
                    }
                    toast.show();
                    dim.setVisibility(View.GONE);
                    popupWindow.dismiss();
                }catch (Exception e){
                    toast = Toast.makeText(getApplicationContext(), R.string.toast_add_new_failure, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dim.setVisibility(View.GONE);
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
        dim.setVisibility(View.VISIBLE);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //dim.setVisibility(View.GONE);
                popupWindow.dismiss();
                return true;
            }
        });

        Button fromGallery = (Button)popupView.findViewById(R.id.fromGallery);
        fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                //dim.setVisibility(View.GONE);
                popupWindow.dismiss();
            }
        });

        Button fromCamera = (Button)popupView.findViewById(R.id.fromCamera);
        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                //dim.setVisibility(View.GONE);
                popupWindow.dismiss();
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dim.setVisibility(View.GONE);
            }
        });
    }

    private boolean checkCategory( SQLiteDatabase db, ContentValues itemData){

        String categoryName = itemData.get(projection[1]).toString().toLowerCase();
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
            return false;
        }else{
            insertNewCategory(db, categoryName);
            cursor.close();
            return true;
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
            if(showReceipts){
                loadImages(data.get(8));
            }else{
                loadImages(data.get(6));
            }
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
                Glide
                    .with(this)
                    .load(itemImagePath)
                    .into(itemPhotoView);
                textFromImage = data.get(5);
                isFavorited = data.get(6);
                dscField.setText(data.get(7));
                itemId = data.get(8);
                warrantySeekBar
                    .setProgress(Integer.parseInt(data.get(9)));
                addToDbBtn.setText("Aktualizuj");
                valRow.setVisibility(View.VISIBLE);
                dateRow.setVisibility(View.VISIBLE);
                processingOn = false;
            }else{
                nameField.setText(data.get(0));
                setCategoryText(data.get(1));
                dateField.setText(data.get(2));
                itemImagePath = data.get(3);
                Glide
                    .with(this)
                    .load(itemImagePath)
                    .into(itemPhotoView);
                isFavorited = data.get(4);
                dscField.setText(data.get(5));
                itemId = data.get(6);
                addToDbBtn.setText("Aktualizuj");
                dateView.setText("Data wygaśnięcia");
                warrantySeekBar.setVisibility(View.GONE);
                warrantyTextView.setVisibility(View.GONE);
                valRow.setVisibility(View.GONE);
                processImgCB.setVisibility(View.GONE);
            }
        }else{
            if(showReceipts){
                addToDbBtn.setText("Dodaj paragon");
                valRow.setVisibility(View.GONE);
                dateRow.setVisibility(View.GONE);
                processImgCB.setVisibility(View.GONE);
            }else {
                dateView.setText("Data wygaśnięcia");
                addToDbBtn.setText("Dodaj kartę");
                valRow.setVisibility(View.GONE);
                warrantySeekBar.setVisibility(View.GONE);
                warrantyTextView.setVisibility(View.GONE);
                processImgCB.setVisibility(View.GONE);
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
            String[] item_id = {id};
            db.update(itemTable, newItemData, updateSelection, item_id);

            ContentValues pathData = new ContentValues();

            for (String path : imgsToSave) {

                pathData.put(photoProjection[1], path);
                pathData.put(photoProjection[2], Integer.valueOf(id));

                db.insert(photosTable, null, pathData);
            }

            toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_update_successful), Toast.LENGTH_SHORT);
            toast.show();

        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_add_new_failure) + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void loadImages(String itemId){
        String[] selectionArgs = new String[]{itemId};
        Cursor photoCursor
            = db.query(
                photosTable,
                photoProjection,
                photoSelection,
                selectionArgs,
                null,
                null,
                null);
        while (photoCursor.moveToNext()){
            loadedImgs.add(
                photoCursor.getString(
                    photoCursor.getColumnIndex(photoColumn)));
        }

        setViewPagerAdapter(loadedImgs);
    }

    private String saveImageInAppFolder(String path){
        try{
            File imageFile = createImageFile();
            Bitmap bmp = BitmapFactory.decodeFile(path);

            FileOutputStream out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();

            bmp = null;

            return mCurrentPhotoPath;
        }catch (Exception e){
            return "";
        }
    }

    @Override
    public void dateSet(String date, String field) {
        dateField.setText(date);
    }

    private void deleteWarning(String path, int id){

        final String photoPath = path;
        final int photoId = id;

        // Wyświetlenie okna z prośbą o potwierdzenie.
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(NewRecordActivity.this, R.style.myDialog));

        builder.setMessage("Czy na pewno chcesz skasować to zdjęcie ?").setTitle("Usuwanie zdjęcia");

        final String[] selArgs = new String[]{ path };

        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePhoto(photoPath, photoId);
            }
        });

        builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePhoto(String path, int id){
        String sel;
        if(showReceipts){
            sel = ReceiptContract.Receipt_Photos.PHOTO_PATH + " = ?";
        }else{
            sel = CardContract.Card_Photos.PHOTO_PATH + " = ? ";
        }

        String[] selArgs = new String[]{path};

        try{
            loadedImgs.remove(id);
        }catch(Exception e){
            toast = Toast.makeText(getApplicationContext(), "Smth went wrong. + id = " + Integer.toString(id), Toast.LENGTH_SHORT);
            toast.show();
        }

        try{
            imgsToSave.remove(imgsToSave.indexOf(path));
        }catch (Exception e){
            //do nothing
        }


        db.delete(photosTable, sel, selArgs);

        try{
            new File(path).delete();
        }catch (Exception e){
            toast = Toast.makeText(getApplicationContext(), "Nie znaleziono pliku zdjęcia do skasowania.", Toast.LENGTH_SHORT);
            toast.show();
        }

        setViewPagerAdapter(loadedImgs);
    }

    private void setViewPagerAdapter(ArrayList<String> imgs){

        viewPagerImageAdapter = new ViewPagerImageAdapter(getApplicationContext(), imgs, showReceipts);
        viewPager.setAdapter(viewPagerImageAdapter);

        if(viewPagerImageAdapter.getCount() == 1 || viewPagerImageAdapter.getCount() == 0){
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
        }else{
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.VISIBLE);
        }

    }

}


