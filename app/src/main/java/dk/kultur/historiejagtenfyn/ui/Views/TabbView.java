package dk.kultur.historiejagtenfyn.ui.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import dk.kultur.historiejagtenfyn.R;

/**
 * Created by RokasTS on 2014.06.30.
 */
public class TabbView extends ImageView {
    private int mWidth;
    private GestureDetector mGestureDetector;
    private int mActiveTabId = 0;
    private OnTabClickedListener mListener;

    public TabbView(Context context) {
        super(context);
        init();
    }

    public TabbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabbView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setImageResource(R.drawable.tabbar_isolated);
    }

    public void setActiveTabId(int id) {
        mActiveTabId = id;
        setImageResource(getActiveTabResource());
    }

    public void setOnTabClickListener(OnTabClickedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = (int) getResources().getDimension(R.dimen.tabbar_width);
        super.onMeasure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getContext(), new MyGestureDetecor());
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!mGestureDetector.onTouchEvent(event)) {
                setImageResource(getActiveTabResource());
            }
            return true;
        } else {
            return mGestureDetector.onTouchEvent(event);
        }
    }

    class MyGestureDetecor extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onTabClicked(e.getX());
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            onTabPressed(e.getX());
            return true;
        }
    }

    private void onTabPressed(float xPos) {
        switch (getTabByX(xPos)) {
            case 1:
                setImageResource(R.drawable.tabbar_isolated_routes);
                break;
            case 2:
                setImageResource(R.drawable.tabbar_isolated_map);
                break;
            case 3:
                setImageResource(R.drawable.tabbar_isolated_ar_view);
                break;
            case 4:
                setImageResource(R.drawable.tabbar_isolated_scan);
                break;
        }
    }

    private void onTabClicked(float xPos) {
        if (mListener != null) {
            mListener.onTabClicked(getTabByX(xPos), true);
        }
    }

    private int getTabByX(float xPos) {
        double fragment = mWidth / 4;
        if (xPos < fragment) {
            return 1;
        } else if (xPos < 2 * fragment) {
            return 2;
        } else if (xPos < 3 * fragment) {
            return 3;
        } else {
            return 4;
        }
    }

    private int getActiveTabResource() {
        switch (mActiveTabId) {
            case 0:
                return R.drawable.tabbar_isolated;
            case 1:
                return R.drawable.tabbar_isolated_routes;
            case 2:
                return R.drawable.tabbar_isolated_map;
            case 3:
                return R.drawable.tabbar_isolated_ar_view;
            case 4:
                return R.drawable.tabbar_isolated_scan;
            default:
                return R.drawable.tabbar_isolated;
        }
    }

    public interface OnTabClickedListener {
        public void onTabClicked(int id, boolean animate);
    }
}
