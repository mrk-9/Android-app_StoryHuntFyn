package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.RoutePointColumns.RoutePointContentColumns;

/**
 * https://parse.com/apps/historiejagt-fyn/collections#class/RoutePointContent
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_ROUTE_POINT_CONTENT_COLLECTION)
public class RoutePointContentHisContract extends ParseContract {
    private static final String LOG_TAG = RoutePointContentHisContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final String KEY_POINTER_LANGUAGE = "language";
    public static final String KEY_TEXT100 = "text100";
    public static final String KEY_TEXT25 = "text25";
    public static final String KEY_TEXT50 = "text50";
    public static final String KEY_TEXT75 = "text75";

    @Override
    public ParseObject getParseObject(String key) {
        return super.getParseObject(key);
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(RoutePointContentColumns.KEY_LANGUAGE, getParseObject(KEY_POINTER_LANGUAGE).getObjectId());
        values.put(RoutePointContentColumns.KEY_TEXT100, getString(KEY_TEXT100));
        values.put(RoutePointContentColumns.KEY_TEXT25, getString(KEY_TEXT25));
        values.put(RoutePointContentColumns.KEY_TEXT50, getString(KEY_TEXT50));
        values.put(RoutePointContentColumns.KEY_TEXT75, getString(KEY_TEXT75));
    }
}
