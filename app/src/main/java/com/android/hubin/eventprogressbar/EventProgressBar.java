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
    private static final String TAG = "EventProgressBar";
    /** 方法执行成功 */
    public final static int METHOD_SUCCESS = 0;
    /** 方法执行失败 */
    public final static int METHOD_FAILED = -1;
    private static final int PROGRESS_MAX = 100;
    private long progressIntervalTime = 10;
    private ArrayList<ProgressEvent> events;
    private NumberFormat numberFormat;
    private Context mContext;
    private LinearLayout progressLayout;
    private RelativeLayout progressBarLayout;
    private TextView progressContent;
    private ImageView mSplashLoadingIV;
    private ImageView mSplashLoadingBgIV;
    private InitServiceTask initServiceTask;
    private EventProgressBarCallback callback;
    private float stepOffset;

    public EventProgressBar(Context context)
    {
        super(context);
        this.mContext = context;
        initView(context);
    }

    private void initView(Context context)
    {
        events = new ArrayList<ProgressEvent>();
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);

        if (progressLayout == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            progressLayout = (LinearLayout) inflater.inflate(R.layout.view_event_progress_bar, this);
        }
        progressContent = (TextView) progressLayout.findViewById(R.id.progress_content);
        progressBarLayout = (RelativeLayout) progressLayout.findViewById(R.id.loading_layout);
        mSplashLoadingIV = (ImageView) progressLayout.findViewById(R.id.splash_loading_item);
        mSplashLoadingBgIV = (ImageView) progressLayout.findViewById(R.id.splash_loading_bg);
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
     * 添加进度回调
     * @param callback
     * @return
     */
    public EventProgressBar setCallback(EventProgressBarCallback callback)
    {
        this.callback = callback;
        return this;
    }

    /**
     * 添加进度事件
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
     * 每单位进度运行时间，单位：ms
     * @param intervalTime
     * @return
     */
    public EventProgressBar setProgressIntervalTime(long intervalTime)
    {
        this.progressIntervalTime = intervalTime;
        return this;
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (initServiceTask != null)
        {
            initServiceTask.cancel(true);
        }
        initServiceTask = new InitServiceTask();
        initServiceTask.execute();
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
     * 重新进行事件加载
     */
    public void restartEventTask()
    {
        if (initServiceTask != null)
        {
            initServiceTask.cancel(true);
        }
        initServiceTask = new InitServiceTask();
        initServiceTask.execute();
    }

    private void initAnimation(float start, float end)
    {
        TranslateAnimation loadingAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        loadingAnimation.setDuration(events.size() * progressIntervalTime);
        loadingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        loadingAnimation.setFillAfter(true);
        mSplashLoadingIV.startAnimation(loadingAnimation);
    }

    public interface EventProgressBarCallback
    {
        /**
         * 加载前准备
         */
        void onProgressStarted();

        /**
         * 加载完成
         *
         * @param result
         */
        void onProgressFinished(int result);
    }

    private class InitServiceTask extends AsyncTask<Void, Integer, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            progressBarLayout.setVisibility(View.VISIBLE);
            // 获取平均每个事件，进度条走的距离：相对自身的比例
            stepOffset = Float.valueOf(numberFormat.format((double) mContext.getResources().getDimension(R.dimen.progress_bg_length) / (mContext.getResources
                    ().getDimension(R.dimen.progress_item_length) * events.size())));
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
            progressContent.setText(values[0] + events.get(values[1]).getName());
        }

        private void startProgressAnimation(int progress)
        {
            int eventSize = events.size();
            if (progress != 0 && progress % eventSize == 0)
            {
                // 获取单个事件运行的起始点
                float start = Float.valueOf(numberFormat.format((double) stepOffset * progress / eventSize));
                float end = Float.valueOf(numberFormat.format(start + stepOffset));
                initAnimation(start, end);
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
    }
}
