package dk.kultur.historiejagtenfyn.data.parse;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import bolts.Continuation;
import bolts.Task;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.data.models.ParseModel;
import dk.kultur.historiejagtenfyn.data.parse.contracts.*;
import java.util.List;

/**
 * Parse settings class
 * Created by JustinasK on 12/10/2014.
 */
@SuppressWarnings("UnusedDeclaration")
public class ParserApiHis {
    private static final String LOG_TAG = ParserApiHis.class.getSimpleName();

    public static final String PARSE_APPLICATION_KEY = "teBX2kjl8AKMsxYT7vmoqbtMWaVwtdJNfAiGNSby";
    public static final String PARSE_CLIENT_KEY = "dl1NGkgSfSpTufVJIHrzxGB2fDRTgmA81MzdeBUX";

    public static final String KEY_ICON_COLLECTION = "Icon";
    public static final String KEY_IMAGE_COLLECTION = "Image";
    public static final String KEY_AVATAR_COLLECTION = "Avatar";
    public static final String KEY_INFO_COLLECTION = "Info";
    public static final String KEY_LANGUAGE_COLLECTION = "Language";

    public static final String KEY_POI_CONTENT_COLLECTION = "POIContent";
    public static final String KEY_POI_COLLECTION = "PointOfInterest";
    public static final String KEY_POI_CONNECTION_COLLECTION = "PointOfInterestConnection";

    public static final String KEY_ROUTE_COLLECTION = "Route";
    public static final String KEY_ROUTE_POINT_COLLECTION = "RoutePoint";
    public static final String KEY_ROUTE_CONTENT_COLLECTION = "RouteContent";
    public static final String KEY_ROUTE_POINT_CONTENT_COLLECTION = "RoutePointContent";

    public static final String _JOIN_POIS_ROUTE_POINT = "_Join_pointOfInterests_RoutePoint";
    public static final String _JOIN_CONTENTS_POI = "_Join_contents_PointOfInterest";
    public static final String _JOIN_CONTENTS_ROUTE = "_Join_contents_Route";
    public static final String _JOIN_CONTENTS_ROUTE_POINT = "_Join_contents_RoutePoint";
    public static final String _JOIN_POIS_ROUTE = "_Join_pointOfInterests_Route";

    public static final String KEY_ANSWER_COLLECTION = "Answer";
    public static final String KEY_QUESTION_COLLECTION= "Question";
    public static final String KEY_QUIZ_COLLECTION = "Quiz";
    public static final String KEY_QUIZ_CONTENT_COLLECTION = "QuizContent";

    public static String getLogTag() {
        return LOG_TAG;
    }


    public static void registerParseModels() {
        Log.d(getLogTag(), "Registering Parse models");
        ParseObject.registerSubclass(AvatarHisContract.class);
        ParseObject.registerSubclass(IconHisContract.class);
        ParseObject.registerSubclass(ImageHisContract.class);
        ParseObject.registerSubclass(InfoHisContract.class);
        ParseObject.registerSubclass(LanguageHisContract.class);

        ParseObject.registerSubclass(POIConnectionHisContract.class);
        ParseObject.registerSubclass(POIContentHisContract.class);
        ParseObject.registerSubclass(POIHisContract.class);
        
        ParseObject.registerSubclass(RouteContentHisContract.class);
        ParseObject.registerSubclass(RouteHisContract.class);
        ParseObject.registerSubclass(RoutePointContentHisContract.class);
        ParseObject.registerSubclass(RoutePointHisContract.class);

        ParseObject.registerSubclass(QuestionHisContract.class);
        ParseObject.registerSubclass(AnswerHisContract.class);
        ParseObject.registerSubclass(QuizContentHisContract.class);
        ParseObject.registerSubclass(QuizHisContract.class);
    }

    void startTaskWithDialog(final Activity activity) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Answer");
        query.whereNotEqualTo("correct", true);

        ParseModel.startSearchTaskBackground(query, activity, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startTaskWithDialog(activity);
            }
        }, new Continuation<List<ParseObject>, Void>() {
            @Override
            public Void then(Task<List<ParseObject>> listTask) throws Exception {
                Log.d(LOG_TAG, "task finished");
                if (listTask.isFaulted()) {
                    try {
                        throw listTask.getError();
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Uncaught exception", e);
                    }
                } else {
                    final List<ParseObject> result = listTask.getResult();
                    for (ParseObject o : result) {
                        Log.d(LOG_TAG, o.getString("answer"));
                    }
                }
                return null;
            }
        });
    }

}
