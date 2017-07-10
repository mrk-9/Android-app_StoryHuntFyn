package dk.kultur.historiejagtenfyn.ui.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.List;

import dk.kultur.historiejagtenfyn.R;

/**
 * Created by RokasTS on 2014.07.01.
 */
public class PageProvider implements CurlView.PageProvider {

    private Context mContext;
private List<Bitmap> mBitmaps;

    public PageProvider(Context context, List<Bitmap> bitmaps) {
        mContext = context;
        mBitmaps = bitmaps;
    }

    @Override
    public int getPageCount() {
        return 2;
    }



    @Override
    public void updatePage(CurlPage page, int width, int height, int index) {

        switch (index) {
            // First case is image on front side, solid colored back.
            case 0: {
                Log.d("test", "0");
                Bitmap front = mBitmaps.get(0).copy(Bitmap.Config.ARGB_8888, false);
                page.setTexture(front, CurlPage.SIDE_FRONT);
                page.setTexture(front, CurlPage.SIDE_BACK);
                break;
            }
            // Second case is image on back side, solid colored front.
            case 1: {
                Log.d("test", "1");
                Bitmap back = mBitmaps.get(1).copy(Bitmap.Config.ARGB_8888, false);
                page.setTexture(back, CurlPage.SIDE_FRONT);
                page.setColor(Color.argb(255, 255, 255, 255), CurlPage.SIDE_BACK);
                break;
            }
//            // Third case is images on both sides.
//            case 2: {
//                Log.d("test", "2");
//                Bitmap front = loadBitmap(width, height, 1);
//                Bitmap back = loadBitmap(width, height, 3);
//                page.setTexture(front, CurlPage.SIDE_FRONT);
//                page.setTexture(back, CurlPage.SIDE_BACK);
//                break;
//            }
//            // Fourth case is images on both sides - plus they are blend against
//            // separate colors.
//            case 3: {
//                Log.d("test", "3");
//                Bitmap front = loadBitmap(width, height, 2);
//                Bitmap back = loadBitmap(width, height, 1);
//                page.setTexture(front, CurlPage.SIDE_FRONT);
//                page.setTexture(back, CurlPage.SIDE_BACK);
//                page.setColor(Color.argb(127, 170, 130, 255),
//                        CurlPage.SIDE_FRONT);
//                page.setColor(Color.rgb(255, 190, 150), CurlPage.SIDE_BACK);
//                break;
//            }
//            // Fifth case is same image is assigned to front and back. In this
//            // scenario only one texture is used and shared for both sides.
//            case 4:
//                Log.d("test", "4");
//                Bitmap front = loadBitmap(width, height, 0);
//                page.setTexture(front, CurlPage.SIDE_BOTH);
//                page.setColor(Color.argb(127, 255, 255, 255),
//                        CurlPage.SIDE_BACK);
//                break;
        }
    }

}