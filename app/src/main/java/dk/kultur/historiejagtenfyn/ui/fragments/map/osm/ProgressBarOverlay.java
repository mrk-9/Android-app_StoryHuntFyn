package dk.kultur.historiejagtenfyn.ui.fragments.map.osm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.lang.reflect.Field;

import dk.kultur.historiejagtenfyn.R;

/**
 * Created by Lina on 2015.02.17
 */
public class ProgressBarOverlay extends Overlay {

    // ===========================================================
    // Fields
    // ===========================================================

    private static final Rect sTextBoundsRect = new Rect();



    // Defaults
    int xOffset = 10;
    int yOffset = 10;
    int rightMargin = 0;
    int width = 0;
    int height = 0;

    // Internal

    private final Context context;

    protected final RectF progressBarRect = new RectF();

    public float xdpi;
    public float ydpi;
    public int screenWidth;
    public int screenHeight;

    private final ResourceProxy resourceProxy;
    private Paint barPaint;
    private Paint borderPaint;
    private Paint bgPaint;
    private float ratio = 0.01f;
    //private Paint textPaint;

    private boolean adjustLength = false;
    //private float maxLength;

    // ===========================================================
    // Constructors
    // ===========================================================

    public ProgressBarOverlay(final Context context) {
        this(context, new DefaultResourceProxyImpl(context));
    }

    public ProgressBarOverlay(final Context context, final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        this.resourceProxy = pResourceProxy;
        this.context = context;
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        this.barPaint = new Paint();
        this.barPaint.setColor(context.getResources().getColor(R.color.route_color));
        this.barPaint.setAntiAlias(true);
        this.barPaint.setStyle(Paint.Style.FILL);

        this.bgPaint = new Paint();
        this.bgPaint.setColor(context.getResources().getColor(android.R.color.white));
        this.bgPaint.setAntiAlias(true);
        this.bgPaint.setStyle(Paint.Style.FILL);

        this.borderPaint = new Paint();
        this.borderPaint.setColor(context.getResources().getColor(R.color.progress_border_color));
        this.borderPaint.setAntiAlias(true);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        //this.barPaint.setAlpha(255);
        this.borderPaint.setStrokeWidth(2 * dm.density);

        xOffset = context.getResources().getDimensionPixelSize(R.dimen.bg_left_margin);
        yOffset = context.getResources().getDimensionPixelOffset(R.dimen.bg_top_margin);
        rightMargin = context.getResources().getDimensionPixelOffset(R.dimen.bg_right_margin);
        height = context.getResources().getDimensionPixelOffset(R.dimen.progress_bar_height);

        this.xdpi = dm.xdpi;
        this.ydpi = dm.ydpi;

        this.screenWidth = dm.widthPixels;
        this.screenHeight = dm.heightPixels;

        width = screenWidth - xOffset - rightMargin;

        //Log.d("ProgressBar", "width " + width + " h " +height);

        // DPI corrections for specific models
        String manufacturer = null;
        try {
            final Field field = android.os.Build.class.getField("MANUFACTURER");
            manufacturer = (String) field.get(null);
        } catch (final Exception ignore) {
        }

        if ("motorola".equals(manufacturer) && "DROIDX".equals(android.os.Build.MODEL)) {

            // If the screen is rotated, flip the x and y dpi values
            WindowManager windowManager = (WindowManager) this.context
                    .getSystemService(Context.WINDOW_SERVICE);
            if (windowManager.getDefaultDisplay().getOrientation() > 0) {
                this.xdpi = (float) (this.screenWidth / 3.75);
                this.ydpi = (float) (this.screenHeight / 2.1);
            } else {
                this.xdpi = (float) (this.screenWidth / 2.1);
                this.ydpi = (float) (this.screenHeight / 3.75);
            }

        } else if ("motorola".equals(manufacturer) && "Droid".equals(android.os.Build.MODEL)) {
            // http://www.mail-archive.com/android-developers@googlegroups.com/msg109497.html
            this.xdpi = 264;
            this.ydpi = 264;
        }

        // set default max length to 1 inch
        //maxLength = 2.54f;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================


    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected void draw(Canvas c, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }

        // If map view is animating, don't update, scale will be wrong.
        if (mapView.isAnimating()) {
            return;
        }

            final Projection projection = mapView.getProjection();

            if (projection == null) {
                return;
            }

            final IGeoPoint center = projection.fromPixels(screenWidth / 2, screenHeight / 2, null);


            int offsetX = (int) xOffset;
            int offsetY = (int) yOffset;

            c.save();
            c.concat(projection.getInvertedScaleRotateCanvasMatrix());
            c.translate(offsetX, offsetY);

            progressBarRect.set(0, 0, width, height);

                //c.drawRect(progressBarRect, bgPaint);
        c.drawRoundRect(progressBarRect, 10.0f, 10.0f, bgPaint);
        c.drawRoundRect(progressBarRect, 10.0f, 10.0f, borderPaint);
        RectF clipRect = new RectF();
        //Log.d("ProgressBarOverlay", "ratio " + ratio);
        clipRect.set(0, 0, width*ratio, height);
        c.clipRect(clipRect);
        c.drawRoundRect(progressBarRect, 10.0f, 10.0f, barPaint);

            c.restore();

    }

    //visited RoutePoint poi count devided by all RoutePoint poi count
    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}
