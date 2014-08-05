package com.appsbylyon.mymap.app.custom;

import com.appsbylyon.mymap.app.objects.Icon;

import java.util.ArrayList;

/**
 * Singleton for stuff that the app needs access to.
 *
 * Created by infinite on 8/4/2014.
 */
public class AppWide
{
    private static final AppWide instance = new AppWide();

    private AppWide(){};

    private int screenWidth;

    private int ScreenHeight;

    private ArrayList<Icon> icons = new ArrayList<Icon>();

    public static AppWide getInstance() {return instance;}

    public void setIcons(ArrayList<Icon> icons)
    {
        this.icons = icons;
    }

    public ArrayList<Icon> getIcons ()
    {
        return icons;
    }


    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return ScreenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        ScreenHeight = screenHeight;
    }
}
