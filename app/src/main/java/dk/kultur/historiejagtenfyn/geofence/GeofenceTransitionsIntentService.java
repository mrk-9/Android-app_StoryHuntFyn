/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.kultur.historiejagtenfyn.geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.kultur.historiejagtenfyn.HistoriejagtenfynApplication;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIContentHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.activities.HomeActivity;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "geofence-trans-service";
    public static final String POI_GEOFENCE_ENTERED = "dk.kultur.historiejagtenfyn.geofence.POI_GEOFENCE_ENTERED";
    private static final int POI_LOADER_ID = 111;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "foreground " + ((HistoriejagtenfynApplication)getApplicationContext()).isForeground());

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String poiId = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            if (((HistoriejagtenfynApplication)getApplicationContext()).isForeground()) {
                //app in foreground
                sendLocalBroadcast(poiId);
            } else {
                //app in backgroud
                //check global cooldown
                if (LocationPreferences.isReadyToSendNotificationInBackground(getApplicationContext())) {
                    Loader<Cursor> loader = ProviderMethods.getPointContentEntry(getApplicationContext(), new String[]{
                                    HisContract.POIEntry.TABLE_NAME + "." + POIContentHisContract.KEY_OBJECT_ID,
                                    HisContract.POIContentEntry.TABLE_NAME + "." + POIContentHisContract.KEY_NAME},
                            poiId, null);
                    loader.registerListener(POI_LOADER_ID, mLoaderCompleteListener);
                    loader.startLoading();


                } else {
                    Log.d(TAG, "still in clobal cooldown ");
                }

            }
            Log.i(TAG, poiId);
        } else {
            // Log the error.
            Log.e(TAG, "invalid transition type");
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {



        // Get the Ids of each geofence that was triggered.
        String poiId = null;
        for (Geofence geofence : triggeringGeofences) {
            poiId = geofence.getRequestId();
            break;
        }

        return poiId;
    }

    private void sendLocalBroadcast(String poiId) {
        //check poi cooldown
        if (LocationPreferences.isReadyToSendNotificationInForeground(getApplicationContext(), poiId)) {
            Log.d(TAG, "broadcast poiId " + poiId);
            Intent intent = new Intent(POI_GEOFENCE_ENTERED);
            intent.putExtra(HomeActivity.ARG_POI_ID, poiId);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        } else {
            Log.d(TAG, "poi still in cooldown");
        }
    }


    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String poiId, String title) {
        Log.d(TAG, "sendNotification " + poiId);

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
        notificationIntent.putExtra(HomeActivity.ARG_POI_ID, poiId);
        /*notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        */


        int requestCode = new Random().nextInt();

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        requestCode,
                        notificationIntent,
                        0
                );

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                //.setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(resultPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
        Log.d(TAG, "global cooldown activated ");
    }

    private Loader.OnLoadCompleteListener<Cursor> mLoaderCompleteListener = new Loader.OnLoadCompleteListener<Cursor>() {
        @Override
        public void onLoadComplete(Loader loader, Cursor data) {
            if (data.getCount() > 0) {
                data.moveToFirst();
                String poiId = data.getString(0);
                String title = data.getString(1);
                sendNotification(poiId, title);
            } else {
                Log.d(TAG, "no data found");
            }


        }
    };

}
