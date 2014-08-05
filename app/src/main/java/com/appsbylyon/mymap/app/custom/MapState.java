package com.appsbylyon.mymap.app.custom;

import com.appsbylyon.mymap.app.objects.ALocation;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by infinite on 8/4/2014.
 */
public class MapState implements Serializable
{
    private static final long serialVersionUID = 121466L;

    private double cameraPosLat = 0;
    private double cameraPosLong = 0;

    private float bearing = 0;
    private float tilt = 0;
    private float zoom = 17;

    private ArrayList<ALocation> markers = new ArrayList<ALocation>();

    public void updateCameraPosition(CameraPosition camPos)
    {
        cameraPosLat = camPos.target.latitude;
        cameraPosLong = camPos.target.longitude;
        bearing = camPos.bearing;
        tilt = camPos.tilt;
        zoom = camPos.zoom;
    }

    public CameraPosition getCameraPosition()
    {
        CameraPosition camPos = new CameraPosition.Builder()
                .target(new LatLng(cameraPosLat, cameraPosLong))
                .zoom(zoom)
                .tilt(tilt)
                .bearing(bearing)
                .build();
        return camPos;
    }

    public ArrayList<ALocation> getMarkers()
    {
        return markers;
    }
}
