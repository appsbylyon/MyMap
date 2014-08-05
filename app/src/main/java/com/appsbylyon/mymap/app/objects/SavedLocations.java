package com.appsbylyon.mymap.app.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by infinite on 8/2/2014.
 */
public class SavedLocations implements Serializable
{
    private static final long serialVersionUID = 23442L;

    private ArrayList<ALocation> locations = new ArrayList<ALocation>();

    public void setLocations (ArrayList<ALocation> locations)
    {
        this.locations = locations;
    }

    public void setLocationAtPosition(ALocation location, int pos)
    {
        locations.set(pos, location);
    }

    public void addLocation(ALocation location)
    {
        locations.add(location);
    }

    public void removeLocation(ALocation location)
    {
        locations.remove(location);
    }

    public void removeLocationAtPosition(int pos)
    {
        locations.remove(pos);
    }

    public ArrayList<ALocation> getAllLocations()
    {
        return locations;
    }

    public ALocation getLocationAtPosition(int pos)
    {
        return (ALocation) locations.get(pos);
    }

    public int getNumberOfLocations()
    {
        return locations.size();
    }
}
