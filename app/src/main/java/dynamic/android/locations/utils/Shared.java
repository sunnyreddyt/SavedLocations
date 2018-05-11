package dynamic.android.locations.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sarveshwar on 30/04/18.
 */
public class Shared extends Application {


    SharedPreferences preference;
    SharedPreferences.Editor editor;
    Context mContext;

    public Shared() {

    }

    public Shared(Context context) {
        // TODO Auto-generated constructor stub
        mContext = context;

        preference = mContext.getSharedPreferences("fileName", Context.MODE_PRIVATE);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
//		preference = mContext.getSharedPreferences("fileName", Context.MODE_PRIVATE);
//		preference = PreferenceManager.getDefaultSharedPreferences(this);
    }


    public String getCurrentLocation() {
        return preference.getString("CurrentLocation", "");
    }

    public void setCurrentLocation(String CurrentLocation) {
        editor = preference.edit();
        editor.putString("CurrentLocation", CurrentLocation);
        editor.commit();
    }

    public String getCustomerLatitude() {

        return preference.getString("CustomerLatitude", "");
    }

    public void setCustomerLatitude(String CustomerLatitude) {
        editor = preference.edit();
        editor.putString("CustomerLatitude", CustomerLatitude);
        editor.commit();
    }

    public String getCustomerLongitude() {

        return preference.getString("CustomerLongitude", "");
    }

    public void setCustomerLongitude(String CustomerLongitude) {
        editor = preference.edit();
        editor.putString("CustomerLongitude", CustomerLongitude);
        editor.commit();
    }

}

