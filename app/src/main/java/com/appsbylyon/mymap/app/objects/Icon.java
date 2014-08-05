package com.appsbylyon.mymap.app.objects;

/**
 * Created by infinite on 8/4/2014.
 */
public class Icon
{
    private int iconResourceId;

    private String iconDescription;

    public Icon (int resourceId, String description)
    {
        this.setIconResourceId(resourceId);
        this.setIconDescription(description);
    }


    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public String getIconDescription() {
        return iconDescription;
    }

    public void setIconDescription(String iconDescription) {
        this.iconDescription = iconDescription;
    }
}
