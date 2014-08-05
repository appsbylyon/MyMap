package com.appsbylyon.mymap.app.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsbylyon.mymap.R;
import com.appsbylyon.mymap.app.objects.Icon;

import java.util.ArrayList;

/**
 * Created by infinite on 8/4/2014.
 */
public class IconChooserAdapter extends ArrayAdapter<Icon>
{
    private final Context context;

    private ArrayList<Icon> values;

    public IconChooserAdapter(Context context, ArrayList<Icon> values)
    {
        super(context, android.R.layout.simple_list_item_1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.icon_chooser_layout, parent, false);

        Icon icon = (Icon) values.get(position);

        ImageView iconView = (ImageView) view.findViewById(R.id.icon_chooser_icon);
        iconView.setImageResource(icon.getIconResourceId());

        TextView text = (TextView) view.findViewById(R.id.icon_chooser_text);
        text.setText(icon.getIconDescription());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.icon_chooser_layout, parent, false);

        Icon icon = (Icon) values.get(position);

        ImageView iconView = (ImageView) view.findViewById(R.id.icon_chooser_icon);
        iconView.setImageResource(icon.getIconResourceId());

        TextView text = (TextView) view.findViewById(R.id.icon_chooser_text);
        text.setText(icon.getIconDescription());

        return view;
    }
}
