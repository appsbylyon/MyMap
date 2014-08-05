package com.appsbylyon.mymap.app.activities;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.appsbylyon.mymap.R;
import com.appsbylyon.mymap.app.custom.AppWide;
import com.appsbylyon.mymap.app.custom.CustomAutoCompleteAdapter;
import com.appsbylyon.mymap.app.custom.MapState;
import com.appsbylyon.mymap.app.fragments.NewLocationDialog;
import com.appsbylyon.mymap.app.fragments.OpenLocationDialog;
import com.appsbylyon.mymap.app.io.FileManager;
import com.appsbylyon.mymap.app.map.GMapV2Direction;
import com.appsbylyon.mymap.app.objects.ALocation;
import com.appsbylyon.mymap.app.objects.ResultBundle;
import com.appsbylyon.mymap.app.objects.SavedLocations;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener, OnMyLocationButtonClickListener, OnItemClickListener, View.OnClickListener,
        NewLocationDialog.NewLocationDialogListener, OpenLocationDialog.OpenLocationDialogListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener
{
    private AppWide appWide = AppWide.getInstance();

    private GoogleMap mMap;

    private LocationClient mLocationClient;

    private FileManager fileManager = FileManager.getInstance();

    private AutoCompleteTextView searchBar;

    private Button clearButton;

    private SharedPreferences prefs;

    private List<Address> addressResults = new ArrayList<Address>();
    private ArrayList<String> results = new ArrayList<String>();

    private InputMethodManager imm;

    private long lastSearch;

    private double currLat = -360;
    private double currLong = 0;
    private double lastMyLat = 0;
    private double lastMyLong = 0;

    private String currTitle;

    private ArrayList<LatLng> trackPoints = new ArrayList<LatLng>();

    private boolean isTracking = false;
    private boolean getPointByTouch = false;
    private boolean removeMarker = false;

    private SavedLocations locations;

    private MapState mapState;


    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);
        mapState = fileManager.loadMapState();
        if (mapState == null)
        {
            mapState = new MapState();
            new SaveMapState().execute();
        }

        locations = fileManager.loadLocations();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        clearButton = (Button) findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);

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

        this.setUpMapIfNeeded();


    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if (removeMarker)
        {
            removeMarker = false;
            marker.remove();
            return true;
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng point)
    {
        if (getPointByTouch)
        {
            getPointByTouch = false;
            this.showNewLocationDialog(point.latitude, point.longitude);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.mapactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_add_location_by_touch:
                this.getPointByTouch = true;
                Toast.makeText(this, "Click Map To Add Location", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_remove_marker:
                this.removeMarker = true;
                Toast.makeText(this, "Click Marker To Remove", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_location_manager:
                this.showOpenLocationDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        FragmentManager fm = this.getFragmentManager();
        Bundle bundle = new Bundle();
        switch(id)
        {
            case R.id.clear_button:
                searchBar.setText("");
                break;

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

    private void restoreMapState()
    {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mapState.getCameraPosition()));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        new SaveMapState().execute();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    private void setUpMapIfNeeded()
    {
        if (mMap == null)
        {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMapClickListener(this);
                mMap.setOnMarkerClickListener(this);
                mMap.setOnCameraChangeListener(this);
                this.restoreMapState();
            }
        }
    }

    private void setUpLocationClientIfNeeded()
    {
        if (mLocationClient == null)
        {
            mLocationClient = new LocationClient(getApplicationContext(),this, this);
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
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (isTracking)
        {
            lastMyLat = location.getLatitude();
            lastMyLong = location.getLongitude();
            updateTrack(location);
            zoomToAddress(lastMyLat, lastMyLong);
        }
    }

    public void updateTrack(Location location)
    {
        if (location != null)
        {
            LatLng nextPoint = new LatLng(location.getLatitude(), location.getLongitude());
            trackPoints.add(nextPoint);
        }
        PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.BLUE);

        for(int i = 0 ; i < trackPoints.size() ; i++)
        {
            rectLine.add(trackPoints.get(i));
        }
        mMap.addPolyline(rectLine);

    }

    @Override
    public void onConnected(Bundle connectionHint) {mLocationClient.requestLocationUpdates(REQUEST,this);}

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

    @Override
    public void saveLocation(ALocation newLocation)
    {
        new SaveLocations().execute(newLocation);
    }

    @Override
    public void updateLocations(SavedLocations locations)
    {
        this.locations = locations;
        new SaveLocations().execute();
    }

    @Override
    public void markLocation(ALocation location)
    {
        String title = location.getTitle();
        int iconId = appWide.getIcons().get(location.getIcon()).getIconResourceId();
        MarkerOptions marker = new MarkerOptions();
        marker.position(location.getLocation());
        marker.title(title);
        marker.icon(BitmapDescriptorFactory.fromResource(iconId));
         mMap.addMarker(marker);
    }

    @Override
    public void gotoLocation(ALocation location)
    {
        LatLng latLng = location.getLocation();
        double mLat = latLng.latitude;
        double mLong = latLng.longitude;
        this.zoomToAddress(mLat, mLong);
    }

    @Override
    public SavedLocations getLocations() {
        return locations;
    }


    private void showOpenLocationDialog()
    {
        if (locations != null)
        {
            if (locations.getNumberOfLocations() > 0)
            {
                FragmentManager fm = this.getFragmentManager();
                OpenLocationDialog openLocationDialog = new OpenLocationDialog();
                openLocationDialog.show(fm, "open_location_fragment");
            }
            else
            {
                Toast.makeText(this, "No Saved Locations To Open!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "No Saved Locations To Open!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNewLocationDialog(double lat, double lng)
    {
        FragmentManager fm = this.getFragmentManager();
        Bundle bundle = new Bundle();
        NewLocationDialog newLoc = new NewLocationDialog();
        bundle.putDouble(getString(R.string.new_location_lat_id), lat);
        bundle.putDouble(getString(R.string.new_location_long_id), lng);
        newLoc.setArguments(bundle);
        newLoc.show(fm, "new_location_fragment_layout");
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition)
    {
        mapState.updateCameraPosition(cameraPosition);
        Log.i("MapActivity", "Camera Position Changed!");
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

        private Exception exception;

        public GetDirectionsAsyncTask(MapActivity activity /*String url*/)
        {
            super();
            this.activity = activity;
        }

        @Override
        public void onPostExecute(ArrayList<LatLng> result)
        {
            if (exception == null)
            {
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

    private class SaveLocations extends AsyncTask<ALocation, Boolean, Void>
    {
        @Override
        protected Void doInBackground(ALocation... newLocation)
        {
            if (locations == null)
            {
                locations = new SavedLocations();
            }
            if (newLocation.length > 0)
            {
                locations.addLocation(newLocation[0]);
            }
            this.publishProgress(fileManager.saveLocations(locations));
            return null;
        }

        protected void onProgressUpdate(Boolean ... results)
        {
            boolean result = results[0];
            if (result)
            {
                Toast.makeText(MapActivity.this, "My Locations Saved", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MapActivity.this, "Error Saving My Locations!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private class SaveMapState extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... newLocation)
        {
            fileManager.saveMapState(mapState);
            return null;
        }
    }


}