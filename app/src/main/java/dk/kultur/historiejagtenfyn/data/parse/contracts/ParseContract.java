package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseObject;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;

/**
 * Shares same column name values
 * Created by JustinasK on 2/10/2015.
 */
public abstract class ParseContract extends ParseObject implements ValueCollectible {
    private static final String LOG_TAG = ParseContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final String KEY_OBJECT_ID = "objectId";
    public static final String KEY_CREATED_DATE = "createdAt";
    public static final String KEY_UPDATED_DATE = "updatedAt";
    public static final String POINTER_ID = "_" + KEY_OBJECT_ID;
    public static final String URL = "_url";

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(KEY_OBJECT_ID, getObjectId());
        final String dbDateString = HisContract.getDbDateString(getCreatedAt());
        values.put(KEY_CREATED_DATE, dbDateString);
        final String dbDateString1 = HisContract.getDbDateString(getUpdatedAt());
        values.put(KEY_UPDATED_DATE, dbDateString1);
        fillValues(values);
        return values;
    }

    protected abstract void fillValues(ContentValues values);
}
