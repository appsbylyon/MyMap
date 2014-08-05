package com.appsbylyon.mymap.app.io;

import android.content.Context;
import android.util.Log;

import com.appsbylyon.mymap.app.custom.MapState;
import com.appsbylyon.mymap.app.objects.SavedLocations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by infinite on 8/4/2014.
 */
public class FileManager
{
    private static final FileManager instance = new FileManager();

    private static final String LOCATIONS_FILE = "locations.loc";
    private static final String MAPSTATE_FILE = "mapstate.map";

    private Context context;

    private FileManager(){}

    public static FileManager getInstance(){return instance;}

    public void setAppContext(Context context)
    {
        this.context = context;
    }

    public synchronized boolean saveMapState(MapState mapState)
    {
        try
        {
            FileOutputStream fos = context.openFileOutput(FileManager.MAPSTATE_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mapState);
            oos.close();
            return true;
        }
        catch (IOException IOE)
        {
            Log.e("Error Saving Map State", IOE.getMessage());
            return false;
        }
    }

    public MapState loadMapState()
    {
        MapState mapState = null;
        try
        {
            FileInputStream fis = context.openFileInput(FileManager.MAPSTATE_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            mapState = (MapState) ois.readObject();
            ois.close();
        }
        catch (IOException IOE)
        {
            Log.e("IOE On Load Map State", IOE.getMessage());
        }
        catch (ClassNotFoundException CNFE)
        {
            Log.e("CNFE On Load Map State", CNFE.getMessage());
        }
        finally
        {
            return mapState;
        }
    }

    public boolean saveLocations(SavedLocations locations)
    {
        try
        {
            FileOutputStream fos = context.openFileOutput(FileManager.LOCATIONS_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(locations);
            oos.close();
            return true;
        }
        catch (IOException IOE)
        {
            Log.e("Error Saving Locations", IOE.getMessage());
            return false;
        }
    }

    public SavedLocations loadLocations()
    {
        SavedLocations locations = null;
        try
        {
            FileInputStream fis = context.openFileInput(FileManager.LOCATIONS_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            locations = (SavedLocations) ois.readObject();
            ois.close();
        }
        catch (IOException IOE)
        {
            Log.e("Error Loading Locations", IOE.getMessage());
        }
        catch (ClassNotFoundException CNFE)
        {
            Log.e("Error Loading Locations", CNFE.getMessage());
        }
        finally
        {
            return locations;
        }
    }
}
