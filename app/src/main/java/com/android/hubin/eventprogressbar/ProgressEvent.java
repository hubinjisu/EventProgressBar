package com.android.hubin.eventprogressbar;

/**
 * Created by Hubin on 2016/5/20.
 */
public abstract class ProgressEvent
{
    private String name;

    public ProgressEvent(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public abstract void doEvent();
}
