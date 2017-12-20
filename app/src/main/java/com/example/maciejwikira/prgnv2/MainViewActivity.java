package com.example.maciejwikira.prgnv2;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Aktywność wyświetla główny ekran aplikacji, zawierającą
    listę na której w zależności od wyboru użytkownika wyświetlane są
    zapisane karty lub paragony. Posiada szufladę nawigacyjną, która
    pozwala na wybór przełączanie trybu aplikacji pomiędzy wyświetlaniem
    paragonów i kart. Zawiera menu, które pozwala na dodanie nowego
    paragonu bądź karty.
 */
public class MainViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DatePickerFragment.onDateSetListener {

    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";
    public static final String CARDS_OR_RECEIPTS = "CARDS_OR_RECEIPTS";
    public static final int RESULT_FILTER = 110;

    private Context context;

    private Menu menu;
    private MenuItem favItem;
    private FloatingActionMenu fabMenu;

    private EditText editFromDate, editToDate;

    private ListView listView;
    private CardListAdapter cardListAdapter;
    private ReceiptListAdapter receiptListAdapter;

    private SQLiteDatabase db;
    private ReceiptDbHelper mDbHelper;

    private ArrayList<Integer> itemIds;
    private String[] projection;
    private String categoriesTable;
    private String itemTable;
    private String[] tableCols;

    private boolean showReceipts;
    private boolean showFavorites;

    private String chosenCategory, chosenFromDate, chosenToDate;

    private Matcher matcherFrom, matcherTo;
    private Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    private String query;

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        mDbHelper = new ReceiptDbHelper(context);
        setShowReceipts(true);
        itemIds = new ArrayList<>();
        query = null;

        setContentView(R.layout.activity_main_view2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabMenu = (FloatingActionMenu)findViewById(R.id.material_design_android_floating_action_menu);

        FloatingActionButton fabMedia = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item1);
        fabMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
                extras.putString(CAMERA_OR_MEDIA, "media");
                extras.putBoolean(Constants.UPDATE, false);
                intent.putExtras(extras);
                startActivityForResult(intent, Constants.RESULT_PROCESSING);
            }
        });

        FloatingActionButton fabCamera = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item2);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
                extras.putString(CAMERA_OR_MEDIA, "cam");
                extras.putBoolean(Constants.UPDATE, false);
                intent.putExtras(extras);
                startActivityForResult(intent, Constants.RESULT_PROCESSING);
            }
        });

        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
                extras.putString("item_id", Integer.toString(itemIds.get(position)));
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        FixedImageReceiver fixedImageReceiver = new FixedImageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(fixedImageReceiver, intentFilter);

    }

    @Override
    public void onResume(){
        super.onResume();
        populateList(listView, null);
    }

    @Override
    public void onPause(){
        super.onPause();
        fabMenu.close(true);
        showFavorites = false;
        favItem = menu.findItem(R.id.action_fav);
        favItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_FILTER){
            Bundle extras = data.getExtras();
            filterList(extras);
        }else if(resultCode == Constants.RESULT_PROCESSING){
            Bundle extras = data.getExtras();
            if(extras.getBoolean(Constants.RECEIPT_PROCESSING)){
                ProgressBar progressBar = (ProgressBar)findViewById(R.id.processingImagesProgressBar);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.paragons_tab) {
            if(showReceipts == false){
                setShowReceipts(true);
                populateList(listView, null);
                fabMenu.setMenuButtonLabelText("Dodaj paragon");
            }
        } else if (id == R.id.cards_tab) {
            if(showReceipts == true){
                setShowReceipts(false);
                populateList(listView, null);
                fabMenu.setMenuButtonLabelText("Dodaj kartę");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Ustaw i wyświetl zawartość manu w pasku narzędzi.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        this.menu = menu;

        // Ustawienie elementu interfejsu pozwalającego na wyszukiwanie w bazie danych, oraz
        // obsługa kliknięcia.
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(listView, searchView.getQuery().toString());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Gdy widok wyszukiwania jest zwinięty wyświetl odpowiednio wszystkie paragony lub karty.
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                populateList(listView, null);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        // Ustawienie i obsługa przycisku odpowiedzialnego za filtrowanie listy.
        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showFilterPopup();
                return true;
            }
        });

        // Ustawienie i obsługa elementu odpowiedzialnego za przełączanie wyświetlania ulubionych
        // wpisów.
        favItem = menu.findItem(R.id.action_fav);
        favItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){

                String query;

                // Jeśli ulubione obecnie nie wyświetlają się, pokaż ulubione i zmień wygląd ikony.
                if(showFavorites == false) {
                    favItem.setIcon(R.drawable.ic_favorite_white_24dp);
                    showFavorites = true;
                    if (showReceipts == true) {
                        query = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " +
                        ReceiptContract.Receipt.FAVORITED + " = 'yes'";
                        populateList(listView, query);
                    } else {
                        query = "SELECT * FROM " + CardContract.Card.TABLE_NAME + " WHERE " +
                        CardContract.Card.FAVORITED + " = 'yes'";
                        populateList(listView, query);
                    }
                }else{
                    // W przeciwnym wypadku przestań wyświetlać tylko ulubione wpisy i zmień wygląd
                    // ikony.
                    favItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    showFavorites = false;
                    populateList(listView, null);
                }

                return true;
            }
        });
        return true;
    }

    private void setShowReceipts(boolean show){
        if(show){
            showReceipts = true;
            projection = Constants.receiptCategoriesProjection;
            categoriesTable = ReceiptContract.Categories.TABLE_NAME;
            itemTable = ReceiptContract.Receipt.TABLE_NAME;
            tableCols = Constants.receiptTableCols;
        }else{
            showReceipts = false;
            projection = Constants.cardCategoriesProjection;
            categoriesTable = CardContract.Card_Categories.TABLE_NAME;
            itemTable = CardContract.Card.TABLE_NAME;
            tableCols = Constants.cardTableCols;
        }
    }

    public void populateList(ListView lv, String query){
        try{
            db = mDbHelper.getWritableDatabase();
            Cursor c;

            final ListView list = lv;

            if(query != null){
                c = db.rawQuery(query, null);
            }else if(query == null && this.query != null){
                c = db.rawQuery(this.query, null);
            }else{
                c = db.query(
                        true,
                        itemTable,
                        tableCols,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }

            itemIds.clear();

            while (c.moveToNext()){
                itemIds.add(c.getInt(c.getColumnIndex(tableCols[0])));
            }

            c.moveToFirst();

            if(showReceipts){
                receiptListAdapter = new ReceiptListAdapter(
                        context,
                        R.layout.list_item,
                        c,
                        Constants.fromReceiptTable,
                        Constants.toReceiptTable,
                        0
                );
                lv.setAdapter(receiptListAdapter);
            }else{
                cardListAdapter = new CardListAdapter(
                        context,
                        R.layout.list_item,
                        c,
                        Constants.fromCardTable,
                        Constants.toCardTable,
                        0
                );
                lv.setAdapter(cardListAdapter);
            }

        }catch (Exception e){
            //Wyświetlenie komunikatu błędu w wypadku jego wystąpienia
            toast = Toast.makeText(context, R.string.toast_add_new_failure + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void filterList(Bundle filterData){
        boolean reset =
                filterData.getBoolean("Reset");
        chosenCategory =
                filterData.getString("Chosen_Category");
        if(showReceipts){
            chosenFromDate =
                    filterData.getString("Chosen_From_Date");
            chosenToDate =
                    filterData.getString("Chosen_To_Date");
        }

        if(!reset){

            query =
                    "SELECT * FROM "
                    + itemTable
                    + " WHERE ";
            String categoryPart = "";
            String datePart = "";
            boolean filterByCategory = false;
            boolean filterByDate = false;

            if(!chosenCategory.equals("")){
                filterByCategory = true;
                categoryPart =
                        tableCols[2]
                        + " = '"
                        + chosenCategory.toLowerCase()
                        + "'";
            }

            if(showReceipts){
                if(!chosenFromDate.equals("YYYY-MM-DD")) {
                    filterByDate = true;
                    matcherFrom =
                            datePattern.matcher(chosenFromDate);
                    matcherTo =
                            datePattern.matcher(chosenToDate);
                    if (matcherFrom.find()) {
                        if (matcherTo.find()) {
                            datePart =
                                    ReceiptContract.Receipt.DATE +
                                    " >= " + "'"
                                    + chosenFromDate
                                    + "'" + " AND "
                                    + ReceiptContract.Receipt.DATE
                                    + " <= " + "'"
                                    + chosenToDate + "'";
                        } else {
                            datePart =
                                    ReceiptContract.Receipt.DATE
                                    + " >= " + "'"
                                    + chosenFromDate + "'";
                        }
                    }
                }
            }

            if(filterByCategory){
                query = query + categoryPart;
            }

            if(filterByCategory && filterByDate){
                query = query + " AND " + datePart;
            }

            if(!filterByCategory && filterByDate){
                query = query + datePart;
            }

            populateList(listView, query);

        }else{
            query = null;
            populateList(listView, null);
        }
    }

    private void search(ListView lv, String query){

        String dbQuery;

        if(showReceipts){
            dbQuery =
                    "SELECT * FROM " +
                            ReceiptContract.Receipt.TABLE_NAME +
                            " WHERE " +
                            ReceiptContract.Receipt.NAME +
                            " LIKE '" + query +
                            "' OR " + ReceiptContract.Receipt.CONTENT +
                            " LIKE '%" + query + "%'" +
                            " OR " + ReceiptContract.Receipt.DESCRIPTION +
                            " LIKE '%" + query + "%'";
        }else{
            dbQuery =
                    "SELECT * FROM " +
                            CardContract.Card.TABLE_NAME +
                            " WHERE " +
                            CardContract.Card.NAME +
                            " LIKE '%" + query + "%'" +
                            " OR " + ReceiptContract.Receipt.DESCRIPTION +
                            " LIKE '%" + query + "%'";
        }

        populateList(lv, dbQuery);

    }

    public void showFilterPopup(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.filter_popup, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        editFromDate = (EditText)popupView.findViewById(R.id.editFromDate);
        editFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arg = new Bundle();
                arg.putString("Field", "from");
                newFragment.setArguments(arg);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        editToDate = (EditText)popupView.findViewById(R.id.editToDate);
        editToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                Bundle arg = new Bundle();
                arg.putString("Field", "to");
                newFragment.setArguments(arg);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        final Spinner spinner = (Spinner)popupView.findViewById(R.id.categorySpinner);

        Button filterButton =(Button)popupView.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                Cursor cursor = (Cursor)spinner.getSelectedItem();
                chosenCategory = cursor.getString(cursor.getColumnIndex(projection[1]));
                extras.putString("Chosen_Category", chosenCategory);
                if(showReceipts == true){
                    extras.putString("Chosen_From_Date", editFromDate.getText().toString());
                    extras.putString("Chosen_To_Date", editToDate.getText().toString());
                }
                extras.putBoolean("Reset", false);
                filterList(extras);
                popupWindow.dismiss();
            }
        });

        Button resetButton = (Button)popupView.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("Chosen_Category", "");
                extras.putBoolean("Reset", true);
                filterList(extras);
                popupWindow.dismiss();
            }
        });


        TextView dateText = (TextView)popupView.findViewById(R.id.dateText);
        LinearLayout dateLayout = (LinearLayout)popupView.findViewById(R.id.dateLayout);


        if(!showReceipts) {
            dateText.setVisibility(View.GONE);
            dateLayout.setVisibility(View.GONE);
        }

        try{
            db = mDbHelper.getReadableDatabase();

            Cursor c = db.query(true, categoriesTable, projection, null, null, null, null, null, null);

            String[] from = new String[]{
                    projection[1]
            };

            int[] to = new int[]{
                    R.id.categoryName
            };

            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.category_spinner_item, c, from, to, 0);
            spinner.setAdapter(simpleCursorAdapter);
        }finally {
            db.close();
        }
    }

    public void dateSet(String date, String field) {

        if(field.equals("to")){
            editToDate.setText(date);
        }else{
            editFromDate.setText(date);
        }

    }

    @Override
    protected void onDestroy(){
        db.close();
        super.onDestroy();
    }

    private class FixedImageReceiver extends BroadcastReceiver{
        private FixedImageReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent) {
            populateList(listView, null);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.processingImagesProgressBar);
            progressBar.setVisibility(View.GONE);
        }
    }
}
