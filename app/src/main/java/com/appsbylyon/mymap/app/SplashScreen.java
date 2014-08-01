package com.appsbylyon.mymap.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.appsbylyon.mymap.R;

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
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash_screen);

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
    }
}// End of SplashScreen class