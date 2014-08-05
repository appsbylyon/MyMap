package com.appsbylyon.mymap.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;

import com.appsbylyon.mymap.R;
import com.appsbylyon.mymap.app.custom.AppWide;
import com.appsbylyon.mymap.app.io.FileManager;
import com.appsbylyon.mymap.app.objects.Icon;

import java.util.ArrayList;

/**
 * Splash Screen Activity
 *
 * Modified: 7/18/2014
 *
 * @author Adam Lyon
 *
 */
public class SplashScreen extends Activity
{
    private static final int SHOW_SPLASH_TIME = 500;

    private AppWide appWide = AppWide.getInstance();

    private FileManager fileManager = FileManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash_screen);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        appWide.setScreenWidth(size.x);
        appWide.setScreenHeight(size.y);

        fileManager.setAppContext(this.getApplicationContext());

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent mainActivity = new Intent(SplashScreen.this, MapActivity.class);
                startActivity(mainActivity);
                finish();
            }

        }, SHOW_SPLASH_TIME);

        loadIcons();
    }

    private void loadIcons()
    {
        ArrayList<Icon> icons = new ArrayList<Icon>();

        icons.add(new Icon(R.drawable.infinity, "Infinity"));
        icons.add(new Icon(R.drawable.moutains, "Mountains"));

        appWide.setIcons(icons);
    }
}// End of SplashScreen class