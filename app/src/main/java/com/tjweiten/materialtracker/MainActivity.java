package com.tjweiten.materialtracker;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.dexafree.materialList.cards.BigImageCard;
import com.dexafree.materialList.cards.SmallImageCard;
import com.dexafree.materialList.controller.OnDismissCallback;
import com.dexafree.materialList.model.Card;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class MainActivity extends ActionBarActivity implements SwipyRefreshLayout.OnRefreshListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Drawer.Result mDrawer;
    private Toolbar mToolbar;
    private SwipyRefreshLayout mSwipyRefreshLayout;
    public DatabaseHandler db = new DatabaseHandler(this);
    private List<Package> parcels = null;
    private ArrayList<Card> card_list = new ArrayList<Card>();

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
        mRecyclerView.setOnDismissCallback(new OnDismissCallback() {
            @Override
            public void onDismiss(Card card, int position) {
                /* try to set card to archived */
            }
        });
        mRecyclerView.setItemAnimator(new SlideInRightAnimator());

        /* create the cards */
        parcels = db.getAllPackages();
        if(parcels.isEmpty()) {
            SmallImageCard card = new SmallImageCard(this);
            card.setTitle("Welcome to Material Tracker!");
            card.setDescription("Welcome to the Material Tracker parcel tracking application. You may add a new parcel by hitting the + button down bellow. " +
                    "Swipe a package left or right to archive it. You will receive notifications whenever a status update to your package is detected. ");
            card.setDismissible(false);
            card.setBackgroundColor(getResources().getColor(R.color.accent));
            card.setTitleColor(getResources().getColor(R.color.icons));
            card.setDescriptionColor(getResources().getColor(R.color.icons));
            mRecyclerView.add(card);
        } else {
            for(Package pn : parcels) {
                int active = pn.getActive();
                if (active == 1) {
                    BigImageCard card = new BigImageCard(this);
                    card.setTitle(pn.getName());
                    card.setDescription(pn.getXML());
                    card.setDrawable("https://maps.googleapis.com/maps/api/staticmap?size=900x300&path=weight:3%7Ccolor:orange%7Cenc:_fisIp~u%7CU}%7Ca@pytA_~b@hhCyhS~hResU%7C%7Cx@oig@rwg@amUfbjA}f[roaAynd@%7CvXxiAt{ZwdUfbjAewYrqGchH~vXkqnAria@c_o@inc@k{g@i`]o%7CF}vXaj\\h`]ovs@?yi_@rcAgtO%7Cj_AyaJren@nzQrst@zuYh`]v%7CGbldEuzd@%7C%7Cx@spD%7CtrAzwP%7Cd_@yiB~vXmlWhdPez\\_{Km_`@~re@ew^rcAeu_@zhyByjPrst@ttGren@aeNhoFemKrvdAuvVidPwbVr~j@or@f_z@ftHr{ZlwBrvdAmtHrmT{rOt{Zz}E%7Cc%7C@o%7CLpn~AgfRpxqBfoVz_iAocAhrVjr@rh~@jzKhjp@``NrfQpcHrb^k%7CDh_z@nwB%7Ckb@a{R%7Cyh@uyZ%7CllByuZpzw@wbd@rh~@%7C%7CFhqs@teTztrAupHhyY}t]huf@e%7CFria@o}GfezAkdW%7C}[ocMt_Neq@ren@e~Ika@pgE%7Ci%7CAfiQ%7C`l@uoJrvdAgq@fppAsjGhg`@%7ChQpg{Ai_V%7C%7Cx@mkHhyYsdP%7CxeA~gF%7C}[mv`@t_NitSfjp@c}Mhg`@sbChyYq}e@rwg@atFff}@ghN~zKybk@fl}A}cPftcAite@tmT__Lha@u~DrfQi}MhkSqyWivIumCria@ciO_tHifm@fl}A{rc@fbjAqvg@rrqAcjCf%7Ci@mqJtb^s%7C@fbjA{wDfs`BmvEfqs@umWt_Nwn^pen@qiBr`xAcvMr{Zidg@dtjDkbM%7Cd_@");
                    card.setTitleColor(getResources().getColor(R.color.primary_text));
                    card.setDescriptionColor(getResources().getColor(R.color.primary_text));
                    card.setDismissible(true);
                    card_list.add(card);
                    mRecyclerView.add(card);
                }
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

        /* create an alarm */
        Context context = this.getApplicationContext();
        setRecurringAlarm(context);

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

    @Override
    protected void onResume() {
        super.onResume();
        Intent downloader = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(getApplicationContext(),
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if(DEBUG_MODE) Log.d("AlarmManager", "Attempting creation of alarm.");
        alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, recurringDownload);
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

        /* piss poor way of updating MainActivity */
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);

    }

    public void refreshDatabase() throws IOException {

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
            String old_xml = parcel[0].getXML();

            try {
                /* USPS */
                if(courier == 0) {
                        parcel[0].setXML(loadUSPSxml(parcel[0]));
                }

                /* FedEx */
                else if(courier == 1) {
                    Toast.makeText(getApplicationContext(), "FedEx Not Supported", Toast.LENGTH_SHORT).show();
                }

                /* UPS */
                else if(courier == 2) {
                    Toast.makeText(getApplicationContext(), "UPS Not Supported", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            /* new xml is different from old xml */
            if(!old_xml.equals(parcel[0].getXML())) {

                if(DEBUG_MODE) Log.d("UPDATE", "New XML Update");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Tracking Update on " + parcel[0].getName())
                                .setContentText("Activity on " + parcel[0].getTracking() + "!\n\nPlease view the application to see the updates.");
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.build());

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

    private void setRecurringAlarm(Context context) {

        Calendar updateTime = Calendar.getInstance();
        updateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        updateTime.set(Calendar.HOUR_OF_DAY, 11);
        updateTime.set(Calendar.MINUTE, 45);

        Intent downloader = new Intent(context, AlarmReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(context,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if(DEBUG_MODE) Log.d("AlarmManager", "Attempting creation of alarm.");
        alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, recurringDownload);

    }

    public class AlarmReceiver extends BroadcastReceiver {

        private static final String DEBUG_TAG = "AlarmReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DEBUG_TAG, "Recurring alarm; requesting download service.");

            Intent downloader = new Intent(context, MainActivity.class);

            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                // start the download
                try {
                    refreshDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                context.startService(downloader);
            }

            try {
                refreshDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
            context.startService(downloader);

        }

    }

}
