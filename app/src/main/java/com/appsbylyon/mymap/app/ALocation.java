package com.appsbylyon.mymap.app;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by infinite on 8/2/2014.
 */
public class ALocation implements Serializable
{
    private static final long serialVersionUID = 885466L;

    private LatLng location;
    private String title;
    private int icon;

    public ALocation(LatLng location, String title)
    {
        this.setLocation(location);
        this.setTitle(title);
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon()
    {
        return icon;
    }

    public void setIcon(int icon)
    {
        this.icon = icon;
    }
}
