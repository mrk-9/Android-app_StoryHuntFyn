package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseRelation;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.RoutePointColumns;

/**
 * https://parse.com/apps/historiejagt-fyn/collections#class/RoutePoint
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_ROUTE_POINT_COLLECTION)
public class RoutePointHisContract extends ParseContract {
    private static final String LOG_TAG = RoutePointHisContract.class.getSimpleName();
    public static final String KEY_RELATION_POI = "pointOfInterests";
    public static final String KEY_RELATION_CONTENT = "contents";
    public static final String KEY_POINTER_ROUTE = "route";
    public static final String KEY_NAME = "name";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(RoutePointColumns.COLUMNS_ROUTE, getParseObject(KEY_POINTER_ROUTE) == null ? null : getParseObject(KEY_POINTER_ROUTE).getObjectId());
        values.put(RoutePointColumns.COLUMNS_NAME, getString(KEY_NAME));
    }

    public ParseRelation<POIHisContract> getPointOfInterestsRelation() {
        return getRelation(KEY_RELATION_POI);
    }

    public RouteHisContract getRoute() {
        return (RouteHisContract) get(KEY_POINTER_ROUTE);
    }
}
