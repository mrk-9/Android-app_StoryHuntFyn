package dk.kultur.historiejagtenfyn.ui.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import dk.kultur.historiejagtenfyn.R;

/**
 * Created by JustinasK on 2/9/2015.
 */
public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private static final String LOG_TAG = SimpleDividerItemDecoration.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    private Drawable mDivider;

    public SimpleDividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.list_divider);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }

        //Draw additional dividers
        if (childCount > 0) {
            //draw TOP divider
            View child = parent.getChildAt(0);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int topFirst = child.getTop() - params.topMargin;
            int bottomFirst = topFirst + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, topFirst, right, bottomFirst);
            mDivider.draw(c);

            //draw BOTTOM divider
            View childB = parent.getChildAt(childCount - 1);
            RecyclerView.LayoutParams paramsB = (RecyclerView.LayoutParams) childB.getLayoutParams();
            //top will go up in - divider height
            int top = childB.getBottom() + paramsB.bottomMargin - mDivider.getIntrinsicHeight();
            int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}