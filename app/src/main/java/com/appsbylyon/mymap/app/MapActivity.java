package com.appsbylyon.mymap.app;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsbylyon.mymap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMyLocationButtonClickListener, OnItemClickListener, View.OnClickListener
{
    private GoogleMap mMap;
    private LocationClient mLocationClient;

    private Geocoder geoCoder;

    private AutoCompleteTextView searchBar;
    private TextView locationLabel;
    private TextView savedLocationLabel;

    private Button saveButton;
    private Button directionsButton;
    private Button gotoButton;

    private SharedPreferences prefs;

    private List<Address> addressResults = new ArrayList<Address>();
    private ArrayList<String> results = new ArrayList<String>();

    private InputMethodManager imm;
    private ArrayAdapter<String> searchAdapter;

    private long lastSearch;

    private double savedLat;
    private double savedLong;
    private String savedTitle;

    private boolean hasSaved;

    private double currLat = -360;
    private double currLong = 0;
    private String currTitle;

    private GMapV2Direction  md = new GMapV2Direction();


    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        hasSaved = prefs.getBoolean(getString(R.string.pref_has_saved_location), false);

        savedLocationLabel = (TextView) findViewById(R.id.map_saved_location);
        savedLocationLabel.setText("No Location Saved");

        if (hasSaved)
        {
            savedLat = (double) prefs.getFloat(getString(R.string.pref_saved_latitude), 0);
            savedLong= (double) prefs.getFloat(getString(R.string.pref_saved_longitude), 0);
            savedTitle = prefs.getString(getString(R.string.pref_saved_title), "DEFAULT");
            savedLocationLabel.setText("Saved Location: "+savedLat+", "+savedLong);
        }

        saveButton = (Button) findViewById(R.id.map_save_button);
        saveButton.setOnClickListener(this);

        directionsButton = (Button) findViewById(R.id.map_directions_button);
        directionsButton.setOnClickListener(this);

        gotoButton = (Button) findViewById(R.id.map_goto_button);
        gotoButton.setOnClickListener(this);

        locationLabel = (TextView) findViewById(R.id.map_location_label);
        locationLabel.setText("No Location");

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        searchBar = (AutoCompleteTextView) findViewById(R.id.map_activity_search_bar);
        searchBar.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable view){}

            @Override
            public void beforeTextChanged(CharSequence text, int arg1, int arg2, int arg3){}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count)
            {
                lastSearch = System.currentTimeMillis();
                new SearchAddress().execute(text.toString().trim(), Long.toString(lastSearch));
            }

        });

        searchBar.setOnItemClickListener(this);

        geoCoder =  new Geocoder(this, Locale.US);
        this.setUpMapIfNeeded();


    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch(id)
        {
            case R.id.map_save_button:

                if (currLat != -360)
                {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putFloat(getString(R.string.pref_saved_latitude), (float) currLat);
                    edit.putFloat(getString(R.string.pref_saved_longitude), (float) currLong);
                    edit.putString(getString(R.string.pref_saved_title), currTitle);
                    edit.putBoolean(getString(R.string.pref_has_saved_location), true);
                    edit.apply();
                    savedLocationLabel.setText("Saved Location: " + currLat + ", " + currLong);
                    savedLat = currLat;
                    savedLong = currLong;
                }
                break;
            case R.id.map_goto_button:
                if (hasSaved)
                {
                    addMarker(savedLat, savedLong, savedTitle);
                    zoomToAddress(savedLat, savedLong);
                }
                break;
            case R.id.map_directions_button:
                if(hasSaved && currLat != -360)
                {
                    LatLng fromPosition = new LatLng(savedLat, savedLong);
                    LatLng toPosition = new LatLng(currLat, currLong);

                    findDirections(savedLat, savedLong, currLat, currLong, GMapV2Direction.MODE_DRIVING );
                }


        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
    }

    private List<Address> getAddresses(String searchName)
    {
        if (Geocoder.isPresent())
        {
            try
            {
                return geoCoder.getFromLocationName(searchName, 5);
            }
            catch(IOException IOE)
            {
                Toast.makeText(this, "Unable to get location info", Toast.LENGTH_SHORT).show();
                Log.e("GeoAddress", "Failed to get location info", IOE);
                return null;
            }
        }
        else
        {
            Toast.makeText(this, "GeoCoder is Unavailable!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void zoomToAddress(Address address)
    {
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(address.getLatitude(), address.getLongitude()))
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    private void zoomToAddress(double lat, double mLong)
    {
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(lat, mLong))
                .zoom(17)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

        private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.

            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);

            }
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    private void addMarker(Address address)
    {
        currLat = address.getLatitude();
        currLong = address.getLongitude();
        currTitle = address.getFeatureName();

        MarkerOptions marker = new MarkerOptions();
        LatLng coords = new LatLng(currLat, currLong);
        marker.position(coords);
        marker.title(currTitle);
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.infinity));
        mMap.addMarker(marker);
        locationLabel.setText("Location: " + currLat + ", " + currLong);

    }

    private void addMarker(double lat, double mLong, String mTitle)
    {
        currLat = lat;
        currLong = mLong;

        MarkerOptions marker = new MarkerOptions();
        LatLng coords = new LatLng(currLat, currLong);
        marker.position(coords);
        marker.title(mTitle);
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.infinity));
        mMap.addMarker(marker);
        locationLabel.setText("Location: " + currLat + ", " + currLong);

    }

    @Override
    public void onLocationChanged(Location location){}

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
    }

    @Override
    public void onDisconnected() {}

    @Override
    public void onConnectionFailed(ConnectionResult result) {}

    @Override
    public boolean onMyLocationButtonClick(){return false;}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long rowId)
    {
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        Address location = (Address) addressResults.get(position);
        this.zoomToAddress(location);
        this.addMarker(location);

    }



    private class SearchAddress extends AsyncTask<String, String, ResultBundle>
    {
        @Override
        protected ResultBundle doInBackground(String... searchText)
        {
            String searchString = searchText[0];
            ResultBundle bundle = new ResultBundle();
            bundle.setBundleTime(Long.parseLong(searchText[1]));
            ArrayList<Address> addressResults =  new ArrayList<Address>();
            Geocoder localGeo = new Geocoder(MapActivity.this, Locale.US);
            ArrayList<String> localResults = new ArrayList<String>();
            {
                try
                {
                    addressResults = (ArrayList<Address>) localGeo.getFromLocationName(searchString, 6);
                }
                catch (Exception E)
                {
                    this.publishProgress("Error Searching For Address: "+E.getMessage());
                }

            }
           for (Address addressResult : addressResults)
            {
                String thisLine = "";
                boolean featureNameRepeated = false;
                if (addressResult.getMaxAddressLineIndex() > 0)
                {
                    if (thisLine.length() != 0) {
                        thisLine +=", ";
                    }
                    for (int i = 0; i < addressResult.getMaxAddressLineIndex(); i++)
                    {
                        if (addressResult.getAddressLine(i) != null)
                        {
                            if (!addressResult.getAddressLine(i).equalsIgnoreCase(addressResult.getFeatureName()))
                            {
                                thisLine += addressResult.getAddressLine(i);
                                if (i!=addressResult.getMaxAddressLineIndex()-1)
                                {
                                    thisLine += ", ";
                                }
                            }
                            else
                            {
                                featureNameRepeated = true;
                            }
                        }
                    }
                    if (featureNameRepeated)
                    {
                        if (addressResult.getFeatureName() != null)
                        {
                            thisLine = addressResult.getFeatureName() + ", " + thisLine;
                        }
                    }
                    localResults.add(thisLine);
                }


            }
            bundle.setAddresses(addressResults);
            bundle.setSearchResults(localResults);
            return bundle;


        }

        protected void onProgressUpdate(String ...strings)
        {
            Toast.makeText(MapActivity.this, strings[0], Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(ResultBundle results)
        {
            updateSearchBar(results);
        }
    }

    private synchronized void updateSearchBar(ResultBundle bundle)
    {
        results = bundle.getSearchResults();
        addressResults = bundle.getAddresses();
        if (bundle.getBundleTime() == lastSearch)
        {
            CustomAutoCompleteAdapter searchAdapter = new CustomAutoCompleteAdapter(MapActivity.this, results);
            searchBar.setAdapter(searchAdapter);

        }
    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints)
    {
        //Polyline newPolyline;
        PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }
        mMap.addPolyline(rectLine);
    }


    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);
    }

    public class GetDirectionsAsyncTask extends AsyncTask<Map<String, String>, Object,   ArrayList<LatLng>> {

        public static final String USER_CURRENT_LAT = "user_current_lat";
        public static final String USER_CURRENT_LONG = "user_current_long";
        public static final String DESTINATION_LAT = "destination_lat";
        public static final String DESTINATION_LONG = "destination_long";
        public static final String DIRECTIONS_MODE = "directions_mode";
        private MapActivity activity;
        private String url;

        private Exception exception;

        //private Dialog progressDialog;

        public GetDirectionsAsyncTask(MapActivity activity /*String url*/)
        {
            super();
            this.activity = activity;

            //  this.url = url;
        }

        public void onPreExecute() {
            //progressDialog = DialogUtils.createProgressDialog(activity, activity.getString(R.string.get_data_dialog_message));
            //progressDialog.show();
        }

        @Override
        public void onPostExecute(ArrayList<LatLng> result) {
           // progressDialog.dismiss();

            if (exception == null) {
                activity.handleGetDirectionsResult(result);
            } else {
                processException();
            }
        }

        @Override
        protected ArrayList<LatLng> doInBackground(Map<String, String>... params) {

            Map<String, String> paramMap = params[0];
            try{
                LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
                LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)) , Double.valueOf(paramMap.get(DESTINATION_LONG)));
                GMapV2Direction md = new GMapV2Direction();
                Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
                ArrayList<LatLng> directionPoints = md.getDirection(doc);
                return directionPoints;

            }
            catch (Exception e) {
                exception = e;
                return null;
            }
        }

        private void processException() {
            Toast.makeText(activity, "Error getting directions!", Toast.LENGTH_SHORT).show();
        }

    }
}

