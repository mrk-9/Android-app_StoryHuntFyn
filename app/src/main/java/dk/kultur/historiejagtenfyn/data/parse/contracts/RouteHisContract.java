package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import java.util.List;

/**
 * Route model
 * https://parse.com/apps/historiejagt-fyn/collections#class/Route
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_ROUTE_COLLECTION)
public class RouteHisContract extends ParseContract {
    private static final String LOG_TAG = RouteHisContract.class.getSimpleName();
    public static final String KEY_POINT_OF_INTERESTS = "pointOfInterests";
    public static final String KEY_CONTENTS = "contents";

    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_CENTER_COORDINATES = "centerCoordinates";
    public static final String KEY_CENTER_COORDINATES_COLUMN_LAT = "_latitude";
    public static final String KEY_CENTER_COORDINATES_COLUMN_LON = "_longitude";
    public static final String KEY_ICON = "icon";
    public static final String KEY_NAME = "name";

    //parsing
    public static final String KEY_UPDATE_ROUTE = "route";

    public ParseGeoPoint getCenterCoordinates() {
        return getParseGeoPoint(KEY_CENTER_COORDINATES);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public ParseRelation<POIHisContract> getPointOfInterestsRelation() {
        return getRelation(KEY_POINT_OF_INTERESTS);
    }

    public List<POIHisContract> getPOIList() {
        return getList(KEY_POINT_OF_INTERESTS);
    }

    public ParseObject getIcon() {
        return getParseObject(KEY_ICON);
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(KEY_AVATAR + POINTER_ID, getParseObject(KEY_AVATAR).getObjectId());
        values.put(KEY_CENTER_COORDINATES + KEY_CENTER_COORDINATES_COLUMN_LAT, getParseGeoPoint(KEY_CENTER_COORDINATES).getLatitude());
        values.put(KEY_CENTER_COORDINATES + KEY_CENTER_COORDINATES_COLUMN_LON, getParseGeoPoint(KEY_CENTER_COORDINATES).getLongitude());
        values.put(KEY_ICON + POINTER_ID, getParseObject(KEY_ICON).getObjectId());
        values.put(KEY_NAME, getString(KEY_NAME));
    }


}
