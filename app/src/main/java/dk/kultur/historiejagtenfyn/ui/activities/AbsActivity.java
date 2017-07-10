package dk.kultur.historiejagtenfyn.ui.activities;

import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.Views.CurlView;
import dk.kultur.historiejagtenfyn.ui.Views.PageProvider;

/**
 * Created by RokasTS on 2014.06.27.
 */
abstract class AbsActivity extends ActivityGroup {

    private CurlView mCurlView;
    private static Handler mHandler;
    private View mDecorView;
    private Intent mNextActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            boolean fullScreen = (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
            boolean actionbarVisible = getActionBar() != null && getActionBar().isShowing();
            if (!fullScreen || actionbarVisible) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    protected void startActivityWithPageCurlAnimation(Intent intentToStart) {

        mNextActivityIntent = intentToStart;
        List<Bitmap> bitmaps = initBitmaps(intentToStart);

        mCurlView = (CurlView) findViewById(R.id.curl_view);
        BitmapDrawable drawable = new BitmapDrawable(bitmaps.get(0));
        mCurlView.setBackgroundDrawable(drawable);

        mCurlView.setPageProvider(new PageProvider(this, bitmaps));
        mCurlView.setSizeChangedObserver(new SizeChangedObserver());
        mCurlView.setCurrentIndex(0);
        mCurlView.setVisibility(View.VISIBLE);
        mHandler = new PageCurlHandler();
        mHandler.sendEmptyMessage(1);
    }

    private List<Bitmap> initBitmaps(Intent intentToStart) {
        mDecorView = getWindow().getDecorView();
        Bitmap currentBm = getBitmapFromView(mDecorView);


        View nextView = getLocalActivityManager().startActivity("B", intentToStart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
        nextView.setDrawingCacheEnabled(true);

        nextView.measure(View.MeasureSpec.makeMeasureSpec(mDecorView.getWidth(),
                        View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mDecorView.getHeight(),
                        View.MeasureSpec.EXACTLY)
        );
        nextView.layout(0, 0, nextView.getMeasuredWidth(), nextView.getMeasuredHeight());

        Bitmap nextBm = getBitmapFromView(nextView);


        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        bitmaps.add(currentBm);
        bitmaps.add(nextBm);
        return bitmaps;

    }

    private Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false); // clear drawing cache
        return b;
    }

    private class PageCurlHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, mDecorView.getWidth(), mDecorView.getHeight() / 2, 0);
                mCurlView.dispatchTouchEvent(motionEvent);
                Message m = Message.obtain();
                m.what = 2;
                m.arg2 = mDecorView.getWidth();
                mCurlView.setBackgroundDrawable(null);
                this.sendMessage(m);
            } else if (msg.what == 2) {
                int x = msg.arg2 - 30;
                if (x > 0) {
                    MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, x, mDecorView.getHeight() / 2, 0);
                    Message m = Message.obtain();
                    mCurlView.dispatchTouchEvent(motionEvent);
                    m.what = 2;
                    m.arg2 = x;
                    this.sendMessageDelayed(m, 1);
                } else {
                    this.sendEmptyMessageDelayed(3, 20);
                }

            } else if (msg.what == 3) {
                MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, mDecorView.getHeight() / 2, 0);
                Message m = Message.obtain();
                mCurlView.dispatchTouchEvent(motionEvent);
                startActivity(mNextActivityIntent);
                mNextActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
            }
            // mCurlView.onDrawFrame();
            mCurlView.invalidate();
            super.handleMessage(msg);
        }
    }

    /**
     * CurlView size changed observer.
     */
    private class SizeChangedObserver implements CurlView.SizeChangedObserver {
        @Override
        public void onSizeChanged(int w, int h) {
            if (w > h) {
                mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
                // mCurlView.setMargins(.1f, .05f, .1f, .05f);
            } else {
                mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
                float verticalMargin = getResources().getDimensionPixelSize(R.dimen.bg_vertical_margin);
                float horizontalMargin = getResources().getDimensionPixelSize(R.dimen.bg_horizontal_margin);
               // mCurlView.setMargins(0, verticalMargin, horizontalMargin, verticalMargin);
                //mCurlView.setMargins(0, 0, 0, 10);
            }
        }
    }

}
