package dynamic.android.locations.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sarveshwar on 30/04/18.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    // Database Tables
    public static final String DATABASE_NAME = "DynamicLocations";
    private static final int DATABASE_VERSION = 1;
    // home table name
    static final String TABLE_USER = "locations";
    private static String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + LocationsDB.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + LocationsDB.LATITUDE + " TEXT," + LocationsDB.LONGITUDE + " TEXT," + LocationsDB.LOCATION + " TEXT);";


    //INTEGER PRIMARY KEY
    private static DataBaseHelper instance;

    static synchronized DataBaseHelper getHelper(Context context) {
        if (instance == null)
            instance = new DataBaseHelper(context);
        return instance;
    }

    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //   SQLiteDatabase dbs = getWritableDatabase();
        db.execSQL(CREATE_LOCATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
