package com.appsbylyon.mymap.app.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsbylyon.mymap.R;
import com.appsbylyon.mymap.app.objects.ALocation;

import java.util.ArrayList;

/**
 * Created by infinite on 8/4/2014.
 */
public class LocationChooserAdapter extends ArrayAdapter<ALocation>
{
    private AppWide appWide = AppWide.getInstance();

    private Context context;

    private ArrayList<ALocation> values;

    public LocationChooserAdapter(Context context, ArrayList<ALocation> values)
    {
        super(context, android.R.layout.simple_list_item_1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.location_listview_layout, parent, false);

        ALocation location = (ALocation) values.get(position);

        ImageView image = (ImageView) view.findViewById(R.id.open_location_listview_icon);
        image.setBackgroundResource((int)(appWide.getIcons().get(location.getIcon()).getIconResourceId()));

        TextView text = (TextView) view.findViewById(R.id.open_location_listview_title_label);
        text.setText(location.getTitle());

        return view;
    }
}
