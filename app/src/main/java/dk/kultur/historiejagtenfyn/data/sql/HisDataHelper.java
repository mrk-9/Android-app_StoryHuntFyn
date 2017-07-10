package dk.kultur.historiejagtenfyn.data.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Database helper
 * Created by JustinasK on 2/10/2015.
 */
public class HisDataHelper extends SQLiteAssetHelper {
    private static final String LOG_TAG = HisDataHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "kib_data.db";
    private static final int DATABASE_VERSION = 2;

    public HisDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onupgrade readonly " + db.isReadOnly());

        if (newVersion == 2 && oldVersion == 1) {
            //SQLiteDatabase wdb = getWritableDatabase();
            try {
                db.execSQL("ALTER TABLE " + HisContract.POIContentEntry.TABLE_NAME + " ADD COLUMN " +
                        HisContract.POIContentEntry.COLUMN_FACTS_IMAGE_TITLE + " TEXT");
                db.execSQL("ALTER TABLE " + HisContract.POIEntry.TABLE_NAME + " ADD COLUMN " +
                        HisContract.POIEntry.COLUMN_PARENT_POINT + " INTEGER");
                db.execSQL("ALTER TABLE " + HisContract.POIEntry.TABLE_NAME + " ADD COLUMN " +
                        HisContract.POIEntry.COLUMN_FACTS_IMAGE + " TEXT");

            } catch (Exception e) {
                Log.d(LOG_TAG, "column already exists exception", e);
            }
        }


    }
}
