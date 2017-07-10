package dk.kultur.historiejagtenfyn.ui.fragments.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lina on 2015.01.30
 */
public class MapPreferences {

    //@55.3391829,10.3604246,10z

    private static final int INITIAL_ZOOM_LEVEL = 10;
    public static final double MAP_CENTER_LATITUDE = 55.3391829;
    public static final double MAP_CENTER_LONGITUDE = 10.3604246;
    public static final String PREFS_MAP_CENTER_LATITUDE = "PREFS_MAP_CENTER_LATITUDE";
    public static final String PREFS_MAP_CENTER_LONGITUDE = "PREFS_MAP_CENTER_LONGITUDE";
    public static final String PREFS_ZOOM_LEVEL = "PREFS_ZOOM_LEVEL";
    private static final String PREFS_SHOW_ROUTES = "PREFS_SHOW_ROUTES";
    private static final String PREFS_PLAY_SOUND = "PREFS_PLAY_SOUND";
    private static final String PREFS_ROUTE_POINT_VISITED_POIS = "ROUTE_POINT_";
    private static final String PREFS_LAST_ROUTE_POINT_ID = "PREFS_LAST_ROUTE_POINT_ID";
    private static final String PREFS_VISITED_POI = "PREFS_VISITED_POI_";
    private static final String PREFS_CURRENT_ROOT = "PREFS_CURRENT_ROOT";

    public static IGeoPoint getMapCenterOsm(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long latLong = mPrefs.getLong(PREFS_MAP_CENTER_LATITUDE, getLong(MAP_CENTER_LATITUDE));
        long lngLong = mPrefs.getLong(PREFS_MAP_CENTER_LONGITUDE, getLong(MAP_CENTER_LONGITUDE));
        return new GeoPoint(getDouble(latLong), getDouble(lngLong));
    }

    public static void saveMapCenterOsm(Context context, IGeoPoint center){
        long latLong = getLong(center.getLatitude());
        long lngLong = getLong(center.getLongitude());
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(PREFS_MAP_CENTER_LATITUDE, latLong)
                .putLong(PREFS_MAP_CENTER_LONGITUDE, lngLong).commit();
    }

    public static float getZoomLevel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(PREFS_ZOOM_LEVEL, INITIAL_ZOOM_LEVEL);
    }

    public static void saveZoomLevel(Context context, float level){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putFloat(PREFS_ZOOM_LEVEL, level).commit();
    }

    public static int getZoomLevelInt(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFS_ZOOM_LEVEL, INITIAL_ZOOM_LEVEL);
    }

    public static void saveZoomLevelInt(Context context, int level){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(PREFS_ZOOM_LEVEL, level).commit();
    }


    public static boolean isDefaultRoute(Context context, String routeId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(routeId, false);
    }

    public static void saveCurrentRoute(Context context, String routeId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREFS_CURRENT_ROOT, routeId).commit();
    }
    public static String getCurrentRoute(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFS_CURRENT_ROOT, null);
    }

    public static boolean isShowRoutes(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFS_SHOW_ROUTES, true);
    }
    public static void saveShowRoutes(Context context, boolean checked) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREFS_SHOW_ROUTES, checked).commit();
    }

    public static boolean isPlaySound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFS_PLAY_SOUND, true);
    }
    public static void savePlaySound(Context context, boolean checked) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREFS_PLAY_SOUND, checked).commit();
    }

    public static long getLong(double value){
        return Double.doubleToRawLongBits(value);
    }
    public static double getDouble (long value) {
        return Double.longBitsToDouble(value);
    }


    public static Set<String> getVisitedPoisByRoutePointId(Context context, String routePointId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREFS_ROUTE_POINT_VISITED_POIS + routePointId, null);
    }

    public static void saveVisitedPoiForRoutePoint(Context context, String routePointId, String poiObjectId){
        Set<String> visitedPoiSet = getVisitedPoisByRoutePointId(context, routePointId);
        if (visitedPoiSet == null) {
            visitedPoiSet = new HashSet<>();
        }
        visitedPoiSet.add(poiObjectId);
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(PREFS_ROUTE_POINT_VISITED_POIS + routePointId, visitedPoiSet).commit();
        saveLastVisitedRoutePointId(context, routePointId);
    }

    public static String getLastVisitedRoutePointId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREFS_LAST_ROUTE_POINT_ID, null);
    }

    public static void saveLastVisitedRoutePointId(Context context, String routePointId){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREFS_LAST_ROUTE_POINT_ID, routePointId).commit();
    }
    //sets that poi is visited
    public static void setPoiVisited(Context context, String poiObjectId) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(PREFS_VISITED_POI + poiObjectId, true).commit();
    }

    public static boolean isPoiVisited(Context context, String poiObjectId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREFS_VISITED_POI + poiObjectId, false);
    }

}
