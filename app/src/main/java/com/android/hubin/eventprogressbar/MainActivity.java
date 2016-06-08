package com.android.hubin.eventprogressbar;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    private EventProgressBar progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressLayout = (EventProgressBar) findViewById(R.id.progressBar_layout);
        findViewById(R.id.reload).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                progressLayout.restartEventTask();
            }
        });
        initTaskEvents();
    }

    private void initTaskEvents()
    {
        if (progressLayout != null)
        {
            progressLayout.setProgressUnitTime(50).setCallback(new EventProgressBar.EventProgressBarCallback()
            {
                @Override
                public void onProgressStarted()
                {
                }

                @Override
                public void onProgressFinished(int result)
                {
                }
            }).addEvent(new ProgressEvent(getString(R.string.clear_error_data))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_common_service))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(1500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_audio_service))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_video_service))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            })/*.addEvent(new ProgressEvent(getString(R.string.init_admin))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_contact))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(1500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_call_record))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_system_setting))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_external_device))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(1500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_config_service))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            }).addEvent(new ProgressEvent(getString(R.string.init_camera))
            {
                @Override
                public void doEvent()
                {
                    SystemClock.sleep(500);
                }
            })*/;
        }
    }
}
