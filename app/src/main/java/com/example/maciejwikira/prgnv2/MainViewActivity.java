package com.example.maciejwikira.prgnv2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.clans.fab.FloatingActionButton;

public class MainViewActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";
    private ParagonsFragment paragonsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (findViewById(R.id.fragmentView) != null) {
            if(savedInstanceState != null){
                return;
            }

            paragonsFragment = new ParagonsFragment();
            getFragmentManager().beginTransaction().add(R.id.fragmentView, paragonsFragment,
                    "PARAGONS_LIST").commit();
        }

        FloatingActionButton fabMedia = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item1);
        fabMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paragonsFragment = (ParagonsFragment)getFragmentManager().findFragmentByTag("PARAGONS_LIST");
                if(paragonsFragment != null){
                    Intent intent = new Intent(getApplicationContext(), NewParagonActivity.class);
                    intent.putExtra(CAMERA_OR_MEDIA, "media");
                    startActivity(intent);
                }
            }
        });

        FloatingActionButton fabCamera = (FloatingActionButton)findViewById(R.id.material_design_floating_action_menu_item2);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paragonsFragment = (ParagonsFragment)getFragmentManager().findFragmentByTag("PARAGONS_LIST");
                if(paragonsFragment != null) {
                    Intent intent = new Intent(getApplicationContext(), NewParagonActivity.class);
                    intent.putExtra(CAMERA_OR_MEDIA, "cam");
                    startActivity(intent);
                }
            }
        });
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.paragons_tab) {
            ParagonsFragment paragonsFragment = (ParagonsFragment)getFragmentManager().findFragmentByTag("PARAGONS_LIST");
            if(paragonsFragment == null){
                paragonsFragment = new ParagonsFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragmentView, paragonsFragment,
                        "PARAGONS_LIST").commit();
            }
        } else if (id == R.id.cards_tab) {
            CardsFragment cardsFragment = (CardsFragment)getFragmentManager().findFragmentByTag("CARDS_LIST");
            if(cardsFragment == null){
                cardsFragment = new CardsFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragmentView, cardsFragment,
                        "CARDS_LIST").commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
