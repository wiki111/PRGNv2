package com.example.maciejwikira.prgnv2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;

public class MainViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";

    private ListView paragonsListView;
    private boolean showFavorites;

    private ParagonFunctions paragonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_view2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showFavorites = false;

        FloatingActionButton fabMedia = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item1);
        fabMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewParagonActivity.class);
                intent.putExtra(CAMERA_OR_MEDIA, "media");
                startActivity(intent);
            }
        });

        FloatingActionButton fabCamera = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item2);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewParagonActivity.class);
                intent.putExtra(CAMERA_OR_MEDIA, "cam");
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        paragonsListView = (ListView)findViewById(R.id.paragonsListView);
        paragonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
                intent.putExtra("item_id", Integer.toString(paragonFunctions.getParagonsArray().get(position).getDbId()));
                startActivity(intent);
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        paragonFunctions = new ParagonFunctions(getApplicationContext());
        paragonFunctions.populateList(paragonsListView, null);

    }

    @Override
    protected void onResume(){
        super.onResume();

        if(paragonFunctions.getResetFilters()){
            paragonFunctions.populateList(paragonsListView, null);
        }else{
            paragonFunctions.populateList(paragonsListView, paragonFunctions.getQuery());
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_view, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final MySearchView searchView = (MySearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                paragonFunctions.search(paragonsListView, searchView.getQuery().toString());
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
                paragonFunctions.populateList(paragonsListView, null);
            }
        });

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        MenuItem favItem = menu.findItem(R.id.action_fav);
        favItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){

                if(showFavorites == false){
                    showFavorites = true;
                }

                return false;
            }
        });

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Bundle extras = data.getExtras();

            paragonFunctions.filterList(extras);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_filter){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
