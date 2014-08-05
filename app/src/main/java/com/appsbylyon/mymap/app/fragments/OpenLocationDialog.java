package com.appsbylyon.mymap.app.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsbylyon.mymap.R;
import com.appsbylyon.mymap.app.custom.AppWide;
import com.appsbylyon.mymap.app.custom.LocationChooserAdapter;
import com.appsbylyon.mymap.app.objects.ALocation;
import com.appsbylyon.mymap.app.objects.SavedLocations;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by infinite on 8/4/2014.
 */
public class OpenLocationDialog extends DialogFragment implements View.OnClickListener
{
    public interface OpenLocationDialogListener
    {
        public void markLocation(ALocation location);
        public void gotoLocation(ALocation location);
        public void updateLocations(SavedLocations locations);
        public SavedLocations getLocations();
    }

    private static final double LIST_VIEW_HEIGHT_RATION = 0.3;

    private static final int DELETE_TIMEOUT = 5000;

    private OpenLocationDialogListener activity;

    private AppWide appWide = AppWide.getInstance();

    private ListView locationList;

    private ImageView locationIcon;

    private TextView locationTitle;
    private TextView locationLat;
    private TextView locationLong;

    private Button deleteButton;
    private Button markButton;
    private Button gotoButton;
    private Button doneButton;

    private SavedLocations locations;

    private ALocation selectedLocation;
    private ALocation locationToDelete;

    private LocationChooserAdapter adapter;

    private int screenHeight;

    private int listHeight;

    private boolean buttonsDisabled = true;
    private boolean deleteTappedOnce = false;
    private boolean deleteCompleted = false;
    private boolean itemSelectedToDelete = false;

    private Toast deletePromptOne;
    private Toast deletePromptTwo;


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.frament_open_location, container);
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        getDialog().setTitle(getString(R.string.open_location_dialog_title));

        activity = (OpenLocationDialogListener) this.getActivity();

        screenHeight = appWide.getScreenHeight();

        listHeight = (int)((double) screenHeight * LIST_VIEW_HEIGHT_RATION);

        locationList = (ListView) view.findViewById(R.id.open_location_listview);

        locationIcon = (ImageView) view.findViewById(R.id.open_location_icon_preview);

        locationTitle = (TextView) view.findViewById(R.id.open_location_title_label);
        locationLat = (TextView) view.findViewById(R.id.open_location_lat_label);
        locationLong = (TextView) view.findViewById(R.id.open_location_long_label);

        deleteButton = (Button) view.findViewById(R.id.open_location_delete_button);
        markButton = (Button) view.findViewById(R.id.open_location_mark_button);
        gotoButton = (Button) view.findViewById(R.id.open_location_goto_button);
        doneButton = (Button) view.findViewById(R.id.open_location_done_button);

        deleteButton.setOnClickListener(this);
        markButton.setOnClickListener(this);
        gotoButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        deleteButton.setEnabled(false);
        markButton.setEnabled(false);
        gotoButton.setEnabled(false);

        locationTitle.setText("NO LOCATION SELECTED");
        locationLat.setText("");
        locationLong.setText("");

        locations = activity.getLocations();

        adapter = new LocationChooserAdapter(getActivity(), locations.getAllLocations());
        locationList.setAdapter(adapter);

        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                if (buttonsDisabled)
                {
                    deleteButton.setEnabled(true);
                    markButton.setEnabled(true);
                    gotoButton.setEnabled(true);
                }
                if (!deleteTappedOnce)
                {
                    selectedLocation = locations.getLocationAtPosition(pos);
                    LatLng latLng = selectedLocation.getLocation();
                    locationIcon.setBackgroundResource(appWide.getIcons().get(selectedLocation.getIcon()).getIconResourceId());
                    locationTitle.setText(selectedLocation.getTitle());
                    locationLat.setText("Lat: " + Double.toString(latLng.latitude));
                    locationLong.setText(("Long: " + Double.toString(latLng.longitude)));
                }
                else
                {
                    itemSelectedToDelete = true;
                    locationToDelete = locations.getLocationAtPosition(pos);
                    String title = locationToDelete.getTitle();
                    deletePromptOne.cancel();
                    deletePromptTwo = Toast.makeText(getActivity(), "Tap \'Delete\' Again to Remove: "+title, Toast.LENGTH_LONG);
                    deletePromptTwo.show();
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.open_location_delete_button:
                if (!deleteTappedOnce)
                {
                    deleteTappedOnce = true;
                    deletePromptOne = Toast.makeText(getActivity(), "Click The Location You Wish To Delete", Toast.LENGTH_LONG);
                    deletePromptOne.show();
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (!deleteCompleted)
                            {
                                deleteTappedOnce = false;
                                locationToDelete = null;
                            }
                            else
                            {
                                deleteCompleted = false;
                            }

                        }

                    }, DELETE_TIMEOUT);
                }
                else
                {
                    if (itemSelectedToDelete)
                    {
                        itemSelectedToDelete = false;
                        deleteTappedOnce = false;
                        deletePromptTwo.cancel();
                        Toast.makeText(getActivity(), "Deleteing: "+locationToDelete.getTitle(), Toast.LENGTH_SHORT).show();
                        locations.removeLocation(locationToDelete);
                        activity.updateLocations(locations);
                        if (locations.getNumberOfLocations() == 0)
                        {
                            this.dismiss();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            case R.id.open_location_mark_button:
                activity.markLocation(selectedLocation);
                break;
            case R.id.open_location_goto_button:
                activity.gotoLocation(selectedLocation);
                break;
            case R.id.open_location_done_button:
                this.dismiss();
                break;

        }
    }
}
