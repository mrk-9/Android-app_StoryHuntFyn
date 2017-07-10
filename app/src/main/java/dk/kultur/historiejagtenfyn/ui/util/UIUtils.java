package dk.kultur.historiejagtenfyn.ui.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.InputStream;
import java.util.Locale;

import dk.kultur.historiejagtenfyn.HistoriejagtenfynApplication;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.parse.contracts.LanguageHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.LanguageEntry;

public class UIUtils {

    //private static Handler mHandler = new Handler();

    public static Typeface getTypeFaceMarkerFelt(Context c) {
        return ((HistoriejagtenfynApplication) c.getApplicationContext()).getTypeface("fonts/markerfelt.ttf");
    }


    public static Typeface getTypeFaceTidyHand(Context c) {
        return ((HistoriejagtenfynApplication) c.getApplicationContext()).getTypeface("fonts/tidyhand.ttf");
    }

    /**
     * @param activity activity
     */
    public static void showProgressBar(final Activity activity) {
        if (activity == null) {
            return;
        }
        View child = activity.getLayoutInflater().inflate(R.layout.view_progress_bar, null);
        activity.addContentView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        child.findViewById(R.id.containerProgressBar).setTag(System.currentTimeMillis());
    }

    public static void hideProgressBar(final Activity activity) {
        if (activity == null) {
            return;
        }
        View v = activity.findViewById(R.id.containerProgressBar);
        if (v != null) {
            long time = (Long) v.getTag();
            long timeLeft = time + 1000 - System.currentTimeMillis();

            if (timeLeft < 0) {
                timeLeft = 0;
            }

            Handler handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    FrameLayout rootLayout = (FrameLayout) activity.findViewById(android.R.id.content);
                    View loadingView = rootLayout.findViewById(R.id.containerProgressBar);
                    if (loadingView != null) {
                        rootLayout.removeView(loadingView);
                    }
                    loadingView = null;
                }
            };
            handler.sendEmptyMessageDelayed(0, timeLeft);
        }
    }

    public static String getActiveLanguage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_active_language), context.getString(R.string.pref_language_default));
    }

    @NonNull
    public static String cacheActiveLanguage(Context context, String languageId) {
        if (languageId == null) {
            return getActiveLanguage(context);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(context.getString(R.string.pref_active_language), languageId).apply();
        return languageId;
    }

    public static boolean isCompleted(Context application) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        return prefs.getBoolean(application.getString(R.string.pref_data_synced), false);
    }

    public static void saveCompleted(Context application) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        prefs.edit().putBoolean(application.getString(R.string.pref_data_synced), true).apply();
    }

    /**
     * @param applicationContext app
     * @return best match of active language
     */
    public static String getActiveLanguageCodeFromProvider(Context applicationContext) {
        String activeUserLanguage = null;
        final String[] projection = {LanguageEntry.COLUMN_OBJECT_ID, LanguageEntry.COLUMN_CODE};
        String code = Locale.getDefault().getLanguage().toUpperCase();
        final Cursor cursorLanguage = applicationContext.getContentResolver().query(LanguageEntry.CONTENT_URI,  // Table to Query
                projection, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                LanguageHisContract.KEY_PRIORITY + " DESC LIMIT 1" // columns to group by

        );
        Log.d("UIUtils", "cursor size 1 " + cursorLanguage.getCount());
        if (cursorLanguage.getCount() == 0) {
            return null;
        }
        while (cursorLanguage.moveToNext()) {
            if (cursorLanguage.getString(cursorLanguage.getColumnIndex(LanguageEntry.COLUMN_CODE)).equals(code)) {
                activeUserLanguage = cursorLanguage.getString(cursorLanguage.getColumnIndex(LanguageEntry.COLUMN_OBJECT_ID));
                break;
            }
        }
        Log.d("UIUtils", "cursor size 2 " + cursorLanguage.getCount());
        //Take first highest prior language
        if (activeUserLanguage == null && cursorLanguage.getCount()>0) {
            cursorLanguage.moveToFirst();
            activeUserLanguage = cursorLanguage.getString(cursorLanguage.getColumnIndex(LanguageEntry.COLUMN_OBJECT_ID));
        }
        cursorLanguage.close();
        UIUtils.assertNonNull(activeUserLanguage, "No active language found");
        return activeUserLanguage;
    }

    static
    @NonNull
    <T> T assertNonNull(@Nullable T value, @Nullable String msg) {
        if (value == null) throw new AssertionError(msg);
        return value;
    }

    /**
     * Saves best match language from provider to shared preferences
     *
     * @param applicationContext app
     * @return best match language from provider to shared preferences
     */
    public static String cacheActiveLanguageFromProvider(Context applicationContext) {
        String languageId = getActiveLanguageCodeFromProvider(applicationContext);
        return cacheActiveLanguage(applicationContext, languageId);
    }

    public static Bitmap createScaledBitmap(@NonNull Bitmap bitmap, @NonNull Activity activity, int maxHeight) {
        final float density = activity.getResources().getDisplayMetrics().density; //density
        final float retinaMultilayer = density * 0.5f; //adjust to retina pic
        final float scaledMultilayer = getScreenSizeModifier(activity) * retinaMultilayer; //adjust screen size
        int targetedHeight = maxHeight;
        while (targetedHeight > 0 && targetedHeight < bitmap.getHeight()) {
            targetedHeight /= 2;
        }

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaledMultilayer, scaledMultilayer);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    public static Bitmap createScaledBitmap(@NonNull Bitmap bitmap, @NonNull Activity activity) {
        return createScaledBitmap(bitmap, activity, -1);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Options getOptions(InputStream bitmapInputStream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(bitmapInputStream, null, options);
        return options;
    }

    public static Bitmap decodeSampledBitmapFromResource(InputStream is, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * Screen size adjustments
     *
     * @return screen size modifier
     */
    private static float getScreenSizeModifier(@NonNull Context context) {
        if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            return 4f;
        } else if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            return 2f;
        }
        return 1f;
    }

    /**
     * Will disable view for short amount of time. Should be used to prevent double tap issue on android.
     *
     * @param view - view to be disabled
     */
    /*public static void disableView(final View view) {
        view.setEnabled(false);

        Runnable enableViewRunnable = new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        };
        mHandler.postDelayed(enableViewRunnable, 500);
    }*/

}
