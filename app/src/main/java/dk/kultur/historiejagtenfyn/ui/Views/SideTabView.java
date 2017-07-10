package dk.kultur.historiejagtenfyn.ui.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import dk.kultur.historiejagtenfyn.R;

public class SideTabView extends ImageView {
    //private int mWidth;
    private int mHeight;
    private GestureDetector mGestureDetector;
    private OnTabClickedListener mListener;

    private int mActiveTabId = 0;
    private int mTabCount = 3;
    private boolean mIsQuiz;

    public SideTabView(Context context) {
        super(context);
    }

    public SideTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(int tabCount, boolean hasQuiz, int activeTab) {
        mActiveTabId = activeTab;
        mTabCount = tabCount;
        mIsQuiz = hasQuiz;

        setImageResource(getActiveTabResource(mActiveTabId));

    }

    public void setOnTabClickListener(OnTabClickedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //mWidth = (int) getResources().getDimension(R.dimen.tabbar_width);
        int resourceId = -1;
        if (mTabCount == 2) {
            resourceId = R.dimen.side_tab_height2;
        }
        if (mTabCount == 3) {
            resourceId = R.dimen.side_tab_height3;
        }
        if (resourceId == -1) {
            mHeight = 0;
        } else {
            mHeight = (int) getResources().getDimension(resourceId);
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getContext(), new MyGestureDetecor());
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
//            if (!mGestureDetector.onTouchEvent(event)) {
//                setImageResource(getActiveTabResource());
//            }
            mGestureDetector.onTouchEvent(event);
            return true;
        } else {
            return mGestureDetector.onTouchEvent(event);
        }
    }

    class MyGestureDetecor extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onTabClicked(e.getY());
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            setImageResource(getActiveTabResource(getTabByY(e.getY())));
            return true;
        }
    }

    private void onTabClicked(float yPos) {
        if (mListener != null) {
            mListener.onTabClicked(getTabByY(yPos), true);
        }
    }

    private int getTabByY(float yPos) {
        double fragment = mHeight / 2;
        if (mTabCount == 3) {
            fragment = mHeight / 3;
        }
        if (yPos < fragment) {
            return 1;
        } else if (yPos < 2 * fragment) {
            return 2;
        } else {
            return 3;
        }
    }

    private int getActiveTabResource(int tabID) {
        switch (tabID) {
            case 1:
                if (mTabCount == 3) {
                    return (R.drawable.side_menu_content3);
                } else {
                    if (mIsQuiz) {
                        return (R.drawable.side_menu_content_quiz);
                    } else {
                        return (R.drawable.side_menu_content2);
                    }
                }
            case 2:
                if (mTabCount == 3) {
                    return (R.drawable.side_menu_practical3);
                } else {
                    if (mIsQuiz) {
                        return (R.drawable.side_menu_quiz_content);
                    } else {
                        return (R.drawable.side_menu_practical2);
                    }
                }
            case 3:
                return (R.drawable.side_menu_quiz3);
        }
        return -1;
    }

    public interface OnTabClickedListener {
        public void onTabClicked(int id, boolean animated);
    }
}
