package dk.kultur.historiejagtenfyn.geofence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.kultur.historiejagtenfyn.data.sql.HisContract;

/**
 * Gets location updates every 5 minutes. if user location changed more than 10km, recalculates poi for geofence
 */
public class GeofenceLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = GeofenceLocationService.class.getSimpleName();
    private static final int GEOFENCE_LOADER_ID = 3000;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10*60*1000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Location distance limit for geofence poi update (in meters)
     */
    public static final int LOCATION_CHANGE_LIMIT = 10000; //10km

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    protected LatLng lastLocation;
    private Loader<Cursor> mLoader;
    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    private Loader.OnLoadCompleteListener<Cursor> mLoaderCompleteListener = new Loader.OnLoadCompleteListener<Cursor>() {
        @Override
        public void onLoadComplete(Loader loader, Cursor data) {
            removeGeofenceLocations();
            ArrayList<Geofence> geofenceList = new ArrayList<>();
            Log.d(TAG, "poi found count " + data.getCount());
            if (data.getCount() == 0) {
                return;
            }
            while (data.moveToNext()) {
                int radius = data.getInt(3);
                if (radius == 0) {
                    continue;
                }
                geofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(data.getString(0))

                                // Set the circular region of this geofence.
                        .setCircularRegion(
                                data.getDouble(1),
                                data.getDouble(2),
                                data.getInt(3)//GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
                        )

                                // Set the expiration duration of the geofence. This geofence gets automatically
                                // removed after this period of time.
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)

                                // Set the transition types of interest. Alerts are only generated for these
                                // transition. We track entry transitions.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                                // Create the geofence.
                        .build());
            }
            if (geofenceList.size() > 0) {
                addGeofenceLocations(geofenceList);
            }

        }
    };

    private ResultCallback<Status> removeGeofenceCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.d(TAG, "Geofence remove success");
                //for testing
                //List<Geofence> geoList = populateTestGeofenceList();
                //addGeofenceLocations(geoList);
            } else {
                // Get the status code for the error and log it using a user-friendly message.
                String errorMessage = GeofenceErrorMessages.getErrorString(getApplicationContext(),
                        status.getStatusCode());
                Log.e(TAG, errorMessage);
            }
        }
    };

    private ResultCallback<Status> addGeofenceCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.d(TAG, "Geofence add success");
            } else {
                // Get the status code for the error and log it using a user-friendly message.
                String errorMessage = GeofenceErrorMessages.getErrorString(getApplicationContext(),
                        status.getStatusCode());
                Log.e(TAG, errorMessage);
            }
        }
    };


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        lastLocation = LocationPreferences.getLastLocation(this);
        buildGoogleApiClient();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        LocationPreferences.saveLocation(getApplicationContext(), lastLocation);
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand " + startId);

        return START_STICKY;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void removeGeofenceLocations() {
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(removeGeofenceCallback); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Invalid location permission. " +
                    "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
        }
    }

    private void addGeofenceLocations(List<Geofence> geofenceList) {
        Log.d(TAG, "adding geofence locations " + geofenceList.size());
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(geofenceList),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(addGeofenceCallback); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Invalid location permission. " +
                    "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(geofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }



    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in LocationPreferences and check for it in onCreate().
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        }
        if(mCurrentLocation!= null) {
            lastLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
        removeGeofenceLocations();
        mLoader = new CursorLoader(getApplicationContext(),
                HisContract.POIEntry.buildPoiByPositionAndRange(lastLocation.latitude,
                        lastLocation.longitude, 0.02f), new String[]{"objectId",
                HisContract.POIEntry.COLUMN_COORDINATES_LAT,
                HisContract.POIEntry.COLUMN_COORDINATES_LON,
                HisContract.POIEntry.COLUMN_AUTO_RANGE}, null, null, null);
        mLoader.registerListener(GEOFENCE_LOADER_ID, mLoaderCompleteListener);
        mLoader.startLoading();




        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationUpdate " + location + " last loc " + mCurrentLocation);

        if (mCurrentLocation != null) {
            float distance = mCurrentLocation.distanceTo(location);
            //if user has traveled 10 km recalculate geofence
            if (distance > LOCATION_CHANGE_LIMIT) {
                mCurrentLocation = location;
                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                startLoadingPOIs(mCurrentLocation);
            }
        } else {
            mCurrentLocation = location;
            startLoadingPOIs(mCurrentLocation);
        }
    }

    private void startLoadingPOIs(Location location) {
        mLoader = new CursorLoader(getApplicationContext(),
                HisContract.POIEntry.buildPoiByPositionAndRange(location.getLatitude(),
                        location.getLongitude(), 0.02f), new String[]{"objectId",
                HisContract.POIEntry.COLUMN_COORDINATES_LAT,
                HisContract.POIEntry.COLUMN_COORDINATES_LON}, null, null, null);
        mLoader.registerListener(GEOFENCE_LOADER_ID, mLoaderCompleteListener);
        mLoader.startLoading();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This sample hard codes geofence data. used for testing.
     */
    private List<Geofence> populateTestGeofenceList() {
        ArrayList<Geofence> geofenceList = new ArrayList<>();
        for (Map.Entry<String, LatLng> entry : GeofenceConstants.TEST_LANDMARKS.entrySet()) {

            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(GeofenceConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }

        return geofenceList;
    }

}
