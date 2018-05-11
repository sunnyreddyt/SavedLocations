package dynamic.android.locations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import dynamic.android.locations.database.LocationsDB;
import dynamic.android.locations.model.LocationModel;
import dynamic.android.locations.service.FetchAddressIntentService;
import dynamic.android.locations.utils.ABUtil;
import dynamic.android.locations.utils.AppUtils;
import dynamic.android.locations.utils.Circle;
import dynamic.android.locations.utils.ShapeRipple;
import dynamic.android.locations.utils.Shared;

/**
 * Created by Sarveshwar Reddy on 30/04/18.
 */

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static String TAG = "MAP LOCATION";
    public static Context mContext;
    private LatLng mCenterLatLong;
    ImageView lines;
    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyBJvlD3dqnz42r9obhEClc2dEJAdXt9IK8";
    private AddressResultReceiver mResultReceiver;
    ABUtil abUtil;
    protected String mAddressOutput;
    protected String mAreaOutput;
    protected String mCityOutput;
    protected String mStateOutput;
    public static TextView mLocationText;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    String description;
    Location mLocation;
    private GoogleApiClient client;
    TextView textView;
    RelativeLayout mapLayout;
    RelativeLayout coordinatorLayout;
    ImageView imageMarker;

    Handler resultHandler;
    Runnable resultRunnable;
    int isResultCode = 0;
    RelativeLayout goLayout;
    Shared shared;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        finish();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location_activity);

        init();

        checkNetworkConnection();

        resultHandler = new Handler();

        ShapeRipple ripple = (ShapeRipple) findViewById(R.id.ripple);
        ripple.setRippleShape(new Circle());
        ripple.setEnableSingleRipple(true);
        ripple.setRippleColor(Color.parseColor("#80266a7e"));


        shared.setCurrentLocation("");
        shared.setCustomerLatitude("");
        shared.setCustomerLongitude("");

        goLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLocationText.getText().toString().length() > 0) {

                    resultHandler.removeCallbacks(resultRunnable);

                    LocationsDB locationsDB = new LocationsDB(AddLocationActivity.this);
                    LocationModel locationModel = new LocationModel();
                    locationModel.setLocation(shared.getCurrentLocation());
                    locationModel.setLatitude(shared.getCustomerLatitude());
                    locationModel.setLongitude(shared.getCustomerLongitude());

                    Log.v("values", shared.getCurrentLocation() + ":::" + shared.getCustomerLatitude() + ":::" + shared.getCustomerLongitude());

                    if (shared.getCurrentLocation().length() > 0 && shared.getCustomerLatitude().length() > 0 && shared.getCustomerLongitude().length() > 0) {

                        locationsDB.insertUser(locationModel);
                        // Intent intent = new Intent(AddLocationActivity.this, LocationsListActivity.class);
                        //  startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AddLocationActivity.this, "Unable to get address for this location, Please try again", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(AddLocationActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mLocationText = (TextView) findViewById(R.id.Locality);


        mLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mLocationText.setEnabled(false);

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            mLocationText.setEnabled(true);

                        }
                    }, 1500);

                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(AddLocationActivity.this);
                    startActivityForResult(intent, 10);

                } catch (Exception e) {
                    // TODO: Handle the error.
                }
            }
        });


        if (checkPlayServices()) {

            if (!AppUtils.isLocationEnabled(mContext)) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setMessage("Location not enabled!");
                dialog.setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
            buildGoogleApiClient();
        } else {
            Toast.makeText(mContext, "Location not supported in this device", Toast.LENGTH_SHORT).show();
        }

    }


    public void init() {

        mContext = AddLocationActivity.this;
        abUtil = ABUtil.getInstance(AddLocationActivity.this);
        shared = new Shared(AddLocationActivity.this);
        lines = (ImageView) findViewById(R.id.lines);
        coordinatorLayout = (RelativeLayout) findViewById(R.id.coordinatorLayout);
        imageMarker = (ImageView) findViewById(R.id.imageMarker);
        lines.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        textView = (TextView) findViewById(R.id.text);
        mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
        goLayout = (RelativeLayout) findViewById(R.id.goLayout);

    }

    @SuppressLint("LongLogTag")
    public static ArrayList<String> autocomplete(String input) {

        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;

        StringBuilder jsonResults = new StringBuilder();

        try {

            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&types=(regions)");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());

            conn = (HttpURLConnection) url.openConnection();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder

            int read;

            char[] buff = new char[1024];

            while ((read = in.read(buff)) != -1) {

                jsonResults.append(buff, 0, read);

            }

        } catch (MalformedURLException e) {

            Log.e(LOG_TAG, "Error processing Places API URL", e);

            return resultList;

        } catch (IOException e) {

            Log.e(LOG_TAG, "Error connecting to Places API", e);

            return resultList;

        } finally {

            if (conn != null) {
                conn.disconnect();

            }

        }

        try {

            // Create a JSON object hierarchy from the results

            JSONObject jsonObj = new JSONObject(jsonResults.toString());

            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");


            // Extract the Place descriptions from the results

            resultList = new ArrayList(predsJsonArray.length());

            for (int i = 0; i < predsJsonArray.length(); i++) {

                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));

                System.out.println("============================================================");

                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));

            }

        } catch (JSONException e) {

            Log.e(LOG_TAG, "Cannot process JSON results", e);

        }

        return resultList;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            changeMap(mLastLocation);
            Log.d(TAG, "ON connected");

        } else
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, AddLocationActivity.this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "com.example.ctel.giffycustomer.Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }

    private void changeMap(Location location) {

        Log.d(TAG, "Reaching map" + mMap);
        Address locationn = null;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }

        if (mMap != null) {
            if (isResultCode == 0) {
                mMap.getUiSettings().setZoomControlsEnabled(false);
                LatLng latLong;

                latLong = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLong).zoom(15f).build();

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(latLong, 15.0f));

                imageMarker.setVisibility(View.GONE);
                mMap.getUiSettings().setTiltGesturesEnabled(false);
                startIntentService(location);
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "OnMapReady");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera postion change" + "", cameraPosition + "");
                mCenterLatLong = cameraPosition.target;

                mMap.clear();

                try {
                    if (isResultCode == 0) {
                        mLocation = new Location(description);
                        mLocation.setLatitude(mCenterLatLong.latitude);
                        mLocation.setLongitude(mCenterLatLong.longitude);

                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(mCenterLatLong.latitude, mCenterLatLong.longitude)).anchor(0.4f, 1f).icon(BitmapDescriptorFactory.fromResource(R.mipmap.location)));
                        marker.setDraggable(true);
                        imageMarker.setVisibility(View.GONE);

                        Log.e("mCenterLatLong.latitude", mCenterLatLong.latitude + "   " + mCenterLatLong.longitude);

                        shared.setCustomerLatitude(String.valueOf(mCenterLatLong.latitude));
                        shared.setCustomerLongitude(String.valueOf(mCenterLatLong.longitude));

                        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                            @Override
                            public void onMarkerDragStart(Marker marker) {

                            }

                            @Override
                            public void onMarkerDrag(Marker marker) {

                            }

                            @Override
                            public void onMarkerDragEnd(Marker marker) {

                            }
                        });


                        startIntentService(mLocation);
                        Log.e("calling: service", "onMapReady");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }

       /* mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);*/

    }

    private void startIntentService(Location mLocation) {

        Intent intent = new Intent(AddLocationActivity.this, FetchAddressIntentService.class);

        intent.putExtra(AppUtils.LocationConstants.RECEIVER, mResultReceiver);

        intent.putExtra(AppUtils.LocationConstants.LOCATION_DATA_EXTRA, mLocation);

        startService(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null)
                changeMap(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) AddLocationActivity.this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            Log.e("resultData", resultData.toString());
            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(AppUtils.LocationConstants.RESULT_DATA_KEY);
            mAreaOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_AREA);
            mCityOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_CITY);
            mStateOutput = resultData.getString(AppUtils.LocationConstants.LOCATION_DATA_STREET);

            Log.e("calling" + ":", "onReceiverResult " + "Address::" + mAddressOutput + "Area::" + mAreaOutput + "City::" + mCityOutput + "State::" + mStateOutput);

            displayAddressOutput();

            if (resultCode == AppUtils.LocationConstants.SUCCESS_RESULT) {

            }
        }

        public void displayAddressOutput() {

            try {

                if (mAreaOutput != null)
                    if (mAddressOutput.length() < 1) {
                        mLocationText.setText(mStateOutput);
                        shared.setCurrentLocation(mStateOutput);
                        Log.e("calling", "displayAddressOutput: City " + mStateOutput);
                    } else {
                        mLocationText.setText(mAddressOutput);
                        shared.setCurrentLocation(mAddressOutput);
                        Log.e("calling", "displayAddressOutput: Address " + mAddressOutput);
                    }

                if (mAreaOutput == null) {
                    Toast.makeText(AddLocationActivity.this, "Cannot get Address", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            // Check that the result was from the autocomplete widget.
            if (requestCode == REQUEST_CODE_AUTOCOMPLETE) {
                if (resultCode == RESULT_OK) {
                    // Get the user's selected place from the Intent.
                    Place place = PlaceAutocomplete.getPlace(mContext, data);

                    // TODO call location based filter

                    LatLng latLong;

                    latLong = place.getLatLng();

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLong).zoom(12f).tilt(70).build();

                    if (ActivityCompat.checkSelfPermission(AddLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling

                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(mContext, data);
            } else if (resultCode == RESULT_CANCELED) {

            }


            if (requestCode == 10 && data != null) {

                Place place = PlaceAutocomplete.getPlace(this, data);

                isResultCode = 1;
                resultHandler = new Handler();
                resultRunnable = new Runnable() {
                    @Override
                    public void run() {

                        isResultCode = 0;

                    }
                };

                resultHandler.postDelayed(resultRunnable, 3000);

                Double mlatitude = Double.valueOf(place.getLatLng().latitude);
                Double mlongitude = Double.valueOf(place.getLatLng().longitude);
                String mlocation = String.valueOf(place.getAddress());

                shared.setCurrentLocation(mlocation);
                shared.setCustomerLatitude(String.valueOf(mlatitude));
                shared.setCustomerLongitude(String.valueOf(mlongitude));
                mLocationText.setText((mlocation));
                Log.e("calling", "resultCode: " + mlocation);
                mMap.clear();

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(mlatitude, mlongitude)).zoom(15f).build();
                Log.e("latlonrecultCode", String.valueOf(mlatitude) + " : " + String.valueOf(mlongitude));
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(mlatitude, mlongitude)).anchor(0.4f, 1f).icon(BitmapDescriptorFactory.fromResource(R.mipmap.location)));
                marker.setDraggable(true);
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.location));
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                imageMarker.setVisibility(View.VISIBLE);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void checkNetworkConnection() {

        if (abUtil.isConnectingToInternet()) {

            mapLayout.setVisibility(View.VISIBLE);

        } else {

            Toast.makeText(AddLocationActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            mapLayout.setVisibility(View.GONE);

        }
    }

}


