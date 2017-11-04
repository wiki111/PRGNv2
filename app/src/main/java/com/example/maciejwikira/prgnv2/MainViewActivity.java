package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
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


public class MainViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";
    public static final String CARDS_OR_PARAGONS = "CARDS_OR_PARAGONS";
    public static final int RESULT_FILTER = 110;

    private ListView listView;
    private ParagonFunctions paragonFunctions;
    private CardFunctions cardFunctions;
    private Context context;
    private Menu menu;
    private MenuItem favItem;
    private boolean showFavorites;
    private boolean showParagons;
    private FloatingActionMenu fabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        paragonFunctions = new ParagonFunctions(context);
        cardFunctions = new CardFunctions(context);

        showParagons = true;

        setContentView(R.layout.activity_main_view2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabMenu = (FloatingActionMenu)findViewById(R.id.material_design_android_floating_action_menu);

        FloatingActionButton fabMedia = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item1);
        fabMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), NewParagonActivity.class);
            Bundle extras = new Bundle();
            extras.putString(CARDS_OR_PARAGONS, Boolean.toString(showParagons));
            extras.putString(CAMERA_OR_MEDIA, "media");
            intent.putExtras(extras);
            startActivity(intent);
            }
        });

        FloatingActionButton fabCamera = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item2);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), NewParagonActivity.class);
            Bundle extras = new Bundle();
            extras.putString(CARDS_OR_PARAGONS, Boolean.toString(showParagons));
            extras.putString(CAMERA_OR_MEDIA, "cam");
            intent.putExtras(extras);
            startActivity(intent);
            }
        });

        listView = (ListView)findViewById(R.id.listView);
        showParagonsList(listView);
    }

    private void showParagonsList(ListView lv){
        showParagons = true;
        paragonFunctions.populateList(lv, null);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_PARAGONS, showParagons);
                extras.putString("item_id", Integer.toString(paragonFunctions.getParagonsArray().get(position).getDbId()));
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    private void showCardsList(ListView lv){
        showParagons = false;
        cardFunctions.populateList(lv, null);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_PARAGONS, showParagons);
                extras.putString("item_id", Integer.toString(cardFunctions.getItemIds().get(position)));
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(showParagons == true){
            if(paragonFunctions.getResetFilters()){
                paragonFunctions.populateList(listView, null);
            }else{
                paragonFunctions.populateList(listView, paragonFunctions.getQuery());
            }
        }else{
            if(cardFunctions.getResetFilters()){
                cardFunctions.populateList(listView, null);
            }else {
                cardFunctions.populateList(listView, cardFunctions.getQuery());
            }

        }

    }

    @Override
    public void onPause(){
        super.onPause();
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
            if(showParagons == true)
                paragonFunctions.filterList(extras);
            else
                cardFunctions.filterList(extras);
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.paragons_tab) {
            if(showParagons == false){
                showParagonsList(listView);
                fabMenu.setMenuButtonLabelText("Dodaj paragon");
            }
        } else if (id == R.id.cards_tab) {
            if(showParagons == true){
                showCardsList(listView);
                fabMenu.setMenuButtonLabelText("Dodaj kartę");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        this.menu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final MySearchView searchView = (MySearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(showParagons == true){
                    paragonFunctions.search(listView, searchView.getQuery().toString());
                }else{
                    cardFunctions.search(listView, searchView.getQuery().toString());
                }

                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewCollapsedEventListener(new MySearchView.OnSearchViewCollapsedEventListener() {
            @Override
            public void onSearchViewCollapsed() {
                if(showParagons == true)
                    paragonFunctions.populateList(listView, null);
                else
                    cardFunctions.populateList(listView, null);
            }
        });

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(context, FilterActivity.class);
                Bundle extras = new Bundle();
                extras.putBoolean(CARDS_OR_PARAGONS, showParagons);
                intent.putExtras(extras);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        favItem = menu.findItem(R.id.action_fav);
        favItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){

                String query;

                if(showFavorites == false) {
                    favItem.setIcon(R.drawable.ic_favorite_white_24dp);
                    showFavorites = true;
                    if (showParagons == true) {
                        query = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " +
                        ParagonContract.Paragon.FAVORITED + " = 'yes'";
                        paragonFunctions.populateList(listView, query);
                    } else {
                        query = "SELECT * FROM " + CardContract.Card.TABLE_NAME + " WHERE " +
                        CardContract.Card.FAVORITED + " = 'yes'";
                        cardFunctions.populateList(listView, query);

                    }
                }else{
                    favItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    showFavorites = false;
                    if (showParagons == true) {
                        paragonFunctions.populateList(listView, null);
                    } else {
                        cardFunctions.populateList(listView, null);
                    }
                }

                return false;
            }
        });
        return true;
    }


}
