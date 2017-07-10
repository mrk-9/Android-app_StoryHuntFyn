package dk.kultur.historiejagtenfyn.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.parse.contracts.AnswerHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.AvatarHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.IconHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ImageHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.InfoHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.LanguageHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIConnectionHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.QuestionHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.QuizContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.QuizHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RoutePointContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RoutePointHisContract;
import dk.kultur.historiejagtenfyn.data.sql.Columns.JoinColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.ParseColumns;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisDataHelper;
import dk.kultur.historiejagtenfyn.data.sql.contracts.QuizContracts;
import dk.kultur.historiejagtenfyn.ui.activities.SplashScreenActivity;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 *
 * Created by juskrt on 3/27/15.
 */
public class HisSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = HisSyncAdapter.class.getSimpleName();

    public HisSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Sync all data
     */
    private void syncFunctions() throws ParseException {
        if (isInternetConnected(getContext())) {
            updateLanguages();
            //saves active language to shared preference
            final String activeLanguageSaved = UIUtils.cacheActiveLanguageFromProvider(getContext());
            updateInfo(activeLanguageSaved);
            updateRoutesWithContent();
            updatePointOfInterest();
            updateAvatars();
            updatePointsOfInterestConnection();
            updateRoutePointContents();
            updatableQuizzes();
        //end ussual sync


//            //start     generate full sync and downland files this is needed for creating obb file.
 /*           updateRoutesWithContent(FileUtils.getExternalMediaDirFile(getContext()), true);
            updatePointOfInterest(FileUtils.getExternalMediaDirFile(getContext()), true);
            updateAvatars(FileUtils.getExternalMediaDirFile(getContext()), true);
            updatePointsOfInterestConnection();
            updateRoutePointContents();
            updatableQuizzes();

            //copy database file. this generated file should be put in assets.
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //end generate full sync


            UIUtils.saveCompleted(getContext());

        }
    }

    /**
     * getUserLanguage download all active languages
     *
     * @throws ParseException
     */
    private void updateLanguages() throws ParseException {
        updateUI(getContext().getString(R.string.updating_language));

        ParseQuery<LanguageHisContract> languages = ParseQuery.getQuery(LanguageHisContract.class);
        languages.whereEqualTo(LanguageHisContract.KEY_ACTIVE, true);
        languages.orderByDescending(LanguageHisContract.KEY_PRIORITY);
        final List<LanguageHisContract> languageHisContracts = languages.find();
        if (!languageHisContracts.isEmpty()) {
            Log.d("Language_update", "SUCCESS");
            getContext().getContentResolver().delete(HisContract.LanguageEntry.CONTENT_URI, null, null);
            for (LanguageHisContract languageHisContract : languageHisContracts) {
                getContext().getContentResolver().insert(HisContract.LanguageEntry.CONTENT_URI, languageHisContract.getContentValues());
            }
        }
    }

    private void updateUI(String string) {
        Intent intent = new Intent(SplashScreenActivity.ACTION_FILTER);
        intent.putExtra(SplashScreenActivity.TYPE_MESSAGE, string);
        getContext().sendBroadcast(intent);
    }

    /**
     * Update info table
     *
     * @throws ParseException
     */
    private void updateInfo(String languageObjectId) throws ParseException {

        updateUI(getContext().getString(R.string.updating_info));

        ParseObject parseObject = ParseObject.createWithoutData(LanguageHisContract.class, languageObjectId);
        ParseQuery<InfoHisContract> infoQuery = ParseQuery.getQuery(InfoHisContract.class);
        infoQuery.whereEqualTo(InfoHisContract.KEY_LANGUAGE, parseObject);
        final InfoHisContract info = infoQuery.getFirst();
        updateInfoIfNeeded(info);
    }

    /**
     * Checks if value exists and inserts or update if needed
     *
     * @param info contract from server
     */
    private void updateInfoIfNeeded(InfoHisContract info) {
        final Cursor query = getContext().getContentResolver().query(HisContract.InfoEntry.buildInfoUri(info.getObjectId()), null, null, null, null);
        final ContentValues contentValues = info.getContentValues();
        if (query.getCount() > 0) {
            query.close();
            int langaugeUri = getContext().getContentResolver().update(HisContract.InfoEntry.CONTENT_URI, contentValues, null, new String[]{info.getObjectId()});
            if (langaugeUri == 0) {
                Log.e(getLogTag(), "0 records updated");
            }
        } else {
            getContext().getContentResolver().insert(HisContract.InfoEntry.CONTENT_URI, contentValues);
        }
    }

    /**
     * Updating RouteContent table and downloads files
     *
     * @throws ParseException
     */
    public void updateRoutesWithContent() throws ParseException {
        updateRoutesWithContent(FileUtils.getExternalMediaDirFile(getContext()), false);
    }

    /**
     * Updating RouteContent table
     *
     * @param dir      to save media content, may be <code>null</code>.
     * @param forceAll <code>true</code> if all entries should be downloaded; otherwise,
     *                 only non existence or absolute entries are updated.
     * @throws ParseException
     */
    private void updateRoutesWithContent(@Nullable File dir, boolean forceAll) throws ParseException {
        updateUI(getContext().getString(R.string.updating_routes));

        Map<String, Object> existenceMap;
        if (forceAll) {
            existenceMap = new HashMap<>(1);
            existenceMap.put("existing", null);
        } else {
            existenceMap = getExistingParseObject(HisContract.RouteEntry.CONTENT_URI);
        }
        Log.d("updateRoutes", "start");
        final Map updateRoutesWithContent = ParseCloud.callFunction("updateRoutesWithContent", existenceMap);
        Log.d("updateRoutes", "done");
        final HashMap<String, Map> updated = (HashMap<String, Map>) updateRoutesWithContent.get("updated");
        int totalUpdated = updated.size();
        int done = 0;
        final Set<Map.Entry<String, Map>> entrySet = updated.entrySet();
        for (final Map.Entry<String, Map> entry : entrySet) {
            final Map serverObject = entry.getValue();
            IconHisContract icon = (IconHisContract) serverObject.get(RouteHisContract.KEY_ICON);
            final String routeKey = entry.getKey();
            updateIcons(icon, dir);
            ArrayList<String> pointOfInInterest = (ArrayList<String>) serverObject.get(RouteHisContract.KEY_POINT_OF_INTERESTS);
            updatePOIsRouteJoin(pointOfInInterest, routeKey);
            AvatarHisContract avatar = (AvatarHisContract) serverObject.get(RouteHisContract.KEY_AVATAR);
            updateAvatar(avatar, dir);
            HashMap contentMap = (HashMap) serverObject.get(RouteHisContract.KEY_CONTENTS);

            ArrayList<String> joinContentsRoute = new ArrayList<>();

            final Set<Map.Entry<String, RouteContentHisContract>> set = contentMap.entrySet();
            for (Map.Entry<String, RouteContentHisContract> content : set) {
                final RouteContentHisContract valueOfMap = content.getValue();
                final ContentValues contentValues = valueOfMap.getContentValues();
                final Cursor query = getContext().getContentResolver().query(HisContract.RouteContentEntry.buildRouteContentUri(valueOfMap.getObjectId()), null, null, null, null);
                final int count = query.getCount();
                query.close();
                if (count > 0) {
                    getContext().getContentResolver().update(HisContract.RouteContentEntry.CONTENT_URI, contentValues, null, new String[]{valueOfMap.getObjectId()});
                } else {
                    getContext().getContentResolver().insert(HisContract.RouteContentEntry.CONTENT_URI, contentValues);
                }
                joinContentsRoute.add(valueOfMap.getObjectId());

            }
            updateContentsRouteJoin(joinContentsRoute, entry.getKey());
            RouteHisContract route = (RouteHisContract) serverObject.get(RouteHisContract.KEY_UPDATE_ROUTE);
            updateRoute(route);
            updateUI(String.format(getContext().getString(R.string.fetching_formatted), ++done, totalUpdated));
        }

        final ArrayList<String> deleted = (ArrayList) updateRoutesWithContent.get("deleted");
        for (String s : deleted) {
            getContext().getContentResolver().delete(HisContract.RouteEntry.buildRouteUri(s), null, null);
        }
//        ParseCloud.callFunctionInBackground("updateRoutesWithContent", existenceMap,new FunctionCallback<Map>() {
//            @Override
//            public void done(final Map updateRoutesWithContent, ParseException e) {
//                if (e == null) {
//
//
//                }
//            }} );

    }

    /**
     * Updating PointOfInterest table and downloads files
     *
     * @throws ParseException
     */
    private void updatePointOfInterest() throws ParseException {
        updatePointOfInterest(FileUtils.getExternalMediaDirFile(getContext()), false);
    }

    /**
     * Updating PointOfInterest table
     *
     * @param dir         to save media content, may be <code>null</code>.
     * @param forceUpdate <code>true</code> if all entries should be downloaded; otherwise,
     *                    only non existence or absolute entries are updated.
     * @throws ParseException
     */
    private void updatePointOfInterest(File dir, boolean forceUpdate) throws ParseException {

        updateUI(getContext().getString(R.string.updating_locations));

        Map<String, Object> existenceMap;
        if (forceUpdate) {
            existenceMap = new HashMap<>(1);
            existenceMap.put("existing", null);
        } else {
            existenceMap = getExistingParseObject(HisContract.POIEntry.CONTENT_URI);
        }

        final Map<String, ArrayList<String>> getUpdatablePointOfInterestsWithIds = ParseCloud.callFunction("getUpdatablePointOfInterests", existenceMap);
        final ArrayList<String> updatable = getUpdatablePointOfInterestsWithIds.get("updatable");
        int totalCount = updatable.size();
        int progress = 0;
        for (String poiId : updatable) {
            Map<String, String> mapWithObjectId = new HashMap<>();
            mapWithObjectId.put("objectId", poiId);
            final Map<String, Map> getPointOfInterestWithContent = ParseCloud.callFunction("getPointOfInterestWithContent", mapWithObjectId);
            Map<String, POIContentHisContract> mappedPOIContent = getPointOfInterestWithContent.get("contents");
            final Set<Map.Entry<String, POIContentHisContract>> entries = mappedPOIContent.entrySet();
            for (Map.Entry<String, POIContentHisContract> entry : entries) {
                final POIContentHisContract content = entry.getValue();
                updatePOIContent(content);
                addJoinedContentsPOI(poiId, content.getObjectId());
            }
            final POIHisContract poiHisContract = (POIHisContract) getPointOfInterestWithContent.get("pointOfInterest");
            updatePOI(poiHisContract, dir);
            updateUI(String.format(getContext().getString(R.string.fetching_formatted), ++progress, totalCount));
        }
        final ArrayList<String> delete = getUpdatablePointOfInterestsWithIds.get("deleted");
        for (String s : delete) {
            getContext().getContentResolver().delete(HisContract.POIEntry.buildPOIUri(s), null, null);
        }
    }

    /**
     * Update Avatars table and downloads files
     *
     * @throws ParseException
     */
    private void updateAvatars() throws ParseException {
        updateAvatars(FileUtils.getExternalMediaDirFile(getContext()), false);
    }

    /**
     * Update Avatars table
     *
     * @param dir         to save media content, may be <code>null</code>.
     * @param forceUpdate all entries
     * @throws ParseException
     */
    private void updateAvatars(File dir, boolean forceUpdate) throws ParseException {

        updateUI(getContext().getString(R.string.updating_avatars));

        Cursor query = getContext().getContentResolver().query(HisContract.AvatarEntry.CONTENT_URI, null, null, null, null);
        ParseQuery<AvatarHisContract> avatarQuery = ParseQuery.getQuery(AvatarHisContract.class);
        List<AvatarHisContract> avatarHisContracts = avatarQuery.find();
        for (AvatarHisContract avatar : avatarHisContracts) {
            String id = avatar.getObjectId();
            String updatedAt = HisContract.getDbDateString(avatar.getUpdatedAt());
            boolean insert = true;
            for (query.moveToFirst(); !query.isAfterLast(); query.moveToNext()) {
                String string = query.getString(query.getColumnIndex(HisContract.AvatarEntry.COLUMN_OBJECT_ID));
                if (string.equals(id)) {
                    insert = false;
                    //check date
                    if (forceUpdate || !query.getString(query.getColumnIndex(HisContract.AvatarEntry.COLUMN_UPDATED_AT)).equals(updatedAt)) {
                        getContext().getContentResolver().update(
                                HisContract.AvatarEntry.CONTENT_URI, avatar.getContentValues(),
                                HisContract.AvatarEntry.COLUMN_OBJECT_ID + " = ?", new String[]{id});
                        if (dir != null) {
                            avatar.downloadFiles(dir);
                        }
                        break;
                    }
                }
            }
            if (insert) {
                getContext().getContentResolver().insert(HisContract.AvatarEntry.CONTENT_URI, avatar.getContentValues());
                if (dir != null) {
                    avatar.downloadFiles(dir);
                }
            }
        }
        query.close();
    }

    /**
     * Update avatar record
     *
     * @param avatar object to update
     * @param dir    to save media content, may be <code>null</code>.
     */
    private void updateAvatar(AvatarHisContract avatar, File dir) {
        final ContentValues contentValues = avatar.getContentValues();
        final Cursor query = getContext().getContentResolver().query(HisContract.AvatarEntry.buildAvatarUri(avatar.getObjectId()), null, null, null, null);
        final int count = query.getCount();
        query.close();
        if (count > 0) {
            getContext().getContentResolver().update(HisContract.AvatarEntry.CONTENT_URI, contentValues, null, null);
        } else {
            getContext().getContentResolver().insert(HisContract.AvatarEntry.CONTENT_URI, contentValues);
        }
        if (dir != null) {
            avatar.downloadFiles(dir);
        }
    }

    /**
     * Update POIConnection table, represents 1 to * relation
     *
     * @throws ParseException
     */
    private void updatePointsOfInterestConnection() throws ParseException {

        updateUI(getContext().getString(R.string.updating_poi_connections));

        final ArrayList<Map<String, String>> getUpdatablePointOfInterests = ParseCloud.callFunction("getPointOfInterestConnections", new HashMap<String, Object>(1));
        if (getUpdatablePointOfInterests.size() > 0) {
            Cursor query = getContext().getContentResolver().query(HisContract.POIConnectionEntry.CONTENT_URI, null, null, null, null);
            int count = query.getCount();
            query.close();
            int delete = getContext().getContentResolver().delete(HisContract.POIConnectionEntry.CONTENT_URI, null, null);
            if (count == delete) {
                for (Map<String, String> getPointOfInterestConnection : getUpdatablePointOfInterests) {
                    final String source = getPointOfInterestConnection.get(POIConnectionHisContract.SOURCE);
                    final String destination = getPointOfInterestConnection.get(POIConnectionHisContract.DESTINATION);
                    final ContentValues artificialValues = POIConnectionHisContract.getContentValues(source, destination);
                    getContext().getContentResolver().insert(HisContract.POIConnectionEntry.CONTENT_URI, artificialValues);
                }
            } else {
                throw new ParseException(ParseException.INVALID_QUERY, "Could not delete all records");
            }

        }
        Log.d(getLogTag(), "updatePointsOfInterestConnection finished");
    }


    /**
     * Updated RoutePoint table
     *
     * @throws ParseException
     */
    private void updateRoutePointContents() throws ParseException {

        updateUI(getContext().getString(R.string.updating_route_points));

        final HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        final Map<String, Map<String, Map>> getRoutePointSystem = ParseCloud.callFunction("getRoutePointSystem", objectObjectHashMap);
        final Set<Map.Entry<String, Map<String, Map>>> entries = getRoutePointSystem.entrySet();
        if (entries.size() > 0) {
            getContext().getContentResolver().delete(HisContract.RoutePointEntry.CONTENT_URI, null, null);
            getContext().getContentResolver().delete(HisContract.RoutePointContentEntry.CONTENT_URI, null, null);
            getContext().getContentResolver().delete(HisContract.JoinPOIsRoutePointEntry.CONTENT_URI, null, null);
            getContext().getContentResolver().delete(HisContract.JoinContentsRoutePointEntry.CONTENT_URI, null, null);
            for (Map.Entry<String, Map<String, Map>> entry : entries) {
                Map routeMapk = entry.getValue();
                final RoutePointHisContract routePoint = (RoutePointHisContract) routeMapk.get("routePoint");
                saveRoutePoint(routePoint);
                final Map contents = (Map) routeMapk.get("contents");
                List<String> relatedContent = new ArrayList<>();
                final Set<Map.Entry<String, RoutePointContentHisContract>> set = contents.entrySet();
                for (Map.Entry<String, RoutePointContentHisContract> contract : set) {
                    final RoutePointContentHisContract routePointContent = contract.getValue();
                    saveRoutePointContentItem(routePointContent);
                    relatedContent.add(routePointContent.getObjectId());
                }
                ArrayList<String> pointOfInterests = (ArrayList<String>) routeMapk.get("pointOfInterests");
                final String route = (String) routeMapk.get("route");
                saveJoinedContentsRoutPoint(entry.getKey(), relatedContent);
                saveJoinedPOIsRoutPoint(entry.getKey(), pointOfInterests);
            }
        }
        Log.d(getLogTag(), "getRoutePointSystem");
    }

    /**
     * Updated quizzes table
     *
     * @throws ParseException
     */
    private void updatableQuizzes() throws ParseException {
        Log.d("Sync", "update quizes");
        final Map<String, ?> existingParseObject = getExistingParseObject(QuizContracts.QuizEntry.CONTENT_URI);
        final Map<String, ArrayList<String>> updatableQuizzes = ParseCloud.callFunction("updatableQuizzes", existingParseObject);

        ArrayList<String> updatable = updatableQuizzes.get("updatable");
        ArrayList<String> deleted = updatableQuizzes.get("deleted");

        for (String delete : deleted) {
            getContext().getContentResolver().delete(QuizContracts.QuizEntry.buildWithParseId(delete), null, null);
        }

        for (String update : updatable) {
            updateExistenceQuiz(update);
        }
        Log.d(getLogTag(), "updatableQuizze");
    }

    /**
     * Update Quiz, QuizContent, Question and Answer tables
     *
     * @param quizObjectId object id
     * @throws ParseException
     */
    private void updateExistenceQuiz(String quizObjectId) throws ParseException {
        final HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("objectId", quizObjectId);
        final Map<String, Object> getRoutePointSystem = ParseCloud.callFunction("getQuizWithContent", objectObjectHashMap);
        Log.d(getLogTag(), "updatableQuizzes");
        final Map<String, Map> quizContentsMap = (Map<String, Map>) getRoutePointSystem.get("contents");
        final Set<Map.Entry<String, Map>> quizContentsSets = quizContentsMap.entrySet();

        ArrayList<ContentValues> quizContentsQuizRelation = new ArrayList<>();
        ArrayList<ContentValues> quizQuestionsQuizContent = new ArrayList<>();
        ArrayList<ContentValues> quizAnswersQuestion = new ArrayList<>();

        for (Map.Entry<String, Map> quizContentSets : quizContentsSets) {
            String quizContentObjectId = quizContentSets.getKey();
            final Map<String, Object> quizContentSet = quizContentSets.getValue();
            final QuizContentHisContract quizContent = (QuizContentHisContract) quizContentSet.get("content");
            HisContract.JoinEntry.fillRelation(quizContentsQuizRelation, quizObjectId, quizContentObjectId);
            saveParseObject(quizContent, QuizContracts.QuizEntry.QuizContentsEntry.CONTENT_URI);
            final ArrayList<Map> questionsArray = (ArrayList<Map>) quizContentSet.get("questions");

            for (Map map : questionsArray) {
                QuestionHisContract question = (QuestionHisContract) map.get("question");
                final String questionId = question.getObjectId();
                HisContract.JoinEntry.fillRelation(quizQuestionsQuizContent, quizContentObjectId, questionId);

                saveParseObject(question, QuizContracts.QuizEntry.QuestionEntry.CONTENT_URI);
                ArrayList<AnswerHisContract> answers = (ArrayList<AnswerHisContract>) map.get("answers");

                for (AnswerHisContract answer : answers) {
                    HisContract.JoinEntry.fillRelation(quizAnswersQuestion, questionId, answer.getObjectId());
                    saveParseObject(answer, QuizContracts.QuizEntry.AnswerEntry.CONTENT_URI);
                }
            }
        }

        saveJoined(quizContentsQuizRelation, QuizContracts.QuizEntry.JoinQuizContentsQuiz.buildRelation());
        saveJoined(quizQuestionsQuizContent, QuizContracts.QuizEntry.JoinQuestionsQuizContent.buildRelation());
        saveJoined(quizAnswersQuestion, QuizContracts.QuizEntry.JoinAnswersQuestion.buildRelation());

        final QuizHisContract quizContract = (QuizHisContract) getRoutePointSystem.get("quiz");
        saveParseObject(quizContract, QuizContracts.QuizEntry.CONTENT_URI);
    }

    /**
     * Inserts or updates parse object to his Uri
     *
     * @param parseContract object to save
     * @param contentUri    uri for data provider
     */
    private void saveParseObject(ParseContract parseContract, Uri contentUri) {
        final Uri buildWithObjectId = contentUri.buildUpon().appendPath(parseContract.getObjectId()).build();
        final Cursor query = getContext().getContentResolver().query(buildWithObjectId, null, null, null, null);
        if (query.moveToFirst()) {
            getContext().getContentResolver().update(buildWithObjectId, parseContract.getContentValues(), null, null);
        } else {
            getContext().getContentResolver().insert(contentUri, parseContract.getContentValues());
        }
        query.close();
    }

    /**
     * Saves new values to Uri
     *
     * @param values objects to save
     * @param uri    location
     */
    private void saveJoined(ArrayList<ContentValues> values, Uri uri) {
        for (ContentValues contentValues : values) {
            String ownerId = contentValues.getAsString(JoinColumns.COLUMN_OWNER);
            getContext().getContentResolver().delete(uri, JoinColumns.COLUMN_OWNER + " = ? ", new String[]{ownerId});

        }

        getContext().getContentResolver().bulkInsert(uri, values.toArray(new ContentValues[values.size()]));
    }

    /**
     * Return whether wifi or mobile network is connected
     *
     * @param context app
     * @return whether wifi or mobile network is connected
     */
    private boolean isInternetConnected(Context context) {
        ConnectivityManager connect = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Check if wifi or mobile network is available or not. If any of them is
        // available or connected then it will return true, otherwise false;
        return (wifi != null && wifi.isConnected()) || (mobile != null && mobile.isConnected());
    }

    /**
     * @param routeHisContract
     */
    private void updateRoute(RouteHisContract routeHisContract) {
        final ContentValues contentValues = routeHisContract.getContentValues();
        final Cursor query = getContext().getContentResolver().query(HisContract.RouteEntry.buildRouteUri(routeHisContract.getObjectId()), null, null, null, null);
        final int count = query.getCount();
        query.close();
        if (count > 0) {
            getContext().getContentResolver().update(HisContract.RouteEntry.CONTENT_URI, contentValues, null, new String[]{routeHisContract.getObjectId()});
        } else {
            getContext().getContentResolver().insert(HisContract.RouteEntry.CONTENT_URI, contentValues);
        }
    }

    /**
     * Update Icons table
     *
     * @param icon object
     * @param dir  to save file, might be <code>null<code/>.
     */
    private void updateIcons(IconHisContract icon, File dir) {
        final ContentValues contentValues = icon.getContentValues();
        final Cursor query = getContext().getContentResolver().query(HisContract.IconEntry.buildIconUri(icon.getObjectId()), null, null, null, null);
        final int count = query.getCount();
        query.close();
        if (count > 0) {
            getContext().getContentResolver().update(HisContract.IconEntry.CONTENT_URI, contentValues, null, null);
        } else {
            getContext().getContentResolver().insert(HisContract.IconEntry.CONTENT_URI, contentValues);
        }
        if (dir != null) {
            final ParseFile arRetina = icon.getParseFile(IconHisContract.KEY_AR_PIN_RETINA);
            FileUtils.saveFileFromBytes(arRetina, new File(dir, FileUtils.getRelativeName(icon.getObjectId(), FileUtils.ICON_AR_ACTIVE_RETINA)));

            final ParseFile arInactiveRetina = icon.getParseFile(IconHisContract.KEY_AR_PIN_INACTIVE_RETINA);
            FileUtils.saveFileFromBytes(arInactiveRetina, new File(dir, FileUtils.getRelativeName(icon.getObjectId(), FileUtils.ICON_AR_INACTIVE_RETINA)));

            final ParseFile pinRetina = icon.getParseFile(IconHisContract.KEY_PIN_RETINA);
            FileUtils.saveFileFromBytes(pinRetina, new File(dir, FileUtils.getRelativeName(icon.getObjectId(), FileUtils.ICON_PIN_ACTIVE_RETINA)));

            final ParseFile pinInactiveRetina = icon.getParseFile(IconHisContract.KEY_PIN_INACTIVE_RETINA);
            FileUtils.saveFileFromBytes(pinInactiveRetina, new File(dir, FileUtils.getRelativeName(icon.getObjectId(), FileUtils.ICON_PIN_INACTIVE_RETINA)));

            final ParseFile iconRetina = icon.getParseFile(IconHisContract.KEY_ICON_RETINA);
            FileUtils.saveFileFromBytes(iconRetina, new File(dir, FileUtils.getRelativeName(icon.getObjectId(), FileUtils.ICON_RETINA)));
        }
    }

    /**
     * Update ContentsRoute join with Route
     *
     * @param routeContents parse id
     * @param route         parse id
     */
    private void updateContentsRouteJoin(ArrayList<String> routeContents, String route) {
        for (String joinedConnection : routeContents) {
            final ContentValues artificialValues = HisContract.JoinEntry.getArtificialValues(route, joinedConnection);
            final Cursor query = getContext().getContentResolver().query(HisContract.JoinContentsRouteEntry.buildRouteContentsWithRouteUri(route, joinedConnection), null, null, null, null);
            query.moveToFirst();
            final int count = query.getCount();
            if (count == 0) {
                getContext().getContentResolver().insert(HisContract.JoinContentsRouteEntry.CONTENT_URI, artificialValues);
            }
            query.close();
        }
    }

    /**
     * Update POIsRoute join with Route
     *
     * @param pointOfInInterest object id
     * @param route             object id
     */
    private void updatePOIsRouteJoin(ArrayList<String> pointOfInInterest, String route) {
        for (String joinedConnection : pointOfInInterest) {
            final ContentValues artificialValues = HisContract.JoinEntry.getArtificialValues(route, joinedConnection);
            final Cursor query = getContext().getContentResolver().query(HisContract.JoinPOIsRouteEntry.buildJRoutePOIUri(route, joinedConnection), null, null, null, null);
            query.moveToFirst();
            final int count = query.getCount();
            if (count == 0) {
                getContext().getContentResolver().insert(HisContract.JoinPOIsRouteEntry.CONTENT_URI, artificialValues);
            }
            query.close();
        }
    }

    /**
     * Adds data to 1 to * table
     *
     * @param poiId    object id
     * @param objectId related id
     */
    private void addJoinedContentsPOI(String poiId, String objectId) {
        final ContentValues artificialValues = HisContract.JoinEntry.getArtificialValues(poiId, objectId);
        final Cursor query = getContext().getContentResolver().query(HisContract.JoinPOIContentsPOIEntry.buildJContentsPOIUri(poiId, objectId), null, null, null, null);
        query.moveToFirst();
        final int count = query.getCount();
        if (count == 0) {
            getContext().getContentResolver().insert(HisContract.JoinPOIContentsPOIEntry.CONTENT_URI, artificialValues);
        }
        query.close();
    }

    /**
     * Updated POI Content records
     *
     * @param content app
     */
    private void updatePOIContent(POIContentHisContract content) {
        final String objectId = content.getObjectId();
        final Cursor queryPoiContent = getContext().getContentResolver().query(HisContract.POIContentEntry.buildPOIContentUri(objectId), null, null, null, null);
        final int count = queryPoiContent.getCount();
        queryPoiContent.close();
        final ContentValues contentValues = content.getContentValues();
        if (count > 0) {
            final int update = getContext().getContentResolver().update(HisContract.POIContentEntry.buildPOIContentUri(objectId), contentValues, null, null);
        } else {
            final Uri insert = getContext().getContentResolver().insert(HisContract.POIContentEntry.CONTENT_URI, contentValues);
        }
    }

    /**
     * Update point of interest records
     *
     * @param poi object
     * @param dir file to save
     */
    private void updatePOI(POIHisContract poi, File dir) {
        final String objectId = poi.getObjectId();
        final Cursor queryPoiContent = getContext().getContentResolver().query(HisContract.POIEntry.buildPOIUri(objectId), null, null, null, null);
        final int count = queryPoiContent.getCount();
        queryPoiContent.close();
        final ContentValues contentValues = poi.getContentValues();
        if (count > 0) {
            final int update = getContext().getContentResolver().update(HisContract.POIEntry.buildPOIUri(objectId), contentValues, null, null);
        } else {
            final Uri insert = getContext().getContentResolver().insert(HisContract.POIEntry.CONTENT_URI, contentValues);
        }

        if (dir != null) {
            final ImageHisContract image = (ImageHisContract) poi.getParseObject(POIHisContract.IMAGE);
            if (image != null) {

                final ParseFile imageCropped = image.getParseFile(ImageHisContract.CROPPED);
                Log.d(getLogTag(), "cropped file is " + imageCropped);
                if (imageCropped!= null) {
                    FileUtils.saveFileFromBytes(imageCropped, new File(dir, FileUtils.getRelativeName(poi.getObjectId(), FileUtils.IMAGE)));
                } else {
                    final ParseFile imageLarge = image.getParseFile(ImageHisContract.IMAGE);
                    FileUtils.saveFileFromBytes(imageLarge, new File(dir, FileUtils.getRelativeName(poi.getObjectId(), FileUtils.IMAGE_LARGE)));
                }
            }

            final ImageHisContract factsImage = (ImageHisContract) poi.getParseObject(POIHisContract.FACTS_IMAGE);
            if (factsImage != null) {
                final ParseFile imageCropped = factsImage.getParseFile(ImageHisContract.CROPPED);
                FileUtils.saveFileFromBytes(imageCropped, new File(dir, FileUtils.getRelativeName(poi.getObjectId(), FileUtils.FACTS_IMAGE)));
            }

            final ParseFile audio = poi.getParseFile(POIHisContract.AUDIO);
            FileUtils.saveFileFromBytes(audio, new File(dir, FileUtils.getRelativeName(poi.getObjectId(), FileUtils.AUDIO)));
        }

    }

    /**
     * Saves route point content object
     *
     * @param contract object to save
     */
    private void saveRoutePointContentItem(@NonNull RoutePointContentHisContract contract) {
        final ContentValues contentValues = contract.getContentValues();
        final Cursor query = getContext().getContentResolver().query(HisContract.RoutePointContentEntry.buildRoutePointContentUri(contract.getObjectId()), null, null, null, null);
        query.moveToFirst();
        final int count = query.getCount();
        if (count > 0) {
            query.moveToFirst();
            getContext().getContentResolver().update(HisContract.RoutePointContentEntry.buildRoutePointContentUri(contract.getObjectId()), contentValues, null, null);
        } else {
            getContext().getContentResolver().insert(HisContract.RoutePointContentEntry.CONTENT_URI, contentValues);
        }
        query.close();
    }

    /**
     * Save route point object
     *
     * @param routePoint object tp save
     */
    private void saveRoutePoint(@NonNull RoutePointHisContract routePoint) {
        final ContentValues contentValues = routePoint.getContentValues();
        final Cursor query = getContext().getContentResolver().query(HisContract.RoutePointEntry.buildRoutePointUri(routePoint.getObjectId()), null, null, null, null);
        query.moveToFirst();
        final int count = query.getCount();
        if (count > 0) {
            query.moveToFirst();
            getContext().getContentResolver().update(HisContract.RoutePointEntry.buildRoutePointUri(routePoint.getObjectId()), contentValues, null, null);
        } else {
            getContext().getContentResolver().insert(HisContract.RoutePointEntry.CONTENT_URI, contentValues);
        }
        query.close();
    }

    /**
     * @param key          owner id
     * @param routeContent related id
     */
    private void saveJoinedPOIsRoutPoint(String key, List<String> routeContent) {
        for (String joinedConnection : routeContent) {
            final ContentValues artificialValues = HisContract.JoinEntry.getArtificialValues(key, joinedConnection);
            final Cursor query = getContext().getContentResolver().query(HisContract.JoinPOIsRoutePointEntry.buildJContentsRoutePointUri(key, joinedConnection), null, null, null, null);
            query.moveToFirst();
            final int count = query.getCount();
            if (count > 0) {
                query.moveToFirst();
                final int anInt = query.getInt(query.getColumnIndex(BaseColumns._ID));
                getContext().getContentResolver().update(HisContract.JoinPOIsRoutePointEntry.buildJPOIsRoutePointsUri(anInt), artificialValues, null, null);
            } else {
                getContext().getContentResolver().insert(HisContract.JoinPOIsRoutePointEntry.CONTENT_URI, artificialValues);
            }
            query.close();
        }
    }

    /**
     * @param key          owner id
     * @param routeContent related id
     */
    private void saveJoinedContentsRoutPoint(String key, List<String> routeContent) {
        for (String joinedConnection : routeContent) {
            final ContentValues artificialValues = HisContract.JoinEntry.getArtificialValues(key, joinedConnection);
            final Cursor query = getContext().getContentResolver().query(HisContract.JoinContentsRoutePointEntry.buildJContentsRoutePointUri(key, joinedConnection), null, null, null, null);
            query.moveToFirst();
            final int count = query.getCount();
            if (count > 0) {
                query.moveToFirst();
                final int anInt = query.getInt(query.getColumnIndex(BaseColumns._ID));
                getContext().getContentResolver().update(HisContract.JoinContentsRoutePointEntry.buildJContentsRoutepointUri(anInt), artificialValues, null, null);
            } else {
                getContext().getContentResolver().insert(HisContract.JoinContentsRoutePointEntry.CONTENT_URI, artificialValues);
            }
            query.close();
        }
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        try {
            syncFunctions();
        } catch (ParseException e) {
            Log.d("exception", "true");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "sync imediately");
        //ContentResolver.setSyncAutomatically(getSyncAccount(context), context.getString(R.string.content_authority), true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet. If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
// Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
// Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
// If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
/*
* Add the account and account type, no password or user data
* If successful, return the Account object, otherwise report an error.
*/
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            } else {
                ContentResolver.setIsSyncable(newAccount, context.getString(R.string.content_authority), 1);
                // Inform the system that this account is eligible for auto sync when the network is up
                ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

            }
/*
* If you don't set android:syncable="true" in
* in your <provider> element in the manifest,
* then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
* here.
*/
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
/*
* Finally, let's do a sync to get things started
*/
//        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
//        getSyncAccount(context);
        syncImmediately(context);
    }

    public String getLogTag() {
        return LOG_TAG;
    }

    /**
     * @param contentUri table
     * @return map of existing objects
     */
    public Map<String, Object> getExistingParseObject(Uri contentUri) {
        final Cursor routeCursor = getContext().getContentResolver().query(contentUri, null, null, null, null);
        Map<String, Object> existenceMap = new HashMap<>();
        final HashMap<String, Object> valuesMap = new HashMap<>();
        existenceMap.put("existing", valuesMap);
        while (routeCursor.moveToNext()) {
            final HashMap<String, Object> dateMap = new HashMap<>();
            dateMap.put("__type", "Date");
            dateMap.put("iso", routeCursor.getString(routeCursor.getColumnIndex(ParseColumns.COLUMN_UPDATED_AT)));
            valuesMap.put(routeCursor.getString(routeCursor.getColumnIndex(ParseColumns.COLUMN_OBJECT_ID)), dateMap);
        }
        routeCursor.close();
        return existenceMap;
    }

    private void copyDataBase() throws IOException
    {
        Log.d(LOG_TAG,"copy DB" + getContext().getApplicationInfo().dataDir + "/databases/" + HisDataHelper.DATABASE_NAME);
        //InputStream mInput = getContext().getResources().getAssets().open("databases/" + HisDataHelper.DATABASE_NAME);
        InputStream mInput = new FileInputStream(getContext().getApplicationInfo().dataDir + "/databases/" + HisDataHelper.DATABASE_NAME);

        File createOutFile = new File(FileUtils.getExternalMediaDirFile(getContext()), HisDataHelper.DATABASE_NAME);
        Log.d(LOG_TAG,"Output:"+createOutFile.toString());
        if(!createOutFile.exists()){
            Log.d(LOG_TAG, "create new file " + createOutFile.createNewFile());
        }
        OutputStream mOutput = new FileOutputStream(createOutFile);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer))>0)
        {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }
}