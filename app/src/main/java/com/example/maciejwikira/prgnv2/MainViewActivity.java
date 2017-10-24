package com.example.maciejwikira.prgnv2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

public class MainViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";

    private ListView paragonsListView;
    private ArrayList<Paragon> paragonsArray;
    private SQLiteDatabase db;
    private ParagonDbHelper mDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fabMedia = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item1);
        fabMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewPrgnActivity.class);
                intent.putExtra(CAMERA_OR_MEDIA, "media");
                startActivity(intent);
            }
        });

        FloatingActionButton fabCamera = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item2);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewPrgnActivity.class);
                intent.putExtra(CAMERA_OR_MEDIA, "cam");
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        paragonsArray = new ArrayList<Paragon>();
        paragonsListView = (ListView)findViewById(R.id.paragonsListView);
        populateList(paragonsListView);

    }

    @Override
    protected void onResume(){
        super.onResume();
        populateList(paragonsListView);
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
                search(searchView.getQuery().toString());
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
                populateList(paragonsListView);
            }
        });

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
                startActivity(intent);
                return false;
            }
        });

        return true;
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

    private void populateList(ListView lv){

        //get reference do the database
        mDbHelper = new ParagonDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        //declare what to get from db
        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        //get cursor
        Cursor c = db.query(true, ParagonContract.Paragon.TABLE_NAME, cols,null, null, null, null, null, null);
        try{
            paragonsArray.clear();

            //fill array with paragon objects created from database data
            while(c.moveToNext()){

                paragonsArray.add(new Paragon(
                        c.getInt(c.getColumnIndex("_id")),
                        c.getString(c.getColumnIndex("name")),
                        c.getString(c.getColumnIndex("category")),
                        c.getString(c.getColumnIndex("value")),
                        c.getString(c.getColumnIndex("date")),
                        c.getString(c.getColumnIndex("img")),
                        c.getString(c.getColumnIndex("text"))
                ));

            }

            //finally get and set adapter
            ParagonListAdapter paragonListAdapter = new ParagonListAdapter(this.getApplicationContext(), R.layout.paragon_list_item, paragonsArray);
            lv.setAdapter(paragonListAdapter);
        }finally {
            c.close();
            db.close();
        }

    }

    private void search(String query){

        mDbHelper = new ParagonDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        String[] cols = new String[]{
                "_id",
                "name",
                "category",
                "value",
                "date",
                "img",
                "text"
        };

        String itemContent;
        String itemNameContent;

        Cursor c;
        int text_index;
        int title_index;
        ArrayList<Paragon> searchResults = new ArrayList<Paragon>();
        Matcher matchName, matchContent;
        Pattern pattern = Pattern.compile(query.toLowerCase());

        c = db.query(true, ParagonContract.Paragon.TABLE_NAME, cols,null, null, null, null, null, null);

        try {
            while (c.moveToNext()) {

                title_index = c.getColumnIndex(ParagonContract.Paragon.NAME);
                itemNameContent = c.getString(title_index).toLowerCase();
                matchName = pattern.matcher(itemNameContent);
                text_index = c.getColumnIndex(ParagonContract.Paragon.CONTENT);
                itemContent = c.getString(text_index);
                itemContent.toLowerCase();
                matchContent = pattern.matcher(itemContent);
                if(matchName.find() || matchContent.find()){
                    searchResults.add(new Paragon(
                            c.getInt(c.getColumnIndex("_id")),
                            c.getString(c.getColumnIndex("name")),
                            c.getString(c.getColumnIndex("category")),
                            c.getString(c.getColumnIndex("value")),
                            c.getString(c.getColumnIndex("date")),
                            c.getString(c.getColumnIndex("img")),
                            c.getString(c.getColumnIndex("text"))
                    ));
                }
            }
        } finally {
            c.close();
            db.close();
        }

        ParagonListAdapter paragonListAdapter = new ParagonListAdapter(this.getApplicationContext(), R.layout.paragon_list_item, searchResults);
        paragonsListView.setAdapter(paragonListAdapter);

    }
}
