package com.tjweiten.materialtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TJ on 3/27/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "packageManager";
    private static final String TABLE_PACKAGES = "packages";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TRACKING = "tracking";
    private static final String KEY_CARRIER = "carrier_id";
    private static final String KEY_XML = "xml_response";
    private static final String KEY_ACTIVE = "active";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PACKAGES_TABLE = "CREATE TABLE " + TABLE_PACKAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT," + KEY_TRACKING + " TEXT," + KEY_CARRIER + " INTEGER," + KEY_XML + " TEXT," + KEY_ACTIVE + " INTEGER" + ")";
        db.execSQL(CREATE_PACKAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PACKAGES);
        onCreate(db);
    }

    /* package is restricted word, btw */
    public void addPackage(Package parcel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, parcel.getName());
        values.put(KEY_TRACKING, parcel.getTracking());
        values.put(KEY_CARRIER, parcel.getCarrierID());
        values.put(KEY_XML, parcel.getXML());
        values.put(KEY_ACTIVE, parcel.getActive());

        db.insert(TABLE_PACKAGES, null, values);
        db.close();
    }

    public Package getPackage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PACKAGES,
                new String[] { KEY_ID, KEY_NAME, KEY_TRACKING, KEY_CARRIER, KEY_XML, KEY_ACTIVE },
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null
        );

        if(cursor != null)
            cursor.moveToFirst();

        Package parcel = new Package(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2),
                Integer.parseInt(cursor.getString(3)),
                cursor.getString(4),
                Integer.parseInt(cursor.getString(5))
        );

        return parcel;
    }

    public List<Package> getAllPackages() {
        List<Package> parcelList = new ArrayList<Package>();

        String selectQuery = "SELECT  * FROM " + TABLE_PACKAGES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do {
                Package parcel = new Package();
                parcel.setID(Integer.parseInt(cursor.getString(0)));
                parcel.setName(cursor.getString(1));
                parcel.setTracking(cursor.getString(2));
                parcel.setCarrierID(Integer.parseInt(cursor.getString(3)));
                parcel.setXML(cursor.getString(4));
                parcel.setActive(Integer.parseInt(cursor.getString(5)));
                parcelList.add(parcel);
            } while(cursor.moveToNext());
        }

        return parcelList;
    }

    public int getPackagesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PACKAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    public int updatePackage(Package parcel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, parcel.getName());
        values.put(KEY_TRACKING, parcel.getTracking());
        values.put(KEY_CARRIER, parcel.getCarrierID());
        values.put(KEY_XML, parcel.getXML());
        values.put(KEY_ACTIVE, parcel.getActive());

        return db.update(TABLE_PACKAGES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(parcel.getID()) });
    }

    public void deletePackage(Package parcel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PACKAGES, KEY_ID + " = ?",
                new String[] { String.valueOf(parcel.getID()) });
        db.close();
    }

}
