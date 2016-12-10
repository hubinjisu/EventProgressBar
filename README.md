# EventProgressBar
EventProgressBar
It is simple to use as the following.
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
            });
            
The Demo gif is in the following:

![image](https://github.com/hubinjisu/images/blob/master/images/event_progress_bar.gif)
