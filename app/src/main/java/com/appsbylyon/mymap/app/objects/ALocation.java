package com.appsbylyon.mymap.app.objects;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by infinite on 8/2/2014.
 */
public class ALocation implements Serializable
{
    private static final long serialVersionUID = 885466L;

    private double latitude;
    private double longitude;
    private String title;
    private int icon;

    public ALocation(double latitude, double longitude, String title, int iconResource)
    {
        this.setLocation(latitude, longitude);
        this.setTitle(title);
        this.icon = iconResource;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public void setLocation(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
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
