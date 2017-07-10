package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseRelation;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.POIEntry;
import org.osmdroid.util.GeoPoint;

/**
 * Parse data model of point of interest
 * URL https://parse.com/apps/historiejagt-fyn/collections#class/PointOfInterest
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_POI_COLLECTION)
public class POIHisContract extends ParseContract {

    private static final String LOG_TAG = POIHisContract.class.getSimpleName();
    public static final String IS_AUTO_RANGE = "ar";
    public static final String AR_RANGE = "arRange";
    public static final String AUTO_RANGE = "autoRange";
    public static final String AUTO_PLAY = "autoplay";
    //click range in meters
    public static final String CLICK_RANGE = "clickRange";
    public static final String COORDINATES = "coordinates";
    public static final String COORDINATES_LAT = "_latitude";
    public static final String COORDINATES_LON = "_longitude";
    public static final String MAP_RANGE = "mapRange";
    public static final String NAME = "name";
    public static final String NO_AVATAR = "noAvatar";
    public static final String NO_AVARAR = "noAvarar";
    public static final String VIDEO_URL = "videoURL";
    public static final String IMAGE = "image";
    public static final String AVATAR = "avatar";
    public static final String AUDIO = "audio";
    public static final String QUIZ = "quiz";
    public static final String UNLOCK_POI = "unlockPOI";
    public static final String PARENT_POI = "parentPOI";
    public static final String POINT_AWARDING = "pointAwarding";
    //image for practical content
    public static final String FACTS_IMAGE = "factsImage";
    //indicates if poi is parent poi
    public static final String PARENT_POINT = "parentPoint";

    // 1 to *
    public static final String CONTENTS = "contents";
    public static final String KEY_PARENT_ROUTE = "route";

    private String routeId;

    public ParseGeoPoint getCoordinates() {
        return getParseGeoPoint("coordinates");
    }

    public GeoPoint getCoordinatesOsm() {
        ParseGeoPoint parseGeoPoint = getCoordinates();

        return new GeoPoint(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
    }

    public int getAutoRange() {
        return getInt(AUTO_RANGE);
    }

    public boolean isAutoPlay() {
        return getBoolean(AUTO_PLAY);
    }

    //click range in meters. Shows how far from the point user must be to be able to view information of the point
    public int getClickRange() {
        return getInt(CLICK_RANGE);
    }

    public int getMapRange() {
        return getInt(MAP_RANGE);
    }

    public String getName() {
        return getString(NAME);
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(POIEntry.COLUMN_IS_AR, getBoolean(IS_AUTO_RANGE));
        values.put(POIEntry.COLUMN_AR_RANGE, getInt(AR_RANGE));
        values.put(POIEntry.COLUMN_AUTO_RANGE, getInt(AUTO_RANGE));
        values.put(POIEntry.COLUMN_AUTO_PLAY, getBoolean(AUTO_PLAY));
        values.put(POIEntry.COLUMN_COORDINATES_LAT, getParseGeoPoint(COORDINATES) == null ? null : getParseGeoPoint(COORDINATES).getLatitude());
        values.put(POIEntry.COLUMN_COORDINATES_LON, getParseGeoPoint(COORDINATES) == null ? null : getParseGeoPoint(COORDINATES).getLongitude());
        values.put(POIEntry.COLUMN_MAP_RANGE, getInt(MAP_RANGE));
        values.put(POIEntry.COLUMN_CLICK_RANGE, getInt(CLICK_RANGE));
        values.put(POIEntry.COLUMN_NAME, getString(NAME));
        values.put(POIEntry.COLUMN_NO_AVATAR, getBoolean(NO_AVATAR));
        values.put(POIEntry.COLUMN_NO_AVARAR, getBoolean(NO_AVARAR));
        values.put(POIEntry.COLUMN_VIDEO_URL, getString(VIDEO_URL));
        values.put(POIEntry.COLUMN_IMAGE, getParseObject(IMAGE) == null ? null : getParseObject(IMAGE).getObjectId());
        values.put(POIEntry.COLUMN_AVATAR, getParseObject(AVATAR) == null ? null : getParseObject(AVATAR).getObjectId());
        values.put(POIEntry.COLUMN_AUDIO, getParseFile(AUDIO) == null ? null : getParseFile(AUDIO).getUrl());
        values.put(POIEntry.COLUMN_QUIZ, getParseObject(QUIZ) == null ? null : getParseObject(QUIZ).getObjectId());
        values.put(POIEntry.COLUMN_UNLOCK_POI, getParseObject(UNLOCK_POI) == null ? null : getParseObject(UNLOCK_POI).getObjectId());
        values.put(POIEntry.COLUMN_PARENT_POI, getParseObject(PARENT_POI) == null ? null : getParseObject(PARENT_POI).getObjectId());
        values.put(POIEntry.COLUMN_POINT_AWARDING, getBoolean(POINT_AWARDING));
        values.put(POIEntry.COLUMN_FACTS_IMAGE, getParseObject(FACTS_IMAGE) == null ? null : getParseObject(FACTS_IMAGE).getObjectId());
        values.put(POIEntry.COLUMN_PARENT_POINT, getBoolean(PARENT_POINT));
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteId() {
        return routeId;
    }

    public ParseRelation<POIConnectionHisContract> getContent() {
        return getRelation(CONTENTS);
    }


    public ImageHisContract getImage() {
        return (ImageHisContract) get(IMAGE);
    }
}
