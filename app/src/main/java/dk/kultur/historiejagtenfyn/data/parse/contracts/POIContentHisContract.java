package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.POIColumns.POIContentColumns;

/**
 * Point Of Interest Content info
 * https://parse.com/apps/historiejagt-fyn/collections#class/POIContent
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_POI_CONTENT_COLLECTION)
public class POIContentHisContract extends ParseContract {
    private static final String LOG_TAG = POIContentHisContract.class.getSimpleName();
    public static final String KEY_FACTS = "facts";
    public static final String KEY_IMAGE_TITLE = "imageTitle";
    public static final String KEY_INFO = "info";
    public static final String KEY_POINTER_LANGUAGE = "language"; //pointer
    public static final String KEY_NAME = "name";
    public static final String KEY_VIDEO_TITLE = "videoTitle";
    public static final String KEY_FACTS_IMAGE_TITLE = "factsImageTitle";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(POIContentColumns.COLUMN_LANGUAGE_ID, getParseObject(KEY_POINTER_LANGUAGE) == null ? null : getParseObject(KEY_POINTER_LANGUAGE).getObjectId());
        values.put(POIContentColumns.COLUMN_FACTS, getString(KEY_FACTS));
        values.put(POIContentColumns.COLUMN_IMAGE_TITLE, getString(KEY_IMAGE_TITLE));
        values.put(POIContentColumns.COLUMN_INFO, getString(KEY_INFO));
        values.put(POIContentColumns.COLUMN_NAME, getString(KEY_NAME));
        values.put(POIContentColumns.COLUMN_VIDEO_TITLE, getString(KEY_VIDEO_TITLE));
        values.put(POIContentColumns.COLUMN_FACTS_IMAGE_TITLE, getString(KEY_FACTS_IMAGE_TITLE));
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public String getInfo() {
        return getString(KEY_INFO);
    }

    public String getPractical() {
        return getString(KEY_FACTS);
    }

}
