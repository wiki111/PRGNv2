package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/*
    Aktywność odpowiada za wyświetlanie głównego ekranu aplikacji.
    Wyświetla listę paragonów lub kart, zależnie od wyboru użytkownika.
    Posiada szufladę nawigacyjną, która pozwala na wybór pomiędzy interfejsem
    kart i paragonów. Poza tym zawiera przycisk, który pozwala na dodanie nowego
    paragonu bądź karty.
 */
public class MainViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Definicje stałych kluczy używanych przy przekazywaniu lub odbieraniu danych z innych
    // aktywności

    // Klucz wartości decydującej o wybraniu zdjęcia z pamięci lub zrobienia nowego
    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";

    // Klucz wartości oznaczającej jakie dane są uwzględniane - paragony lub karty lojalnościowe
    public static final String CARDS_OR_RECEIPTS = "CARDS_OR_RECEIPTS";

    // Klucz identyfikujący kod powrotu z aktywności filtrowania elementów listy
    public static final int RESULT_FILTER = 110;

    // widok listy na której wyświetlane są dane
    private ListView listView;

    // Deklaracja obiektu klasy odpowiedzialnej za obsługę danych paragonów
    private ReceiptFunctions receiptFunctions;

    // Deklaracja obiektu klasy odpowiedzialnej za obsługę danych kart lojalnościowych
    private CardFunctions cardFunctions;

    // Deklaracja zmiennej przechowującej aktualny kontekst aplikacji
    private Context context;

    // Deklaracja obiektu menu aplikacji
    private Menu menu;

    // Deklaracja elementu menu w pasku aplikacji odpowiedzialnego za obsługę wyświetlania
    // ulubionych kart bądź paragonów
    private MenuItem favItem;

    // Deklaracja zmiennej logicznej przechowującej informację o wyświetlaniu ulubionych elementów
    private boolean showFavorites;

    // Deklaracja zmiennej logicznej przechowującej informację trybie działania aplikacji -
    // tryb paragonów lub kart
    private boolean showReceipts;

    // Deklaracja obiektu klasy rozwijanego menu dodawania nowego wpisu
    private FloatingActionMenu fabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        // Utworzenie instancji klas obsługujących paragony i karty
        receiptFunctions = new ReceiptFunctions(context);
        cardFunctions = new CardFunctions(context);

        // Domyślnie aktywność działa w trybie paragonów
        showReceipts = true;

        // Ustawienie widoku aktywności
        setContentView(R.layout.activity_main_view2);

        // Deklaracja i ustawienie paska narzędzi
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Deklaracja i ustawienie szuflady nawigacyjnej, oraz obsługa jej otwierania i zamykania
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Ustawienie nasłuchiwania na wydarzenia w szufladzie nawigacyjnej - wybór opcji
        // przez użytkownika
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Zapisanie referencji do przycisku menu dodawania nowego wpisu
        fabMenu = (FloatingActionMenu)findViewById(R.id.material_design_android_floating_action_menu);

        // Obsługa kliknięcia na przycisk odpowiadający za dodanie nowego wpisu przez wybranie zdjęcia
        // z pamięci urządzenia. Uruchamiana jest aktywność dodawania nowego rekordu z odpowiednimi
        // parametrami w zależności od wyboru użytkownika.
        FloatingActionButton fabMedia = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item1);
        fabMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
            extras.putString(CAMERA_OR_MEDIA, "media");
            intent.putExtras(extras);
            startActivity(intent);
            }
        });

        // Obsługa kliknięcia na przycisk odpowiadający za dodanie nowego wpisu przez zrobienie
        // nowego zdjęcia. Uruchamiana jest aktywność dodawania nowego rekordu z odpowiednimi
        // parametrami w zależności od wyboru użytkownika.
        FloatingActionButton fabCamera = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item2);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), NewRecordActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
            extras.putString(CAMERA_OR_MEDIA, "cam");
            intent.putExtras(extras);
            startActivity(intent);
            }
        });

        // Zapisanie uchwytu do elementu interfejsu który jest odpowiedzialny za wyświetlanie listy
        // wpisów
        listView = (ListView)findViewById(R.id.listView);

        // Wyświetlenie listy wpisów
        showReceiptsList(listView);
    }


    /*
        Metoda odpowiada za wyświetlenie listy wpisów dotyczących paragonów, oraz za ustawienie
        obiektu nasłuchującego na zdarzenia {Listener) który umożliwia obsługę kliknięcia
        (tapnięcia) na element listy.
     */
    private void showReceiptsList(ListView lv){

        // Jeśli wyświetlana jest lista paragonów, to aplikacja jest w trybie paragonów.
        showReceipts = true;

        // Ustawienie obiektu nasłuchującego na zdarzenia. Jeśli nastąpi kliknięcie na element listy
        // uruchamiana jest z odpowiednimi parametrami aktywność odpowiedzialna za pokazanie
        // szczegółów dotyczących wpisu.
        receiptFunctions.populateList(lv, null);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
                extras.putString("item_id", Integer.toString(receiptFunctions.getItemIds().get(position)));
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    // Funkcja analogiczna do funkcji showReceiptsList, tyle, że dotycząca kart, a nie paragonów.
    private void showCardsList(ListView lv){
        showReceipts = false;
        cardFunctions.populateList(lv, null);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
                extras.putString("item_id", Integer.toString(cardFunctions.getItemIds().get(position)));
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        // Jeśli aplikacja znajduje się w trybie paragonów wyświetl listę paragonów. W przeciwnym
        // razie wyświetl listę kart.
        if(showReceipts == true){
            // Jeśli lista nie ma być filtrowana po prostu wyświetl wszystkie rekordy z bazy danych.
            // W przeciwnym wypadku przekaż do funkcji wyświetlającej listę odpowiednie zapytanie do
            // bazy danych.
            if(receiptFunctions.getResetFilters()){
                receiptFunctions.populateList(listView, null);
            }else{
                receiptFunctions.populateList(listView, receiptFunctions.getQuery());
            }
        }else{
                cardFunctions.populateList(listView, null);
        }

    }

    @Override
    public void onPause(){
        super.onPause();

        // Zwiń menu dodawania nowego wpisu.
        fabMenu.close(true);

        // Nie wyświetlaj ulubionych.
        showFavorites = false;

        // Ustawienie ikony oznaczającej, że wyświetlanie ulubionych elementów nie jest aktywne.
        favItem = menu.findItem(R.id.action_fav);
        favItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
    }

    @Override
    public void onBackPressed() {

        // Jeśli szuflada nawigacyjna jest otwarta, zamknij ją i zakończ akcję.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // W przeciwnym razie wywołaj zwyczajną akcję.
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Gdy nastąpi powrót z aktywności która pozwala na wprowadzenie parametrów, według których
        // ma być filtrowana lista wyników (FilterActivity) ...
        if(resultCode == RESULT_FILTER){
            Bundle extras = data.getExtras();
            // Jeśli aplikacja jest w trybie paragonów przekaż dane zwrócone przez FilterActivity
            // do funkcji filtrującej listę paragonów.
            if(showReceipts == true)
                receiptFunctions.filterList(extras);
            else
                // W przeciwnym razie przekaż dane do funkcji filtrującej listę kart.
                cardFunctions.filterList(extras);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Jeśli wybrana została opcja paragonów przełącz aplikację w tryb paragonów, wyświetl ich
        // listę i zmień etykietę menu dodawania wpisów. Postępuj analogicznie w przypadku gdy
        // została wybrana opcja kart lojalnościowych.
        if (id == R.id.paragons_tab) {
            if(showReceipts == false){
                showReceiptsList(listView);
                fabMenu.setMenuButtonLabelText("Dodaj paragon");
            }
        } else if (id == R.id.cards_tab) {
            if(showReceipts == true){
                showCardsList(listView);
                fabMenu.setMenuButtonLabelText("Dodaj kartę");
            }
        }

        // Zamknij szufladę.
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
                // Gdy użytkownik wyśle zapytanie wywoływana jest odpowiednia funkcja wyszukująca,
                // która jako parametr przyjmuje zapytanie użytkownika, oraz uchwyt do elementu
                // interfejsu, który jest odpowiedzialny za wyświetlenie listy wyników.

                    if(showReceipts == true){
                        receiptFunctions.search(listView, searchView.getQuery().toString());
                    }else{
                        cardFunctions.search(listView, searchView.getQuery().toString());
                    }
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
                if(showReceipts == true)
                    receiptFunctions.populateList(listView, null);
                else
                    cardFunctions.populateList(listView, null);

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
                // Uruchom aktywność filtrowania (FilterActivity) i przekaż do niej informację
                // o trybie działania aplikacji.
                Intent intent = new Intent(context, FilterActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_RECEIPTS, showReceipts);
                intent.putExtras(extras);
                startActivityForResult(intent, 0);
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
                    // Jeśli aplikacja jest w trybie paragonów wyświetl listę ulubionych paragonów.
                    // Analogicznie, jeśli aplikacja jest w trybie kart lojalnościowych.
                    if (showReceipts == true) {
                        query = "SELECT * FROM " + ReceiptContract.Receipt.TABLE_NAME + " WHERE " +
                        ReceiptContract.Receipt.FAVORITED + " = 'yes'";
                        receiptFunctions.populateList(listView, query);
                    } else {
                        query = "SELECT * FROM " + CardContract.Card.TABLE_NAME + " WHERE " +
                        CardContract.Card.FAVORITED + " = 'yes'";
                        cardFunctions.populateList(listView, query);
                    }
                }else{
                    // W przeciwnym wypadku przestań wyświetlać tylko ulubione wpisy i zmień wygląd
                    // ikony.
                    favItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    showFavorites = false;
                    if (showReceipts == true) {
                        receiptFunctions.populateList(listView, null);
                    } else {
                        cardFunctions.populateList(listView, null);
                    }
                }

                return true;
            }
        });
        return true;
    }

}
