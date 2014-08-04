package com.appsbylyon.mymap.app.com.appsbylyon.mymap.app.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.appsbylyon.mymap.R;
import com.appsbylyon.mymap.app.ALocation;

/**
 * Created by infinite on 8/3/2014.
 */
public class NewLocationDialog extends DialogFragment implements View.OnClickListener
{
    public interface NewLocationDialogListener
    {
        public void saveLocation(ALocation newLocation);
    }

    private NewLocationDialogListener activity;

    private TextView latLabel;
    private TextView longLabel;

    private EditText titleEntry;

    private Spinner iconSelector;

    private Button confirmButton;
    private Button cancelButton;

    private double currLat;
    private double currLong;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_save_new_location, container);
        getDialog().getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        getDialog().setTitle(getString(R.string.new_location_title_text));

        Bundle bundle = this.getArguments();
        currLat = bundle.getDouble(getString(R.string.new_location_lat_id));
        currLong = bundle.getDouble(getString(R.string.new_location_long_id));

        activity = (NewLocationDialogListener) this.getActivity();

        latLabel = (TextView) view.findViewById(R.id.new_location_lat_label);
        latLabel.setText("Latitude: " + Double.toString(currLat));

        longLabel = (TextView) view.findViewById(R.id.new_location_long_label);
        longLabel.setText("Longitude: " + Double.toString(currLong));

        titleEntry = (EditText) view.findViewById(R.id.new_location_title_entry);

        iconSelector = (Spinner) view.findViewById(R.id.new_location_icon_selection_spinner);

        cancelButton = (Button) view.findViewById(R.id.new_layout_cancel_button);
        cancelButton.setOnClickListener(this);

        confirmButton = (Button) view.findViewById(R.id.new_layout_confirm_button);
        confirmButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch(id)
        {
            case R.id.new_layout_cancel_button:
                this.dismiss();
                break;
            case R.id.new_layout_confirm_button:
                this.dismiss();
                break;
        }
    }


}
