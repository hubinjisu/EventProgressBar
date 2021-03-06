package com.android.hubin.eventprogressbar;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Description: EventProgressBar
 * <p/>
 * Author: Hubin
 * Date: 2016/5/20.
 */
public class EventProgressBar extends LinearLayout
{
    /** success flag */
    public final static int METHOD_SUCCESS = 0;
    /** fail flag */
    public final static int METHOD_FAILED = -1;
    /** Interval for the every progress unit */
    private long progressIntervalTime = 10;
    private ArrayList<ProgressEvent> events;
    private Context mContext;
    private LinearLayout progressLayout;
    private RelativeLayout progressBarLayout;
    private TextView progressContent;
    private ImageView mSplashLoadingIV;
    private InitServiceTask initServiceTask;
    private EventProgressBarCallback callback;

    public EventProgressBar(Context context)
    {
        super(context);
        this.mContext = context;
        initView(context);
    }

    /**
     * init the content view
     * @param context
     */
    private void initView(Context context)
    {
        events = new ArrayList<ProgressEvent>();
        if (progressLayout == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            progressLayout = (LinearLayout) inflater.inflate(R.layout.view_event_progress_bar, this);
        }
        progressContent = (TextView) progressLayout.findViewById(R.id.progress_content);
        progressBarLayout = (RelativeLayout) progressLayout.findViewById(R.id.loading_layout);
        mSplashLoadingIV = (ImageView) progressLayout.findViewById(R.id.splash_loading_item);
    }

    public EventProgressBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;
        initView(context);
    }

    public EventProgressBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.mContext = context;
        initView(context);
    }

    /**
     * add the progress event
     *
     * @param event
     * @return
     */
    public EventProgressBar addEvent(ProgressEvent event)
    {
        if (event != null)
        {
            events.add(event);
        }
        return this;
    }

    /**
     * add the callback of the progress event
     *
     * @param callback
     * @return
     */
    public EventProgressBar setCallback(EventProgressBarCallback callback)
    {
        this.callback = callback;
        return this;
    }

    /**
     * Interval for the every progress unit in ms
     *
     * @param intervalTime
     * @return
     */
    public EventProgressBar setProgressUnitTime(long intervalTime)
    {
        this.progressIntervalTime = intervalTime;
        return this;
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        startEventTask();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (initServiceTask != null)
        {
            initServiceTask.cancel(true);
        }
    }

    /**
     * start the events task
     */
    public void startEventTask()
    {
        if (initServiceTask != null)
        {
            initServiceTask.cancel(true);
        }
        initServiceTask = new InitServiceTask();
        initServiceTask.execute();
    }

    public interface EventProgressBarCallback
    {
        /**
         * Handle something before starting the progress
         */
        void onProgressStarted();

        /**
         * Handle something after finishing the progress
         *
         * @param result
         */
        void onProgressFinished(int result);
    }

    private class InitServiceTask extends AsyncTask<Void, Integer, Integer>
    {
        // 进度条滑动轨迹总长相对于进度条本身的倍数
        private float eventUnitLength;
        // 单位事件在进度条上的滑动距离相当于本身的
        private float stepOffset;
        private NumberFormat numberFormat;
        private static final int PROGRESS_MAX = 100;

        @Override
        protected void onPreExecute()
        {
            progressBarLayout.setVisibility(View.VISIBLE);
            numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMaximumFractionDigits(2);
            eventUnitLength = Float.valueOf(numberFormat.format((double) mContext.getResources().getDimension(R.dimen.progress_bg_length) / mContext
                    .getResources().getDimension(R.dimen.progress_item_length))) - 1;
            // 获取平均每个事件，进度条走的距离：相对自身的比例
            stepOffset = eventUnitLength / events.size();
            if (callback != null)
            {
                callback.onProgressStarted();
            }
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            try
            {
                int progress = 0;
                int eventSize = events.size();
                for (int i = 0; i < eventSize; i++)
                {
                    int progressLength = (i + 1) * PROGRESS_MAX / eventSize;
                    while (progress < progressLength)
                    {
                        if (isCancelled())
                        {
                            return METHOD_FAILED;
                        }
                        else
                        {
                            publishProgress(progress, i);
                            SystemClock.sleep(progressIntervalTime);
                            progress++;
                        }
                    }
                    events.get(i).doEvent();
                }
                if (progress == PROGRESS_MAX)
                {
//                    publishProgress(progress, eventSize - 1);
                    SystemClock.sleep(progressIntervalTime * 10);
                }
            }
            catch (Exception e)
            {
                return METHOD_FAILED;
            }
            return METHOD_SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            if (isCancelled())
            {
                return;
            }
            startProgressAnimation(values[0]);
            progressContent.setText((values[0] + 1) + events.get(values[1]).getName());
        }

        private void startProgressAnimation(int progress)
        {
            int eventSize = events.size();
            if (progress % (PROGRESS_MAX / eventSize) == 0)
            {
                // 获取单个事件运行的起始点
                float start = Float.valueOf(numberFormat.format((double) stepOffset * progress / (PROGRESS_MAX / eventSize)));
                if (start < eventUnitLength)
                {
                    float end = Float.valueOf(numberFormat.format(start + stepOffset));
                    initAnimation(start, end);
                }
            }
        }

        @Override
        protected void onPostExecute(Integer integer)
        {
            try
            {
                if (integer == METHOD_SUCCESS)
                {
                    progressContent.setText(R.string.init_success);
                }
                else
                {
                    progressContent.setText(R.string.fail_init_device);
                    progressBarLayout.setVisibility(View.GONE);
                }

                if (callback != null)
                {
                    callback.onProgressFinished(integer);
                }
            }
            catch (Exception e)
            {
            }
        }

        private void initAnimation(float start, float end)
        {
            TranslateAnimation loadingAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            loadingAnimation.setDuration(PROGRESS_MAX * progressIntervalTime / events.size());
            loadingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            loadingAnimation.setFillAfter(true);
            mSplashLoadingIV.startAnimation(loadingAnimation);
        }

    }
}
