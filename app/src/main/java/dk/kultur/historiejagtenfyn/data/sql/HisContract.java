package dk.kultur.historiejagtenfyn.data.sql;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import dk.kultur.historiejagtenfyn.BuildConfig;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.parse.contracts.InfoHisContract;
import dk.kultur.historiejagtenfyn.data.sql.Columns.*;
import dk.kultur.historiejagtenfyn.data.sql.Columns.POIColumns.POIContentColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.RouteColumns.RouteContentColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.RoutePointColumns.RoutePointContentColumns;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Defines table and column names for the his databaseHelper.
 * Created by JustinasK on 2/10/2015.
 */
public class HisContract {
    private static final String LOG_TAG = HisContract.class.getSimpleName();
    //information from gradle build file
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CLASS = "class";
    public static final String PATH_VIEW = "view";
    public static final String PATH_RELATION = "relation";

    public static final String PATH_JOIN = "join";

    public static final Uri BASE_CONTENT_URI_CLASS = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + PATH_CLASS);
    public static final Uri BASE_CONTENT_URI_FULL_VIEW = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + PATH_VIEW);
    public static final Uri BASE_CONTENT_URI_RELATION = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + PATH_RELATION);

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String PATH_PARSE_COLLECTION = "databaseParse";
    public static final String PATH_LANGUAGE = "language";
    public static final String PATH_INFO = "info";
    public static final String PATH_AVATAR = "avatar";
    public static final String PATH_ICON = "icon";
    public static final String PATH_POI = "pointOfInterest";
    public static final String PATH_POIS = "pointOfInterests";
    public static final String PATH_POI_JOINED_ROUTE_ROUTE_POINT = "pointOfInterestRouteRoutePoint";
    public static final String PATH_POI_CONTENT = "poiContent";
    public static final String PATH_POI_CONNECTION = "pointOfInterestConnection";
    public static final String PATH_ROUTE = "route";
    public static final String PATH_ROUTE_JOINED_ICON_JOINED_ROUTE_CONTENT = "routeJoinedIconJoinedRouteContent";
    public static final String PATH_ROUTE_JOINED_ROUTE_CONTENT = "routeJoinedRouteContent";
    public static final String PATH_ROUTE_POINT = "routePoint";
    public static final String PATH_ROUTE_CONTENT = "routeContent";
    public static final String PATH_ROUTE_POINT_CONTENT = "routePointContent";
    public static final String PATH_ROUTE_POINT_CONTENTS = "routePointContents";

    public static final String PATH_JOIN_POIS_ROUTE = "joinPointOfInterestsRoute";
    public static final String PATH_JOIN_CONTENTS_ROUTE = "joinContentsRoute";
    public static final String PATH_JOIN_POIS_ROUTE_POINT = "joinPointOfInterestsRoutePoint";
    public static final String PATH_JOIN_CONTENTS_ROUTE_POINT = "joinContentsRoutePoint";
    public static final String PATH_JOIN_CONTENTS_POI = "joinContentsPointOfInterest";

    public static final String PATH_JOIN_ANSWERS_QUESTION = "joinAnswersQuestion";
    public static final String PATH_JOIN_CONTENTS_QUIZ = "joinContentsQuiz";
    public static final String PATH_JOIN_QUESTIONS_QUIZ_CONTENT = "joinQuestionsQuizContent";

    public static final String PATH_QUESTION = "question";
    public static final String PATH_QUIZ = "quiz";
    public static final String PATH_QUIZ_CONTENT = "quizContent";
    public static final String PATH_ANSWER = "answer";

    public static final String PATH_QUESTIONS = "questions";
    public static final String PATH_QUIZ_CONTENTS = "quizContents";
    public static final String PATH_ANSWERS = "answers";

    public static final String PATH_POI_JOINED_DESTINATION = "poi_route_routepoint_destination";
    private static final String VND_CURSOR_DIR = "vnd.android.cursor.dir/";
    private static final String VND_CURSOR_ITEM = "vnd.android.cursor.item/";


    /**
     * Converts Date class to a string representation, used for easy comparison and databaseHelper lookup.
     *
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    public static String getDbDateString(Date date) {
        //API returns date in local timezone
        //we need to save in GMT+0
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * Return an ISO 8601 combined date and time string for specified date/time
     *
     * @param date
     *            Date
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    private static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    /**
     * Converts a dateText to a long Unix time representation
     *
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDbIso(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
        try {
            return dbDateFormat.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a dateText to a long Unix time representation
     *
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.UK);
        try {
            return dbDateFormat.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final class LanguageEntry implements BaseColumns, LanguageColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LANGUAGE).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_LANGUAGE;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_LANGUAGE;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_LANGUAGE_COLLECTION;

        public static Uri buildLanguageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildLanguageUri(String parseId) {
            return CONTENT_URI.buildUpon().appendPath(parseId).build();
        }
    }

    public static final class InfoEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_INFO).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_INFO;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_INFO;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_INFO_COLLECTION;

        public static Uri buildInfoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildInfoUri(String objectId) {
            return CONTENT_URI.buildUpon().appendPath(objectId).build();
        }

        public static Uri buildInfoWithLanguage(String languageObjectId) {
            return CONTENT_URI.buildUpon().appendQueryParameter(InfoHisContract.KEY_LANGUAGE, languageObjectId).build();
        }
    }

    public static final class RouteEntry implements BaseColumns, ParseColumns, RouteColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ROUTE;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_ROUTE_COLLECTION;

        public static Uri buildRouteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRouteUri(String parseId) {
            return CONTENT_URI.buildUpon().appendPath(parseId).build();
        }

        public static String getParseId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class RouteJoinedRouteContentEntry implements RouteColumns, RouteContentColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_JOINED_ROUTE_CONTENT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_JOINED_ROUTE_CONTENT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ROUTE_JOINED_ROUTE_CONTENT;

    }

    public static final class RoutePointEntry implements BaseColumns, ParseColumns, RoutePointColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_POINT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_POINT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ROUTE_POINT;

        public static final String TABLE_NAME = ParserApiHis.KEY_ROUTE_POINT_COLLECTION;

        public static Uri buildRoutePointUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRoutePointUri(String objectId) {
            return CONTENT_URI.buildUpon().appendPath(objectId).build();
        }

        public static String getObjectId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class RouteContentEntry implements BaseColumns, RouteContentColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_CONTENT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_CONTENT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ROUTE_CONTENT;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_ROUTE_CONTENT_COLLECTION;

        public static Uri buildRouteContentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRouteContentUri(String parseId) {
            return CONTENT_URI.buildUpon().appendPath(parseId).build();
        }
    }

    public static final class RoutePointContentEntry implements BaseColumns, RoutePointColumns.RoutePointContentColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_POINT_CONTENT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_POINT_CONTENT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ROUTE_POINT_CONTENT;

        public static final String TABLE_NAME = ParserApiHis.KEY_ROUTE_POINT_CONTENT_COLLECTION;

        public static Uri buildRoutePointContentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRoutePointContentUri(String objectId) {
            return CONTENT_URI.buildUpon().appendPath(objectId).build();
        }

        public static String getObjectId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class POIEntry implements BaseColumns, POIColumns, ParseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POI).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_POI;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_POI;

        public static final String TABLE_NAME = ParserApiHis.KEY_POI_COLLECTION;
        public static String KEY_PARAMETER_LAT = "latitude";
        public static String KEY_PARAMETER_LON = "longitude";
        public static final String KEY_RADIUS_IN_DMS = "queryRadiusDms";

        public static Uri buildPOIUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPOIUri(String objectId) {
            return CONTENT_URI.buildUpon().appendPath(objectId).build();
        }

        public static Uri buildPoiByPositionAndRange(double latitude, double longitude, float queryRadiusDms) {
            return CONTENT_URI.buildUpon().
                    appendQueryParameter(KEY_PARAMETER_LAT, String.valueOf(latitude)).
                    appendQueryParameter(KEY_PARAMETER_LON, String.valueOf(longitude)).
                    appendQueryParameter(KEY_RADIUS_IN_DMS, String.valueOf(queryRadiusDms)).build();
        }

        public static Uri buildPointWithRoad(String routeObjectId) {
            return CONTENT_URI.buildUpon().appendPath(routeObjectId).build();
        }

        public static String getRouteFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getParseId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class POIJoinedRoute implements BaseColumns, ParseColumns, POIColumns, RouteColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI_RELATION.buildUpon().appendPath(PATH_POI).appendPath(PATH_JOIN).appendPath(PATH_ROUTE).build();

        public static Uri buildByPositionAndRange(double latitude, double longitude, float queryRadiusDms) {
            return CONTENT_URI.buildUpon().
                    appendQueryParameter(POIEntry.KEY_PARAMETER_LAT, String.valueOf(latitude)).
                    appendQueryParameter(POIEntry.KEY_PARAMETER_LON, String.valueOf(longitude)).
                    appendQueryParameter(POIEntry.KEY_RADIUS_IN_DMS, String.valueOf(queryRadiusDms)).build();
        }
    }

    public static final class POIFullJoinedEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POI_JOINED_ROUTE_ROUTE_POINT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_POI_JOINED_ROUTE_ROUTE_POINT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_POI_JOINED_ROUTE_ROUTE_POINT;

        public static final String POI_OBJECT_ID = "poi_objectId";
        public static final String ROUTE_OBJECT_ID = "route_objectId";
        public static final String ROUTE_POINT_OBJECT_ID = "route_point_objectId";
    }

    public static final class POIContentJoinedPOIEntry implements POIColumns, POIContentColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RELATION).build();

        public static final String CONTENT_TYPE = VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_RELATION + "/" + PATH_POI_CONTENT + "/ " + PATH_POI;

        public static final String POI_OBJECT_ID = "poi_objectId";
        public static final String ROUTE_OBJECT_ID = "route_objectId";
        public static final String ROUTE_POINT_OBJECT_ID = "route_point_objectId";

        /**
         * Return full info about poi ant poi content
         *
         * @param poiId
         * @return
         */
        public static Uri buildPoiContentOfPoi(String poiId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_POI_CONTENT).appendPath(poiId).appendPath(PATH_POI).build();
        }

        public static String getPOIId(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class POIJoinedRoutePointEntry implements POIColumns, POIContentColumns, RoutePointColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_RELATION).build();

        public static final String CONTENT_TYPE = VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_RELATION + "/" + PATH_POI + "/ " + PATH_ROUTE_POINT;

        public static Uri buildJoinedPoiPoiContentRoutePoint(String poiId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_POI).appendPath(poiId).appendPath(PATH_ROUTE_POINT).build();
        }

        public static String getPOIId(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }


    public static final class RouteJoinedIconJoinedContentEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_JOINED_ICON_JOINED_ROUTE_CONTENT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_JOINED_ICON_JOINED_ROUTE_CONTENT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ROUTE_JOINED_ICON_JOINED_ROUTE_CONTENT;
    }

    public static final class POIConnectionEntry implements  POIConnectionColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POI_CONNECTION).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_POI_CONNECTION;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_POI_CONNECTION;

        public static final String TABLE_NAME = ParserApiHis.KEY_POI_CONNECTION_COLLECTION;

        public static Uri buildPOIConnectionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPOIConnectionUri(String objectId) {
            return CONTENT_URI.buildUpon().appendPath(objectId).build();
        }
    }

    public static final class POIContentEntry implements BaseColumns, POIContentColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POI_CONTENT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_POI_CONTENT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_POI_CONTENT;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_POI_CONTENT_COLLECTION;

        public static Uri buildPOIContentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPOIContentUri(String parseId) {
            return CONTENT_URI.buildUpon().appendPath(parseId).build();
        }

        public static String getParseId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class ParseCollectionsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PARSE_COLLECTION).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_PARSE_COLLECTION;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_PARSE_COLLECTION;

        // Table name
        public static final String TABLE_NAME = "DatabaseParse";

        // Table name
        public static final String KEY_COLLECTION = "collection";
        // Table name
        public static final String KEY_PARSE_OBJECT_KEY = "object_key";

        public static Uri buildParseContentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class AvatarEntry implements BaseColumns, ParseColumns {
        public static final String COLUMN_AVATAR = "avatar";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AVATAR).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_AVATAR;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_AVATAR;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_AVATAR_COLLECTION;

        public static Uri buildAvatarUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildAvatarUri(String objectId) {
            return CONTENT_URI.buildUpon().appendPath(objectId).build();
        }

        public static String getAvatarId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class IconEntry implements BaseColumns, IconColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ICON).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ICON;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_ICON;

        // Table name
        public static final String TABLE_NAME = ParserApiHis.KEY_ICON_COLLECTION;

        public static Uri buildIconUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildIconUri(String parseId) {
            return CONTENT_URI.buildUpon().appendPath(parseId).build();
        }
    }

    public static final class JoinPOIsRouteEntry extends JoinEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOIN_POIS_ROUTE).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_JOIN_POIS_ROUTE;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_JOIN_POIS_ROUTE;

        // Table name
        public static final String TABLE_NAME = "_Join_pointOfInterests_Route";

        public static Uri buildJRoutePOIUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildJRoutePOIUri(String owningId, String relatedId) {
            return CONTENT_URI.buildUpon().appendPath(owningId).appendPath(relatedId).build();
        }

        public static String getRouteFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getPointFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class JoinContentsRouteEntry extends JoinEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOIN_CONTENTS_ROUTE).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_JOIN_CONTENTS_ROUTE;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_JOIN_CONTENTS_ROUTE;

        // Table name
        public static final String TABLE_NAME = "_Join_contents_Route";

        public static Uri buildRouteContentsWithRouteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildRouteContentsWithRouteUri(String owningId, String relatedId) {
            return CONTENT_URI.buildUpon().appendPath(owningId).appendPath(relatedId).build();
        }

        public static String getRouteFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getContentFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class JoinContentsRoutePointEntry extends JoinEntry implements BaseColumns {
        // Table name
        public static final String TABLE_NAME = "_Join_contents_RoutePoint";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOIN_CONTENTS_ROUTE_POINT).build();

        public static final String CONTENT_TYPE = VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_JOIN_CONTENTS_ROUTE_POINT;
        public static final String CONTENT_ITEM_TYPE = VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_JOIN_CONTENTS_ROUTE_POINT;

        public static Uri buildJContentsRoutePointUri(String owningId, String relatedId) {
            return CONTENT_URI.buildUpon().appendPath(owningId).appendPath(relatedId).build();
        }

        public static Uri buildJContentsRoutepointUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class POIsOfRoutePointRelation extends RelationEntry implements BaseColumns, POIColumns, RoutePointColumns {
        public static final String CONTENT_TYPE = VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_POINT + "/" + PATH_POIS;

        public static Uri buildRelationUri(String owningId) {
            return BASE_CONTENT_URI_RELATION.buildUpon().appendPath(PATH_ROUTE_POINT).appendPath(owningId).appendPath(PATH_POIS).build();
        }
    }

    public static final class RoutePointContentsOfRoutePointRelation extends RelationEntry implements BaseColumns, RoutePointColumns, RoutePointContentColumns {
        public static final String CONTENT_TYPE = VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_ROUTE_POINT + "/" + PATH_ROUTE_POINT_CONTENTS;

        public static Uri buildRelationUri(String owningId) {
            return BASE_CONTENT_URI_RELATION.buildUpon().appendPath(PATH_ROUTE_POINT).appendPath(owningId).appendPath(PATH_ROUTE_POINT_CONTENTS).build();
        }
    }

    /**
     *
     */
    public static final class JoinPOIsRoutePointEntry extends JoinEntry implements BaseColumns, RoutePointColumns, POIColumns {
        // Table name
        public static final String TABLE_NAME = "_Join_pointOfInterests_RoutePoint";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOIN_POIS_ROUTE_POINT).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_JOIN_POIS_ROUTE_POINT;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_JOIN_POIS_ROUTE_POINT;

        public static Uri buildJContentsRoutePointUri(String owningId, String relatedId) {
            return CONTENT_URI.buildUpon().appendPath(owningId).appendPath(relatedId).build();
        }

        public static Uri buildJPOIsRoutePointsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class JoinPOIContentsPOIEntry extends JoinEntry implements BaseColumns {
        // Table name
        public static final String TABLE_NAME = "_Join_contents_pointOfInterest";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOIN_CONTENTS_POI).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_JOIN_CONTENTS_POI;
        public static final String CONTENT_ITEM_TYPE =
                VND_CURSOR_ITEM + CONTENT_AUTHORITY + "/" + PATH_JOIN_CONTENTS_POI;

        public static Uri buildJContentsPOIUri(String owningId, String relatedId) {
            return CONTENT_URI.buildUpon().appendPath(owningId).appendPath(relatedId).build();
        }

        public static Uri buildJContentsPOIUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * defines many to one relationship. 1 - owner, * - related for inserting and deleting data
     */
    public static class JoinEntry implements BaseColumns, JoinColumns {

        public static ContentValues getArtificialValues(String source, String destination) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_OWNER, source);
            values.put(COLUMN_RELATED, destination);
            return values;
        }

        public static void fillRelation(ArrayList<ContentValues> values, String owning, String related) {
            final ContentValues pair = new ContentValues();
            pair.put(COLUMN_OWNER, owning);
            pair.put(COLUMN_RELATED, related);
            values.add(pair);
        }

        public static String getOwningFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getRelatedFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static final class POIDestinationEntry implements BaseColumns, ParseColumns, RouteColumns, RoutePointColumns, POIColumns {

        public static final String TABLE_NAME = "poi_source";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POI_JOINED_DESTINATION).build();

        public static final String CONTENT_TYPE =
                VND_CURSOR_DIR + CONTENT_AUTHORITY + "/" + PATH_POI_JOINED_DESTINATION;
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    /**
     * Relation entry from two tables with 1 to * relation. fixed to "owner/{id}/related" URI
     */
    public static class RelationEntry {
        public static String getOwnerId(Uri relatedId) {
            return relatedId.getPathSegments().get(2);
        }
    }
}
