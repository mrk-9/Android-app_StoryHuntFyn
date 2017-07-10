package dk.kultur.historiejagtenfyn.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import dk.kultur.historiejagtenfyn.ui.fragments.map.MapPreferences;

/**
 * Created by Lina on 2015.02.22
 */
public class LocationPreferences {

    private static final String PREFS_LAST_LATITUDE = "dk.kultur.historiejagtenfyn.geofence.PREFS_LAST_LATITUDE";
    private static final String PREFS_LAST_LONGITUDE = "dk.kultur.historiejagtenfyn.geofence.PREFS_LAST_LONGITUDE";
    private static final String PREFS_GLOBAL_COOLDOWN_TIMESTAMP = "dk.kultur.historiejagtenfyn.geofence.PREFS_GLOBAL_COOLDOWN_TIMESTAMP";
    private static final String PREFS_LOCAL_COOLDOWN_TIMESTAMP = "dk.kultur.historiejagtenfyn.geofence.PREFS_LOCAL_COOLDOWN_TIMESTAMP";
    //global cooldown is 30 min
    private static final long GLOBAL_COOLDOWN_IN_MILLIS = 30 * 60 * 1000;
    //poi cooldown in foreground is 5 min
    private static final long LOCAL_POI_COOLDOWN_IN_MILLIS = 5 * 60 * 1000;

    public static LatLng getLastLocation(Context context) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long latLong = mPrefs.getLong(PREFS_LAST_LATITUDE, getLong(MapPreferences.MAP_CENTER_LATITUDE));
        long lngLong = mPrefs.getLong(PREFS_LAST_LONGITUDE, getLong(MapPreferences.MAP_CENTER_LONGITUDE));
        return new LatLng(getDouble(latLong), getDouble(lngLong));
    }

    public static void saveLocation(Context context, LatLng center){
        long latLong = getLong(center.latitude);
        long lngLong = getLong(center.longitude);
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(PREFS_LAST_LATITUDE, latLong)
                .putLong(PREFS_LAST_LONGITUDE, lngLong).commit();
    }

    public static long getLong(double value){
        return Double.doubleToRawLongBits(value);
    }
    public static double getDouble (long value) {
        return Double.longBitsToDouble(value);
    }

    public static long getGlobalCoolDownTimestamp(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFS_GLOBAL_COOLDOWN_TIMESTAMP, 0);
    }

    public static void saveGlobalCoolDownTimestamp(Context context, long timestampMillis) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(PREFS_GLOBAL_COOLDOWN_TIMESTAMP, timestampMillis).commit();
    }

    public static boolean isReadyToSendNotificationInBackground(Context context) {

        long lastTime = getGlobalCoolDownTimestamp(context);
        long currentTime = System.currentTimeMillis();
        if ( lastTime == 0 || currentTime - lastTime > GLOBAL_COOLDOWN_IN_MILLIS) {
            saveGlobalCoolDownTimestamp(context, currentTime);
            return true;
        }

        return false;

    }

    public static long getLocalCoolDownTimestamp(Context context, String poiId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFS_LOCAL_COOLDOWN_TIMESTAMP + poiId, 0);
    }

    public static void saveLocalCoolDownTimestamp(Context context, String poiId, long timestampMillis) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(PREFS_LOCAL_COOLDOWN_TIMESTAMP + poiId, timestampMillis).commit();
    }

    public static boolean isReadyToSendNotificationInForeground(Context context, String poiId) {

        long lastTime = getLocalCoolDownTimestamp(context, poiId);
        long currentTime = System.currentTimeMillis();
        if ( lastTime == 0 || currentTime - lastTime > LOCAL_POI_COOLDOWN_IN_MILLIS) {
            saveLocalCoolDownTimestamp(context, poiId, currentTime);
            return true;
        }

        return false;

    }

}
