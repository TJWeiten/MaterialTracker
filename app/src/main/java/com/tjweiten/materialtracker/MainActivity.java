package com.tjweiten.materialtracker;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.view.MaterialListView;
import com.melnykov.fab.FloatingActionButton;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;

public class MainActivity extends ActionBarActivity implements SwipyRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Drawer.Result mDrawer;
    private Toolbar mToolbar;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    public DatabaseHandler db = new DatabaseHandler(this);

    public static final boolean DEBUG_MODE = true;

    public static final String USPS_TRACKING_URL = "http://production.shippingapis.com/ShippingAPI.dll?API=TrackV2&XML=";

    protected void onCreate(Bundle savedInstanceState) {

        /* initiate the main activity */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initiate the toolbar */
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* create the drawer and populate it */
        mDrawer = new Drawer()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withToolbar(mToolbar)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.active_deliv).withIcon(FontAwesome.Icon.faw_cubes),
                        new PrimaryDrawerItem().withName(R.string.archived_deliv).withIcon(FontAwesome.Icon.faw_archive),

                        new SectionDrawerItem().withName(R.string.misc_header),

                        new SecondaryDrawerItem().withName(R.string.settings).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.about).withIcon(FontAwesome.Icon.faw_info_circle)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(((Nameable) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        /* create the material list view */
        MaterialListView mRecyclerView = (MaterialListView) findViewById(R.id.recycler_view);

        /* create the cards */
        List<Package> parcels = db.getAllPackages();
        for(Package pn : parcels) {
            int active = pn.getActive();
            if(active == 1) {
                SmallImageCard card = new SmallImageCard(this);
                card.setTitle(pn.getName());
                card.setDescription(pn.getXML());
                card.setDrawable(R.drawable.ic_launcher);
                card.setBackgroundColor(getResources().getColor(R.color.icons));
                card.setTitleColor(getResources().getColor(R.color.accent));
                card.setDescriptionColor(getResources().getColor(R.color.primary_text));
                mRecyclerView.add(card);
            }
        }

        /* create the FAB */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);

        /* the fuck do these do? */
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        /* create the pull to refresh layout */
        mSwipyRefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipyrefreshlayout);
        mSwipyRefreshLayout.setOnRefreshListener(this);

        /* create the database handler */
        DatabaseHandler db = new DatabaseHandler(this);

        /* print to logcat messages about the database if enabled */
        if(DEBUG_MODE) {
            Log.d("DB READ", "Reading all packages in the database...");
            for(Package pn : parcels) {
                String log = "ID: " + pn.getID() +
                        ", Name: " + pn.getName() +
                        ", Tracking: " + pn.getTracking() +
                        ", Carrier: " + pn.getCarrierID() +
                        "\n\nXML: " + pn.getXML();
                Log.d("PACKAGE INFO", log);
                //db.deletePackage(pn);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    public void addEditTracking(View v) {
        Intent intent = new Intent(this, AddEditTracking.class);
        startActivity(intent);
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

        try {
            refreshDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* hide the refresh after 2 sec */
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipyRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }, 2000);

    }

    public void refreshDatabase() throws IOException {

        List<Package> parcels = db.getAllPackages();
        for(Package pn : parcels) {
            int active = pn.getActive();
            if(active == 1)
                new LoadXml().execute(pn);
        }

    }

    private class LoadXml extends AsyncTask<Package, Void, Package> {

        @Override
        protected Package doInBackground(Package... parcel) {

            int courier = parcel[0].getCarrierID();

            try {
                /* USPS */
                if(courier == 0) {
                        parcel[0].setXML(loadUSPSxml(parcel[0]));
                }

                /* FedEx */
                else if(courier == 1) {
                    Toast.makeText(MainActivity.this, "FedEx Not Supported", Toast.LENGTH_SHORT).show();
                }

                /* UPS */
                else if(courier == 2) {
                    Toast.makeText(MainActivity.this, "UPS Not Supported", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return parcel[0];
        }

        @Override
        protected void onPostExecute(Package parcel) {
            db.updatePackage(parcel);
        }

    }

    public String loadUSPSxml(Package parcel) throws IOException {

        /* get url for xml tracking number */
        String tracking_number = parcel.getTracking();
        String appendedURL = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
                "<TrackRequest USERID=\"990UNIVE2782\">" +
                "<TrackID ID=\""+ tracking_number +"\"></TrackID>" +
                "</TrackRequest>";
        String urlString = USPS_TRACKING_URL + Uri.encode(appendedURL);

        if(DEBUG_MODE) Log.d("USPS", "URL: " + urlString);

        /* make the request to the server and get a response */
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String response_str = client.execute(request, responseHandler);

        if(DEBUG_MODE) Log.d("USPS", "Reply: " + response_str);

        return response_str;

    }

}
