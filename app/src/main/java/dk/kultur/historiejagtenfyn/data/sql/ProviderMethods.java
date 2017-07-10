package dk.kultur.historiejagtenfyn.data.sql;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.*;
import dk.kultur.historiejagtenfyn.data.sql.contracts.QuizContracts.QuizEntry;
import dk.kultur.historiejagtenfyn.data.sql.contracts.QuizContracts.QuizEntry.QuizViewEntry;

/**
 * Methods
 * Created by JustinasK on 2/24/2015.
 */
public class ProviderMethods {
    private static final String LOG_TAG = ProviderMethods.class.getSimpleName();
    private static final String sPoiSelection = POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_OBJECT_ID + " = ?";

    public static String getLogTag() {
        return LOG_TAG;
    }

    /**
     * Returns joined POI and RoutePoint tables
     *
     * @param context    app
     * @param projection column names with table names
     * @param poiId      POI parse object id
     * @return cursor loader
     */
    public static CursorLoader getRoutePointEntry(@NonNull Context context, @Nullable String[] projection, @Nullable String poiId, @Nullable String sortOrder) {
        String[] selectionArgs = new String[]{poiId};
        return new CursorLoader(context, JoinPOIsRoutePointEntry.CONTENT_URI, projection, sPoiSelection, selectionArgs, sortOrder);
    }

    /**
     * Returns joined Route and POI tables
     *
     * @param context    app
     * @param projection column names with table names
     * @param poiId      POI parse object id
     * @return cursor loader
     */
    public static CursorLoader getRouteEntryByPOI(@NonNull Context context, @Nullable String[] projection, @Nullable String poiId, @Nullable String sortOrder) {
        String[] selectionArgs = new String[]{poiId};
        return new CursorLoader(context, POIJoinedRoute.CONTENT_URI, projection, sPoiSelection, selectionArgs, sortOrder);
    }


    /**
     * Returns POI entry
     *
     * @param context    app
     * @param projection column names with table names
     * @param poiId      POI parse object id
     * @return cursor loader
     */
    public static CursorLoader getPointEntry(@NonNull Context context, @Nullable String[] projection, @Nullable String poiId, @Nullable String sortOrder) {
        String[] selectionArgs = new String[]{poiId};
        return new CursorLoader(context, POIEntry.CONTENT_URI, projection, sPoiSelection, selectionArgs, sortOrder);
    }

    /**
     * Returns POI content entry
     *
     * @param context    app
     * @param projection column names with table names
     * @param poiId      POI parse object id
     * @return cursor loader
     */
    public static CursorLoader getPointContentEntry(@NonNull Context context, @Nullable String[] projection, @Nullable String poiId, @Nullable String sortOrder) {
        return new CursorLoader(context, POIContentJoinedPOIEntry.buildPoiContentOfPoi(poiId), projection, null, null, sortOrder);
    }

    /**
     * Returns POI content entry
     *
     * @param context    app
     * @param projection column names with table names
     * @param poiId      POI parse object id
     * @return cursor loader
     */
    public static CursorLoader getPoinEntry(@NonNull Context context, @Nullable String[] projection, @Nullable String poiId, @Nullable String sortOrder) {
        return new CursorLoader(context, POIEntry.buildPOIUri(poiId), projection, null, null, sortOrder);
    }


    /**
     * Returns POI and POIDestination entry
     *
     * @param context    app
     * @param projection projection from two tables, first POIEntry.TABLE_NAME, second POIDestinationEntry.TABLE_NAME
     *                   POIEntry.TABLE_NAME + "." + POIEntry.COLUMN_COORDINATES_LAT, //first table
     *                   POIDestinationEntry.TABLE_NAME + "." + POIDestinationEntry.COLUMN_COORDINATES_LAT}; //second table
     * @return cursor loader
     */
    public static CursorLoader getPoinWithDestinationEntry(@NonNull Context context, @Nullable String[] projection, @Nullable String sortOrder) {
        return new CursorLoader(context, POIDestinationEntry.CONTENT_URI, projection, null, null, sortOrder);
    }


    /**
     * Return all data view of quiz
     *
     * @param context    app
     * @param projection tables
     * @param quizId     id
     * @param sortOrder  order
     * @return flatten quiz view
     */
    public static CursorLoader getQuizView(@NonNull Context context, @Nullable String[] projection, @Nullable String quizId, @Nullable String sortOrder) {
        String selection = QuizEntry.TABLE_NAME + "." + QuizEntry.COLUMN_OBJECT_ID + " = ? ";
        return new CursorLoader(context, QuizViewEntry.CONTENT_URI, projection, selection, new String[]{quizId}, sortOrder);
    }

    /**
     * Return all poi of route point
     *
     * @param context    app
     * @param projection tables
     * @param routeId    objectId
     * @param sortOrder  order
     * @return flatten quiz view
     */
    public static CursorLoader getPoisOfRoute(@NonNull Context context, @Nullable String routeId, @Nullable String[] projection, @NonNull String selection, @NonNull String[] selectionArg, @Nullable String sortOrder) {
        return new CursorLoader(context, POIsOfRoutePointRelation.buildRelationUri(routeId), projection, selection, selectionArg, sortOrder);
    }

    /**
     * Return all data view of quiz
     *
     * @param context    app
     * @param projection tables
     * @param routeId    objectId
     * @param sortOrder  order
     * @return flatten quiz view
     */
    public static CursorLoader getRoutePointContentsOfRoutePoint(@NonNull Context context, @Nullable String routeId, @Nullable String[] projection, @NonNull String selection, @NonNull String[] selectionArg, @Nullable String sortOrder) {
        return new CursorLoader(context, RoutePointContentsOfRoutePointRelation.buildRelationUri(routeId), projection, selection, selectionArg, sortOrder);
    }

}
