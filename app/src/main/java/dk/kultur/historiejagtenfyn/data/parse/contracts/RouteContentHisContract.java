package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;

/**
 * https://parse.com/apps/historiejagt-fyn/collections#class/RouteContent
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_ROUTE_CONTENT_COLLECTION)
public class RouteContentHisContract extends ParseContract {
    private static final String LOG_TAG = RouteContentHisContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(KEY_language + POINTER_ID, getParseObject(KEY_language).getObjectId());
        values.put(KEY_info, getString(KEY_info));
        values.put(KEY_name, getString(KEY_name));
    }

    public static final String KEY_info = "info";
    //pointer
    public static final String KEY_language = "language";
    public static final String KEY_name = "name";
}
