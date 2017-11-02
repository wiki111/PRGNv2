package com.example.maciejwikira.prgnv2;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParagonsFragment extends Fragment {

    public static final String CAMERA_OR_MEDIA = "CAMERA_OR_MEDIA";

    private ListView paragonsList;
    private ParagonFunctions paragonFunctions;
    private Menu menu;
    private MenuItem favItem;
    private boolean showFavorites;

    public ParagonsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        setHasOptionsMenu(true);
        showFavorites = false;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paragons, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        paragonsList = (ListView)getView().findViewById(R.id.paragonsList);
        paragonFunctions = new ParagonFunctions(getActivity().getApplicationContext());

        paragonFunctions.populateList(paragonsList, null);

        paragonsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ParagonDetailsActivity.class);
                intent.putExtra("item_id", Integer.toString(paragonFunctions.getParagonsArray().get(position).getDbId()));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(paragonFunctions.getResetFilters()){
            paragonFunctions.populateList(paragonsList, null);
        }else{
            paragonFunctions.populateList(paragonsList, paragonFunctions.getQuery());
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main_view, menu);
        super.onCreateOptionsMenu(menu,inflater);

        this.menu = menu;

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final MySearchView searchView = (MySearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                paragonFunctions.search(paragonsList, searchView.getQuery().toString());
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
                paragonFunctions.populateList(paragonsList, null);
            }
        });

        MenuItem filterItem = menu.findItem(R.id.action_filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(getActivity().getApplicationContext(), FilterParagonsActivity.class);
                startActivityForResult(intent, 0);
                return false;
            }
        });

        favItem = menu.findItem(R.id.action_fav);
        favItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){

                if(showFavorites == false){
                    favItem.setIcon(R.drawable.ic_favorite_white_24dp);
                    String query = "SELECT * FROM " + ParagonContract.Paragon.TABLE_NAME + " WHERE " +
                            ParagonContract.Paragon.FAVORITED + " = 'yes'";
                    paragonFunctions.populateList(paragonsList, query);
                    showFavorites = true;
                }else {
                    favItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    paragonFunctions.populateList(paragonsList, null);
                    showFavorites = false;
                }

                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Bundle extras = data.getExtras();
            paragonFunctions.filterList(extras);
        }
    }

}
