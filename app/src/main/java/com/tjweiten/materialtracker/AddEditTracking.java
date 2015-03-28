package com.tjweiten.materialtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddEditTracking extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private Toolbar mToolbar;

    private int parcel_courier;

    private static final boolean DEBUG_MODE = MainActivity.DEBUG_MODE; /* load debug flag from MainActivity */

    /* entry method to activity */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_edit_tracking);

        /* initiate & edit the toolbar */
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        /* create the company spinner */
        Spinner spinner = (Spinner) findViewById(R.id.company_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddEditTracking.this,
                R.array.trackable_companies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    /* inflate the menu, adding items to the action bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* handle item clicks on the action bar */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        /* create the database handler */
        DatabaseHandler db = new DatabaseHandler(this);

        switch (item.getItemId()) {

            /* user hit cancel button */
            case R.id.edit_cancel:

                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;

            /* user tried to create entry */
            case R.id.edit_accept:

                //Toast.makeText(getApplicationContext(), "Attempting Entry Creation...", Toast.LENGTH_SHORT).show();

                EditText ET_parcel_name = (EditText) findViewById(R.id.parcel_name);
                EditText ET_parcel_tracking_number = (EditText) findViewById(R.id.parcel_tracking_number);

                String parcel_name = ET_parcel_name.getText().toString().trim();
                String parcel_tracking_number = ET_parcel_tracking_number.getText().toString().trim();

                /* if the user left the parcel tracking field empty, error */
                if(parcel_tracking_number.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "TRACKING NUMBER CANNOT BE EMPTY!", Toast.LENGTH_LONG).show();
                    break;
                }

                /* if the user left the parcel name field empty, use the tracking number as the name */
                if(parcel_name.isEmpty()) {
                    parcel_name = parcel_tracking_number;
                }

                /* add the package to the database */
                db.addPackage(new Package(parcel_name, parcel_tracking_number, parcel_courier, "NULL", 1));
                if(DEBUG_MODE) {
                    Toast.makeText(getApplicationContext(), "New Package Created...\nNAME: " + parcel_name + "\n#: " + parcel_tracking_number + "\nCARRIER: " + parcel_courier, Toast.LENGTH_LONG).show();
                }

                /* return to MainActivity */
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        parcel_courier = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

}
