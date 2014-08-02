package com.appsbylyon.mymap.app;


import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMyLocationButtonClickListener, OnItemClickListener
{
    private GoogleMap mMap;
    private LocationClient mLocationClient;

    private Geocoder geoCoder;

    private AutoCompleteTextView searchBar;

    private List<Address> addressResults = new ArrayList<Address>();
    private ArrayList<String> results = new ArrayList<String>();

    private InputMethodManager imm;
    private ArrayAdapter<String> searchAdapter;

    private long lastSearch;


    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        searchBar = (AutoCompleteTextView) findViewById(R.id.map_activity_search_bar);
        //searchBar.setThreshold(1);
        //searchAdapter = new ArrayAdapter<String>(MapActivity.this, android.R.layout.simple_dropdown_item_1line, results);
       // searchBar.setAdapter(searchAdapter);
        //searchAdapter.setNotifyOnChange(true);
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
                /**
                addressResults = MapActivity.this.getAddresses(text.toString());
                if (addressResults != null)
                {
                    results.clear();
                    for (Address addressResult : addressResults)
                    {
                    String thisLine = "";
                        if (addressResult.getFeatureName() != null)
                        {
                            thisLine += addressResult.getFeatureName();
                        }
                        if (addressResult.getMaxAddressLineIndex() > 0)
                        {
                            if (thisLine.length() != 0)
                            {
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
                                }
                            }
                            results.add(thisLine);
                        }

                    }
                    if (!results.isEmpty())
                    {
                        CustomAutoCompleteAdapter searchAdapter = new CustomAutoCompleteAdapter(MapActivity.this, results);
                        searchBar.setAdapter(searchAdapter);
                    }

                }
                 */
            }

        });

        searchBar.setOnItemClickListener(this);

        geoCoder =  new Geocoder(this, Locale.US);
        this.setUpMapIfNeeded();


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
        /**
        try
        {
           List<Address> addys =  geoCoder.getFromLocationName("8054 Alma Mesa Way", 1);
           if (!addys.isEmpty())
           {
               Address addy = addys.get(0);
               String line = "";
               line += "Feature Name: "+addy.getFeatureName();
               int maxLine = addy.getMaxAddressLineIndex();
               if (maxLine > 0)
               {
                   for (int i = 0; i< maxLine; i++)
                   {
                       String thisLine = addy.getAddressLine(i);

                   }
               }

           }

        }
        catch (Exception E)
        {
            Toast.makeText(this, "Unable to search for results!", Toast.LENGTH_SHORT).show();
        }
         */
        //return null;
    }

    private void zoomToAddress(Address address)
    {
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(address.getLatitude(), address.getLongitude()))
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

    public void showMyLocation(View view) {

        zoomToAddress((Address)getAddresses("5221 Madison Ave, Sacramento, CA 95841").get(0));
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void addMarker(Address address)
    {
        mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())).title(address.getFeatureName()));
    }

    @Override
    public void onLocationChanged(Location location)
    {

    }

    /**
     * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationClient.requestLocationUpdates(
                REQUEST,
                this);  // LocationListener
    }

    /**
     * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
     */
    @Override
    public void onDisconnected() {
        // Do nothing
    }

    /**
     * Implementation of {@link OnConnectionFailedListener}.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do nothing
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

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
            //publishProgress(searchText);

            ResultBundle bundle = new ResultBundle();
            bundle.setBundleTime(Long.parseLong(searchText[1]));
            ArrayList<Address> addressResults =  new ArrayList<Address>();
            Geocoder localGeo = new Geocoder(MapActivity.this, Locale.US);
            try
            {
                addressResults = (ArrayList<Address>) localGeo.getFromLocationName(searchText[0], 6);

            }
            catch (Exception E)
            {

            }
            ArrayList<String> localResults = new ArrayList<String>();
            for (Address addressResult : addressResults)
            {
                String thisLine = "";
                //if (addressResult.getFeatureName() != null)
                //{
                //    thisLine += addressResult.getFeatureName();
               // }
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
            //bundle.setBundleTime(Long.parseLong(searchText[1]));
            return bundle;


        }

        protected void onProgressUpdate(String ...strings)
        {

        }

        protected void onPostExecute(ResultBundle results)
        {
            updateSearchBar(results);
        }
    }

    private synchronized void updateSearchBar(ResultBundle bundle)
    {
        //long bundleSearchTime = bundle.getBundleTime();
        //if (bundleSearchTime == lastSearch)
        //{

        results = bundle.getSearchResults();
        addressResults = bundle.getAddresses();
        if (bundle.getBundleTime() == lastSearch)
        {
            if (!results.isEmpty())
            {
                CustomAutoCompleteAdapter searchAdapter = new CustomAutoCompleteAdapter(MapActivity.this, results);
                searchBar.setAdapter(searchAdapter);
            }
        }
        //}
    }
}

