package dynamic.android.locations.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import dynamic.android.locations.LocationsListActivity;
import dynamic.android.locations.model.LocationModel;


/**
 * Created by Sarveshwar Reddy on 30/04/2018.
 */
public class LocationsDB extends DBContext {

    // home Table Columns names
    static final String KEY_ID = "id";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String LOCATION = "location";

    Context context;

    public LocationsDB(Context cntxt) {
        super(cntxt);
        context = cntxt;
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public void insertUser(LocationModel reg) {

        ContentValues values = new ContentValues();

        LocationsDB locationsDB = new LocationsDB(context);
        ArrayList<LocationModel> home_list = locationsDB.getList();

        values.put(KEY_ID, home_list.size() + 1);
        values.put(LATITUDE, reg.getLatitude());
        values.put(LONGITUDE, reg.getLongitude());
        values.put(LOCATION, reg.getLocation());

        // Inserting Row
        database.insert(DataBaseHelper.TABLE_USER, null, values);
    }


    public LocationModel getHome() {
        String selectQuery = "SELECT * FROM " + DataBaseHelper.TABLE_USER;

        Cursor cursor = database.rawQuery(selectQuery, null);

        LocationModel home = new LocationModel();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            home.setLatitude(cursor.getString(1));
            home.setLongitude(cursor.getString(2));
            home.setLocation(cursor.getString(3));
        }
        if (cursor != null)
            cursor.close();
        return home;
    }

    public ArrayList<LocationModel> getList() {

        ArrayList<LocationModel> home_list = new ArrayList<LocationModel>();
        try {


            // Select All Query
            String selectQuery = "SELECT * FROM " + DataBaseHelper.TABLE_USER;
            System.out.println(selectQuery);
            // SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(selectQuery, null);

            // Looping through all rows and adding to list
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {

                    LocationModel home = new LocationModel();

                   // home.setId(cursor.getString(0));
                    home.setLatitude(cursor.getString(1));
                    home.setLongitude(cursor.getString(2));
                    home.setLocation(cursor.getString(3));

                    // Adding contact to list
                    home_list.add(home);

                } while (cursor.moveToNext());
            }

            // return contact list
            cursor.close();


            return home_list;

        } catch (Exception e) {

            // TODO: handle exception
            Log.e("Stock options list", "" + e.getMessage());
        }
        return home_list;
    }

    public boolean deleteLocation(String id) {
        return database.delete(DataBaseHelper.TABLE_USER, KEY_ID + "=" + id, null) > 0;
    }


}
