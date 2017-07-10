package dk.kultur.historiejagtenfyn.data.sql;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import dk.kultur.historiejagtenfyn.data.parse.contracts.IconHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.RouteHisContract;
import dk.kultur.historiejagtenfyn.data.sql.Columns.ParseColumns;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.*;
import dk.kultur.historiejagtenfyn.data.sql.contracts.QuizContracts.QuizEntry;
import dk.kultur.historiejagtenfyn.data.sql.contracts.QuizContracts.QuizEntry.*;
import java.util.Arrays;

/**
 * Data provider
 * Created by JustinasK on 2/10/2015.
 */
public class HisProvider extends ContentProvider {
    private static final String LOG_TAG = HisProvider.class.getSimpleName();

    private static final int ROUTE = 100;
    private static final int ROUTE_ID = 101;
    private static final int ROUTE_PARSE_ID = 102;
    private static final int ROUTE_JOIN_ICON_AND_ROUTECONTENT = 103;
    private static final int POI = 200;
    private static final int POI_WITH_ROUTE = 201;
    private static final int POI_PARSE_ID = 202;
    private static final int POI_FULL_JOINED = 203;
    private static final int POI_JOINED_ROUTE = 204;
    private static final int POI_DOUBLE_POINT_DESTINATION = 205;
    private static final int PARSE_COLLECTION = 400;
    private static final int LANGUAGE = 500;
    private static final int LANGUAGE_PARSE_ID = 501;
    private static final int INFO = 600;
    private static final int INFO_PARSE_ID = 601;
    private static final int AVATAR = 700;
    private static final int AVATAR_PARSE_ID = 701;
    private static final int POI_CONNECTION = 800;
    private static final int POI_CONNECTION_PARSE_ID = 802;
    private static final int ROUTE_CONTENT = 900;
    private static final int ROUTE_CONTENT_PARSE_ID = 901;
    private static final int ROUTE_JOINED_ROUTE_CONTENT = 902;
    private static final int ICON = 1000;
    private static final int ICON_PARSE_ID = 1001;
    private static final int J_POIS_ROUTE = 1100;
    private static final int J_POIS_ROUTE_WITH_POI_AND_ROUTE = 1101;
    private static final int J_POIS_ROUTE_ID = 1102;
    private static final int POI_CONTENT = 1201;
    private static final int POI_CONTENT_PARSE_ID = 1202;
    private static final int ROUTE_POINT_CONTENT = 1401;
    private static final int ROUTE_POINT_CONTENT_PARSE_ID = 1402;
    private static final int ROUTE_POINT = 1501;
    private static final int ROUTE_POINT_PARSE_ID = 1502;
    private static final int J_CONTENTS_ROUTE = 1600;
    private static final int J_CONTENTS_ROUTE_WITH_CONTENT_ROUTE = 1601;
    private static final int J_CONTENTS_ROUTE_ID = 1602;
    private static final int J_POIS_ROUTE_POINT = 1700;
    private static final int J_POIS_ROUTE_POINT_WITH_KEYS = 1701;
    private static final int J_POIS_ROUTE_POINT_ID = 1702;
    private static final int POIS_OF_ROUTE_POINT = 1703;
    private static final int J_CONTENTS_ROUTE_POINT = 1800;
    private static final int J_CONTENTS_ROUTE_POINT_ID = 1801;
    private static final int J_CONTENTS_ROUTE_POINT_WITH_KEYS = 1802;
    private static final int ROUTE_POINT_CONTENTS_OF_ROUTE_POINT = 1803;
    private static final int J_POI_CONTENTS_POI = 1900;
    private static final int J_POI_CONTENTS_POI_WITH_KEYS = 1901;
    private static final int J_POI_CONTENTS_POI_ID = 1902;
    private static final int J_POI_CONTENTS_POI_POI_ID = 1903;
    private static final int POI_JOIN_POI_CONTENT_JOIN_ROUTE_POINT = 2000;


    /**
     * Quiz
     */
    private static final int QUIZ = 3000;
    private static final int QUIZ_PARSE_ID = 3001;
    private static final int QUIZ_CONTENT = 3002;
    private static final int QUIZ_CONTENT_PARSE_ID = 3003;
    private static final int QUESTION = 3004;
    private static final int QUESTION_PARSE_ID = 3005;
    private static final int ANSWER = 3006;
    private static final int ANSWER_PARSE_ID = 3007;
    private static final int J_QUIZ_CONTENTS_QUIZ = 3008;
    private static final int J_QUESTIONS_QUIZ_CONTENT = 3009;
    private static final int J_ANSWERS_QUESTION = 30010;

    private static final int QUIZ_VIEW = 30011;
    private static final int QUIZ_CONTENT_VIEW = 30021;
    private static final int QUIZ_JOIN_CONTENT = 30023;

    private static final SQLiteQueryBuilder sPOIByRouteQueryBuilder;
    private static final SQLiteQueryBuilder sRouteJoinedRouteContentBuilder;
    private static final SQLiteQueryBuilder sJContentsRouteByRouteQueryBuilder;
    private static final SQLiteQueryBuilder sRoutesWithIconsAndRouteContentsQueryBuilder;
    private static final SQLiteQueryBuilder sJoinRoutesWithPOIsQueryBuilder;
    private static final SQLiteQueryBuilder sPOIsWithRouteAndRoutPointsQueryBuilder;
    private static final SQLiteQueryBuilder sPOIWithPOIDestinationQueryBuilder;

    private static final SQLiteQueryBuilder sPOIContentsJoinedPOIQueryBuilder;
    private static final SQLiteQueryBuilder sPOIsJoinedRoutePointQueryBuilder;
    private static final SQLiteQueryBuilder sPOIsWithPOIContentWithRoutPoint;
    private static final SQLiteQueryBuilder sPOIJoinedRouteQueryBuilder;

    public static final SQLiteQueryBuilder sQuizViewQueryBuilder;

    static {
        sQuizViewQueryBuilder = new SQLiteQueryBuilder();
        sQuizViewQueryBuilder.setTables(
                AnswerEntry.TABLE_NAME

                        + " INNER JOIN " + JoinAnswersQuestion.TABLE_NAME +

                        " ON " + AnswerEntry.TABLE_NAME +
                        "." + AnswerEntry.COLUMN_OBJECT_ID +
                        " = " + JoinAnswersQuestion.TABLE_NAME +
                        "." + JoinAnswersQuestion.COLUMN_RELATED +

                        "  JOIN " + QuestionEntry.TABLE_NAME +

                        " ON " + QuestionEntry.TABLE_NAME +
                        "." + QuestionEntry.COLUMN_OBJECT_ID +
                        " = " + JoinAnswersQuestion.TABLE_NAME +
                        "." + JoinAnswersQuestion.COLUMN_OWNER +

                        "  JOIN " + JoinQuestionsQuizContent.TABLE_NAME +

                        " ON " + QuestionEntry.TABLE_NAME +
                        "." + QuestionEntry.COLUMN_OBJECT_ID +
                        " = " + JoinQuestionsQuizContent.TABLE_NAME +
                        "." + JoinQuestionsQuizContent.COLUMN_RELATED +

                        "  JOIN " + QuizContentsEntry.TABLE_NAME +

                        " ON " + QuizContentsEntry.TABLE_NAME +
                        "." + QuizContentsEntry.COLUMN_OBJECT_ID +
                        " = " + JoinQuestionsQuizContent.TABLE_NAME +
                        "." + JoinQuestionsQuizContent.COLUMN_OWNER +

                        " JOIN " + JoinQuizContentsQuiz.TABLE_NAME +

                        " ON " + QuizContentsEntry.TABLE_NAME +
                        "." + QuizContentsEntry.COLUMN_OBJECT_ID +
                        " = " + JoinQuizContentsQuiz.TABLE_NAME +
                        "." + JoinQuizContentsQuiz.COLUMN_RELATED +

                        "  JOIN " + QuizEntry.TABLE_NAME +

                        " ON " + QuizEntry.TABLE_NAME +
                        "." + QuizEntry.COLUMN_OBJECT_ID +
                        " = " + JoinQuizContentsQuiz.TABLE_NAME +
                        "." + JoinQuizContentsQuiz.COLUMN_OWNER
        );
    }

    static {
        sPOIJoinedRouteQueryBuilder = new SQLiteQueryBuilder();
        sPOIJoinedRouteQueryBuilder.setTables(
                POIEntry.TABLE_NAME + " LEFT JOIN " +
                        JoinPOIsRouteEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + POIEntry.COLUMN_OBJECT_ID +
                        " = " + JoinPOIsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED

                        + " LEFT JOIN " +
                        RouteEntry.TABLE_NAME +
                        " ON " + JoinPOIsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_OWNER +
                        " = " + RouteEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID
        );
    }

    static {
        sPOIByRouteQueryBuilder = new SQLiteQueryBuilder();
        sPOIByRouteQueryBuilder.setTables(
                POIEntry.TABLE_NAME + " INNER JOIN " +
                        RouteEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + POIHisContract.KEY_PARENT_ROUTE +
                        " = " + RouteEntry.TABLE_NAME +
                        "." + RouteHisContract.KEY_OBJECT_ID);
    }

    static {
        sPOIContentsJoinedPOIQueryBuilder = new SQLiteQueryBuilder();
        sPOIContentsJoinedPOIQueryBuilder.setTables(
                POIEntry.TABLE_NAME + " LEFT JOIN " +
                        JoinPOIContentsPOIEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + POIEntry.COLUMN_OBJECT_ID +
                        " = " + JoinPOIContentsPOIEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_OWNER

                        + " LEFT JOIN " +
                        POIContentEntry.TABLE_NAME +
                        " ON " + JoinPOIContentsPOIEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED +
                        " = " + POIContentEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID
        );
    }

    static {
        sPOIsJoinedRoutePointQueryBuilder = new SQLiteQueryBuilder();
        sPOIsJoinedRoutePointQueryBuilder.setTables(
                RoutePointEntry.TABLE_NAME + " LEFT JOIN " +
                        JoinPOIsRoutePointEntry.TABLE_NAME +
                        " ON " + RoutePointEntry.TABLE_NAME +
                        "." + RoutePointEntry.COLUMN_OBJECT_ID +
                        " = " + JoinPOIsRoutePointEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_OWNER

                        + " LEFT JOIN " +
                        POIEntry.TABLE_NAME +
                        " ON " + JoinPOIsRoutePointEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED +
                        " = " + POIEntry.TABLE_NAME +
                        "." + POIEntry.COLUMN_OBJECT_ID
        );
    }


    static {
        sJoinRoutesWithPOIsQueryBuilder = new SQLiteQueryBuilder();
        sJoinRoutesWithPOIsQueryBuilder.setTables(
                RouteEntry.TABLE_NAME + " INNER JOIN " +

                        JoinPOIsRouteEntry.TABLE_NAME +
                        " ON " + RouteEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID +
                        " = " + JoinPOIsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_OWNER

                        + " JOIN " +

                        POIEntry.TABLE_NAME +
                        " ON " + JoinPOIsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED +
                        " = " + POIEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID
        );
    }

    static {
        sPOIWithPOIDestinationQueryBuilder = new SQLiteQueryBuilder();
        sPOIWithPOIDestinationQueryBuilder.setTables(
                POIConnectionEntry.TABLE_NAME + " INNER JOIN " +

                        POIEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + POIEntry.COLUMN_OBJECT_ID +
                        " = " + POIConnectionEntry.TABLE_NAME +
                        "." + POIConnectionEntry.COLUMN_SOURCE

                        + " INNER JOIN " +
                        POIEntry.TABLE_NAME + " " + POIDestinationEntry.TABLE_NAME + //alias
                        " ON " + POIDestinationEntry.TABLE_NAME +
                        "." + POIDestinationEntry.COLUMN_OBJECT_ID +
                        " = " + POIConnectionEntry.TABLE_NAME +
                        "." + POIConnectionEntry.COLUMN_DESTINATION
        );
    }


    static {
        sPOIsWithRouteAndRoutPointsQueryBuilder = new SQLiteQueryBuilder();
        sPOIsWithRouteAndRoutPointsQueryBuilder.setTables(
                POIEntry.TABLE_NAME + " LEFT JOIN " +
                        JoinPOIsRouteEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID +
                        " = " + JoinPOIsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED

                        + " LEFT JOIN " +
                        RouteEntry.TABLE_NAME +
                        " ON " + JoinPOIsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_OWNER +
                        " = " + RouteEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID

                        + " LEFT JOIN " +
                        JoinPOIsRoutePointEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID +
                        " = " + JoinPOIsRoutePointEntry.TABLE_NAME +
                        "." + JoinPOIsRoutePointEntry.COLUMN_RELATED
        );
    }


    static {
        sPOIsWithPOIContentWithRoutPoint = new SQLiteQueryBuilder();
        sPOIsWithPOIContentWithRoutPoint.setTables(
                POIEntry.TABLE_NAME + " LEFT JOIN " +
                        JoinPOIContentsPOIEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID +
                        " = " + JoinPOIContentsPOIEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED

                        + " LEFT JOIN " +
                        JoinPOIsRoutePointEntry.TABLE_NAME +
                        " ON " + POIEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID +
                        " = " + JoinPOIsRoutePointEntry.TABLE_NAME +
                        "." + JoinPOIsRoutePointEntry.COLUMN_RELATED
        );
    }

    static {
        sRouteJoinedRouteContentBuilder = new SQLiteQueryBuilder();
        sRouteJoinedRouteContentBuilder.setTables(
                RouteEntry.TABLE_NAME + " INNER JOIN " +
                        JoinContentsRouteEntry.TABLE_NAME +
                        " ON " + RouteEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID +
                        " = " + JoinContentsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_OWNER

                        + " JOIN " +

                        RouteContentEntry.TABLE_NAME +
                        " ON " + JoinContentsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED +
                        " = " + RouteContentEntry.TABLE_NAME +
                        "." + ParseContract.KEY_OBJECT_ID
        );
    }

    static {
        sJContentsRouteByRouteQueryBuilder = new SQLiteQueryBuilder();
        sJContentsRouteByRouteQueryBuilder.setTables(JoinContentsRouteEntry.TABLE_NAME);
    }

    static {
        sRoutesWithIconsAndRouteContentsQueryBuilder = new SQLiteQueryBuilder();
        sRoutesWithIconsAndRouteContentsQueryBuilder.setTables(
                RouteEntry.TABLE_NAME + " INNER JOIN " +
                        IconEntry.TABLE_NAME +
                        " ON " + RouteEntry.TABLE_NAME +
                        "." + RouteHisContract.KEY_ICON + ParseContract.POINTER_ID +
                        " = " + IconEntry.TABLE_NAME +
                        "." + IconHisContract.KEY_OBJECT_ID

                        + " JOIN " +

                        JoinContentsRouteEntry.TABLE_NAME +
                        " ON " + JoinContentsRouteEntry.TABLE_NAME +
                        "." + JoinContentsRouteEntry.COLUMN_OWNER +
                        " = " + RouteEntry.TABLE_NAME +
                        "." + RouteEntry.COLUMN_OBJECT_ID

                        + " JOIN " +

                        RouteContentEntry.TABLE_NAME +
                        " ON " + JoinContentsRouteEntry.TABLE_NAME +
                        "." + JoinEntry.COLUMN_RELATED +
                        " = " + RouteContentEntry.TABLE_NAME +
                        "." + RouteContentEntry.COLUMN_OBJECT_ID


        );
    }

    private static final String sRouteSelection = RouteEntry.TABLE_NAME + "." + RouteHisContract.KEY_OBJECT_ID + " = ? ";

    private static final String sJoinedSelection =
            JoinEntry.COLUMN_OWNER + " = ? AND " +
                    JoinEntry.COLUMN_RELATED + " = ? ";

    private static final String sPoiIdSelection = POIEntry.TABLE_NAME + "." + ParseContract.KEY_OBJECT_ID + " = ?";

    private static final String sObjectIdSelection = ParseContract.KEY_OBJECT_ID + " = ?";

    private static final String sCoordinateSelection =
            "(" + POIEntry.COLUMN_COORDINATES_LAT + " - ? ) * (" + POIEntry.COLUMN_COORDINATES_LAT + " - ? ) + (" +
                    POIEntry.COLUMN_COORDINATES_LON + " - ? ) * ( " + POIEntry.COLUMN_COORDINATES_LON + " - ? ) < ? * ? ";
    private static final String sParseObjectUpdate = "objectId = ? ";


    HisDataHelper databaseHelper;
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = HisContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, HisContract.PATH_PARSE_COLLECTION, PARSE_COLLECTION);

        matcher.addURI(authority, HisContract.PATH_LANGUAGE, LANGUAGE);
        matcher.addURI(authority, HisContract.PATH_LANGUAGE + "/*", LANGUAGE_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_INFO, INFO);
        matcher.addURI(authority, HisContract.PATH_INFO + "/*", INFO_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_AVATAR, AVATAR);
        matcher.addURI(authority, HisContract.PATH_AVATAR + "/*", AVATAR_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_ICON, ICON);
        matcher.addURI(authority, HisContract.PATH_ICON + "/*", ICON_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_ROUTE, ROUTE);
        matcher.addURI(authority, HisContract.PATH_ROUTE + "/#", ROUTE_ID);
        matcher.addURI(authority, HisContract.PATH_ROUTE + "/*", ROUTE_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_ROUTE_JOINED_ICON_JOINED_ROUTE_CONTENT, ROUTE_JOIN_ICON_AND_ROUTECONTENT);

        matcher.addURI(authority, HisContract.PATH_ROUTE_JOINED_ROUTE_CONTENT, ROUTE_JOINED_ROUTE_CONTENT);

        matcher.addURI(authority, HisContract.PATH_ROUTE_POINT, ROUTE_POINT);
        matcher.addURI(authority, HisContract.PATH_ROUTE_POINT + "/*", ROUTE_POINT_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_ROUTE_POINT_CONTENT, ROUTE_POINT_CONTENT);
        matcher.addURI(authority, HisContract.PATH_ROUTE_POINT_CONTENT + "/*", ROUTE_POINT_CONTENT_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_ROUTE_CONTENT, ROUTE_CONTENT);
        matcher.addURI(authority, HisContract.PATH_ROUTE_CONTENT + "/*", ROUTE_CONTENT_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_POI, POI);
        matcher.addURI(authority, HisContract.PATH_POI + "/*", POI_WITH_ROUTE);
        matcher.addURI(authority, HisContract.PATH_POI + "/*", POI_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_POI_JOINED_ROUTE_ROUTE_POINT, POI_FULL_JOINED);
        matcher.addURI(authority, HisContract.PATH_POI_JOINED_DESTINATION, POI_DOUBLE_POINT_DESTINATION);

        matcher.addURI(authority, HisContract.PATH_POI_CONTENT, POI_CONTENT);
        matcher.addURI(authority, HisContract.PATH_POI_CONTENT + "/*", POI_CONTENT_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_POI_CONNECTION, POI_CONNECTION);
        matcher.addURI(authority, HisContract.PATH_POI_CONNECTION + "/*", POI_CONNECTION_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_JOIN_POIS_ROUTE, J_POIS_ROUTE);
        matcher.addURI(authority, HisContract.PATH_JOIN_POIS_ROUTE + "/#", J_POIS_ROUTE_ID);
        matcher.addURI(authority, HisContract.PATH_JOIN_POIS_ROUTE + "/*/*", J_POIS_ROUTE_WITH_POI_AND_ROUTE);

        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_ROUTE, J_CONTENTS_ROUTE);
        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_ROUTE + "/#", J_CONTENTS_ROUTE_ID);
        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_ROUTE + "/*/*", J_CONTENTS_ROUTE_WITH_CONTENT_ROUTE);

        matcher.addURI(authority, HisContract.PATH_JOIN_POIS_ROUTE_POINT, J_POIS_ROUTE_POINT);
        matcher.addURI(authority, HisContract.PATH_JOIN_POIS_ROUTE_POINT + "/#", J_POIS_ROUTE_POINT_ID);
        matcher.addURI(authority, HisContract.PATH_JOIN_POIS_ROUTE_POINT + "/*/*", J_POIS_ROUTE_POINT_WITH_KEYS);

        matcher.addURI(authority, HisContract.PATH_RELATION + "/" + HisContract.PATH_ROUTE_POINT + "/*/" + HisContract.PATH_POIS, POIS_OF_ROUTE_POINT);

        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_ROUTE_POINT, J_CONTENTS_ROUTE_POINT);
        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_ROUTE_POINT + "/#", J_CONTENTS_ROUTE_POINT_ID);
        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_ROUTE_POINT + "/*/*", J_CONTENTS_ROUTE_POINT_WITH_KEYS);

        matcher.addURI(authority, HisContract.PATH_RELATION + "/" + HisContract.PATH_ROUTE_POINT + "/*/" + HisContract.PATH_ROUTE_POINT_CONTENTS, ROUTE_POINT_CONTENTS_OF_ROUTE_POINT);

        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_POI, J_POI_CONTENTS_POI);
        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_POI + "/#", J_POI_CONTENTS_POI_ID);
        matcher.addURI(authority, HisContract.PATH_JOIN_CONTENTS_POI + "/*/*", J_POI_CONTENTS_POI_WITH_KEYS);

        matcher.addURI(authority,
                HisContract.PATH_RELATION + "/" + HisContract.PATH_POI + "/*/" + HisContract.PATH_ROUTE, POI_JOINED_ROUTE);

        matcher.addURI(authority,
                HisContract.PATH_RELATION + "/" + HisContract.PATH_POI_CONTENT + "/*/" + HisContract.PATH_POI,
                J_POI_CONTENTS_POI_POI_ID);
        matcher.addURI(authority,
                HisContract.PATH_RELATION + "/" + HisContract.PATH_POI + "/*/" + HisContract.PATH_POI_CONTENT + "/" + HisContract.PATH_ROUTE_POINT,
                POI_JOIN_POI_CONTENT_JOIN_ROUTE_POINT);

        matcher.addURI(authority, HisContract.PATH_RELATION + "/" + HisContract.PATH_QUIZ + "/*/" + HisContract.PATH_QUIZ_CONTENTS, J_QUIZ_CONTENTS_QUIZ);
        matcher.addURI(authority, HisContract.PATH_RELATION + "/" + HisContract.PATH_QUIZ_CONTENT + "/*/" + HisContract.PATH_QUESTIONS, J_QUESTIONS_QUIZ_CONTENT);
        matcher.addURI(authority, HisContract.PATH_RELATION + "/" + HisContract.PATH_QUESTION + "/*/" + HisContract.PATH_ANSWERS, J_ANSWERS_QUESTION);

        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_QUIZ, QUIZ);
        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_QUIZ_CONTENT, QUIZ_CONTENT);
        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_QUESTION, QUESTION);
        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_ANSWER, ANSWER);

        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_QUIZ + "/*", QUIZ_PARSE_ID);
        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_QUIZ_CONTENT + "/*", QUIZ_CONTENT_PARSE_ID);
        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_QUESTION + "/*", QUESTION_PARSE_ID);
        matcher.addURI(authority, HisContract.PATH_CLASS + "/" + HisContract.PATH_ANSWER + "/*", ANSWER_PARSE_ID);

        matcher.addURI(authority, HisContract.PATH_VIEW + "/" + HisContract.PATH_QUIZ, QUIZ_VIEW);
        matcher.addURI(authority, HisContract.PATH_VIEW + "/" + HisContract.PATH_JOIN_CONTENTS_QUIZ, QUIZ_CONTENT_VIEW);

        matcher.addURI(authority, HisContract.PATH_RELATION + "/" + HisContract.PATH_QUIZ, QUIZ_JOIN_CONTENT);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PARSE_COLLECTION:
                return ParseCollectionsEntry.CONTENT_TYPE;
            case LANGUAGE:
                return LanguageEntry.CONTENT_TYPE;
            case LANGUAGE_PARSE_ID:
                return LanguageEntry.CONTENT_ITEM_TYPE;
            case INFO:
                return InfoEntry.CONTENT_TYPE;
            case INFO_PARSE_ID:
                return InfoEntry.CONTENT_ITEM_TYPE;
            case AVATAR:
                return AvatarEntry.CONTENT_TYPE;
            case AVATAR_PARSE_ID:
                return AvatarEntry.CONTENT_ITEM_TYPE;
            case ICON:
                return IconEntry.CONTENT_TYPE;
            case ICON_PARSE_ID:
                return IconEntry.CONTENT_ITEM_TYPE;
            case POI:
                return POIEntry.CONTENT_TYPE;
            case POI_FULL_JOINED:
                return POIFullJoinedEntry.CONTENT_TYPE;
            case POI_PARSE_ID:
                return POIEntry.CONTENT_ITEM_TYPE;
            case POI_WITH_ROUTE:
                return POIEntry.CONTENT_ITEM_TYPE;
            case POI_CONTENT:
                return POIContentEntry.CONTENT_TYPE;
            case POI_CONTENT_PARSE_ID:
                return POIContentEntry.CONTENT_ITEM_TYPE;
            case POI_CONNECTION:
                return POIConnectionEntry.CONTENT_TYPE;
            case POI_CONNECTION_PARSE_ID:
                return POIConnectionEntry.CONTENT_ITEM_TYPE;
            case ROUTE:
                return RouteEntry.CONTENT_TYPE;
            case ROUTE_ID:
                return RouteEntry.CONTENT_ITEM_TYPE;
            case ROUTE_PARSE_ID:
                return RouteEntry.CONTENT_ITEM_TYPE;
            case ROUTE_JOIN_ICON_AND_ROUTECONTENT:
                return RouteJoinedIconJoinedContentEntry.CONTENT_TYPE;
            case ROUTE_JOINED_ROUTE_CONTENT:
                return RouteJoinedRouteContentEntry.CONTENT_TYPE;
            case ROUTE_CONTENT:
                return RouteContentEntry.CONTENT_TYPE;
            case ROUTE_CONTENT_PARSE_ID:
                return RouteContentEntry.CONTENT_ITEM_TYPE;
            case ROUTE_POINT:
                return RoutePointEntry.CONTENT_TYPE;
            case ROUTE_POINT_PARSE_ID:
                return RoutePointEntry.CONTENT_ITEM_TYPE;
            case ROUTE_POINT_CONTENT:
                return RoutePointContentEntry.CONTENT_TYPE;
            case ROUTE_POINT_CONTENT_PARSE_ID:
                return RoutePointContentEntry.CONTENT_ITEM_TYPE;
            case J_POIS_ROUTE:
                return JoinPOIsRouteEntry.CONTENT_TYPE;
            case J_POIS_ROUTE_WITH_POI_AND_ROUTE:
                return JoinPOIsRouteEntry.CONTENT_ITEM_TYPE;
            case J_POIS_ROUTE_ID:
                return JoinPOIsRouteEntry.CONTENT_ITEM_TYPE;
            case J_CONTENTS_ROUTE:
                return JoinContentsRouteEntry.CONTENT_TYPE;
            case J_CONTENTS_ROUTE_WITH_CONTENT_ROUTE:
                return JoinContentsRouteEntry.CONTENT_ITEM_TYPE;
            case J_CONTENTS_ROUTE_ID:
                return JoinContentsRouteEntry.CONTENT_ITEM_TYPE;
            case J_POIS_ROUTE_POINT:
                return JoinPOIsRoutePointEntry.CONTENT_TYPE;
            case J_POIS_ROUTE_POINT_WITH_KEYS:
                return JoinPOIsRoutePointEntry.CONTENT_ITEM_TYPE;
            case J_POIS_ROUTE_POINT_ID:
                return JoinPOIsRoutePointEntry.CONTENT_ITEM_TYPE;
            case J_CONTENTS_ROUTE_POINT:
                return JoinContentsRoutePointEntry.CONTENT_TYPE;
            case J_CONTENTS_ROUTE_POINT_WITH_KEYS:
                return JoinContentsRoutePointEntry.CONTENT_ITEM_TYPE;
            case J_CONTENTS_ROUTE_POINT_ID:
                return JoinContentsRoutePointEntry.CONTENT_ITEM_TYPE;
            case J_POI_CONTENTS_POI:
                return JoinPOIContentsPOIEntry.CONTENT_TYPE;
            case J_POI_CONTENTS_POI_ID:
                return JoinPOIContentsPOIEntry.CONTENT_ITEM_TYPE;
            case J_POI_CONTENTS_POI_WITH_KEYS:
                return JoinPOIContentsPOIEntry.CONTENT_ITEM_TYPE;
            case POI_JOIN_POI_CONTENT_JOIN_ROUTE_POINT:
                return POIJoinedRoutePointEntry.CONTENT_TYPE;
            case J_POI_CONTENTS_POI_POI_ID:
                return POIContentJoinedPOIEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new HisDataHelper(getContext());

        Cursor dbCursor = databaseHelper.getReadableDatabase().query(AvatarEntry.TABLE_NAME, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        dbCursor.close();
        Log.d(getLogTag(), Arrays.deepToString(columnNames));

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case PARSE_COLLECTION: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        ParseCollectionsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LANGUAGE: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        LanguageEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LANGUAGE_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        LanguageEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case INFO: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        InfoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case INFO_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        InfoEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case AVATAR: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        AvatarEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case AVATAR_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        AvatarEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ICON: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        IconEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ICON_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        IconEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POI: {
                if (uri.getQuery() != null && uri.getQuery().length() > 2) {
                    final String latitude = uri.getQueryParameter(POIEntry.KEY_PARAMETER_LAT);
                    final String longitude = uri.getQueryParameter(POIEntry.KEY_PARAMETER_LON);
                    final String queryRadiusDms = uri.getQueryParameter(POIEntry.KEY_RADIUS_IN_DMS);

                    selection = sCoordinateSelection;
                    selectionArgs = new String[]{latitude, latitude, longitude, longitude, queryRadiusDms, queryRadiusDms};
                }

                retCursor = databaseHelper.getReadableDatabase().query(
                        POIEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POI_JOINED_ROUTE: {
                if (uri.getQuery() != null && uri.getQuery().length() > 2) {
                    final String latitude = uri.getQueryParameter(POIEntry.KEY_PARAMETER_LAT);
                    final String longitude = uri.getQueryParameter(POIEntry.KEY_PARAMETER_LON);
                    final String queryRadiusDms = uri.getQueryParameter(POIEntry.KEY_RADIUS_IN_DMS);

                    selection = sCoordinateSelection;
                    selectionArgs = new String[]{latitude, latitude, longitude, longitude, queryRadiusDms, queryRadiusDms};
                }
                retCursor = sPOIJoinedRouteQueryBuilder.query(databaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case POI_FULL_JOINED: {
                retCursor = sPOIsWithRouteAndRoutPointsQueryBuilder.query(databaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case POI_DOUBLE_POINT_DESTINATION: {
                retCursor = sPOIWithPOIDestinationQueryBuilder.query(databaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case POI_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        POIEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POI_CONTENT: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        POIContentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POI_CONTENT_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        POIContentEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POI_WITH_ROUTE: {
                retCursor = getPOIByRoute(uri, projection, sortOrder);
                break;
            }
            case POI_CONNECTION: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        POIConnectionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POI_CONNECTION_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        POIConnectionEntry.TABLE_NAME,
                        projection,
                        sObjectIdSelection,
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RouteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RouteEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_JOIN_ICON_AND_ROUTECONTENT: {
                retCursor = getRoutesWithIconAndRoutecontent(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case POI_JOIN_POI_CONTENT_JOIN_ROUTE_POINT: {
                retCursor = getPOIJoinedPOIContentJoinedRoutePoint(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case ROUTE_JOINED_ROUTE_CONTENT: {
                retCursor = sRouteJoinedRouteContentBuilder.query(databaseHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ROUTE_CONTENT: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RouteContentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_CONTENT_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RouteContentEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_POINT_CONTENT: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RoutePointContentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_POINT_CONTENT_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RoutePointContentEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_POINT: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RoutePointEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ROUTE_POINT_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        RoutePointEntry.TABLE_NAME,
                        projection,
                        ParseContract.KEY_OBJECT_ID + " = '" + uri.getLastPathSegment() + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case J_POIS_ROUTE: {
                retCursor = getJoinRoutesWithPOIs(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case J_POIS_ROUTE_WITH_POI_AND_ROUTE: {
                retCursor = getJoinedByConnection(uri, projection, sortOrder, JoinPOIsRouteEntry.TABLE_NAME);
                break;
            }
            case J_CONTENTS_ROUTE: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        JoinContentsRouteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case J_CONTENTS_ROUTE_WITH_CONTENT_ROUTE: {
                retCursor = getJoinedByConnection(uri, projection, sortOrder, JoinContentsRouteEntry.TABLE_NAME);
                break;
            }
            case J_POIS_ROUTE_POINT: {
                retCursor = getPOISJoinedRouterPoint(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            case J_POIS_ROUTE_POINT_WITH_KEYS: {
                retCursor = getJoinedByConnection(uri, projection, sortOrder, JoinPOIsRoutePointEntry.TABLE_NAME);
                break;
            }
            case J_CONTENTS_ROUTE_POINT: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        JoinContentsRoutePointEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case J_CONTENTS_ROUTE_POINT_WITH_KEYS: {
                retCursor = getJoinedByConnection(uri, projection, sortOrder, JoinContentsRoutePointEntry.TABLE_NAME);
                break;
            }
            case J_POI_CONTENTS_POI_WITH_KEYS: {
                retCursor = getJoinedByConnection(uri, projection, sortOrder, JoinPOIContentsPOIEntry.TABLE_NAME);
                break;
            }
            case J_CONTENTS_ROUTE_POINT_ID: {
                retCursor = getJoinedByConnection(uri, projection, sortOrder, JoinPOIContentsPOIEntry.TABLE_NAME);
                break;
            }
            case J_POI_CONTENTS_POI_POI_ID: {
                retCursor = getPOIJoinedPOIContent(uri, projection, sortOrder, JoinPOIContentsPOIEntry.TABLE_NAME);
                break;
            }
            case QUIZ:{
                retCursor = databaseHelper.getReadableDatabase().query(
                        QuizEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case QUIZ_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        QuizEntry.TABLE_NAME,
                        projection,
                        sObjectIdSelection,
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case QUIZ_CONTENT_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        QuizContentsEntry.TABLE_NAME,
                        projection,
                        sObjectIdSelection,
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case QUESTION_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        QuestionEntry.TABLE_NAME,
                        projection,
                        sObjectIdSelection,
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ANSWER_PARSE_ID: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        AnswerEntry.TABLE_NAME,
                        projection,
                        sObjectIdSelection,
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case QUIZ_VIEW: {
                retCursor = sQuizViewQueryBuilder.query(databaseHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);

                break;
            }
            case POIS_OF_ROUTE_POINT: {
                final SQLiteQueryBuilder sqLiteQueryBuilder = buildJoinRelation(JoinPOIsRoutePointEntry.TABLE_NAME, RoutePointEntry.TABLE_NAME, POIEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(JoinEntry.COLUMN_OWNER + " = " + "'" + RelationEntry.getOwnerId(uri) + "'");
                retCursor = sqLiteQueryBuilder.query(databaseHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }

            case ROUTE_POINT_CONTENTS_OF_ROUTE_POINT: {
                final SQLiteQueryBuilder sqLiteQueryBuilder = buildJoinRelation(JoinContentsRoutePointEntry.TABLE_NAME, RoutePointEntry.TABLE_NAME, RoutePointContentEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(JoinEntry.COLUMN_OWNER + " = " + "'" + RelationEntry.getOwnerId(uri) + "'");
                retCursor = sqLiteQueryBuilder.query(databaseHelper.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case QUIZ_CONTENT:{
                retCursor = databaseHelper.getReadableDatabase().query(
                        QuizContentsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            }
            case QUESTION: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        QuestionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ANSWER: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        AnswerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case QUIZ_JOIN_CONTENT: {
                retCursor = databaseHelper.getReadableDatabase().query(
                        JoinQuizContentsQuiz.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private SQLiteQueryBuilder buildJoinRelation(@NonNull String joinedTableName, @NonNull String relatedTable) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(
                joinedTableName + " INNER JOIN " + relatedTable + " ON " +
                        relatedTable + "." + AnswerEntry.COLUMN_OBJECT_ID +
                        " = " +
                        joinedTableName + "." + JoinAnswersQuestion.COLUMN_RELATED
        );
        return builder;
    }

    private SQLiteQueryBuilder buildJoinRelation(@NonNull String joinedTableName, @NonNull String owningTable, @NonNull String relatedTable) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(
                joinedTableName + " INNER JOIN " + owningTable + " ON " +

                        owningTable + "." + ParseColumns.COLUMN_OBJECT_ID +
                        " = " + joinedTableName + "." + JoinEntry.COLUMN_OWNER +

                        " JOIN " + relatedTable + " ON " +

                        relatedTable + "." + ParseColumns.COLUMN_OBJECT_ID +
                        " = " + joinedTableName + "." + JoinEntry.COLUMN_RELATED

        );
        return builder;
    }

    private String buildOwnerSelection(String ownerTable) {
        return ownerTable + "." + ParseColumns.COLUMN_OBJECT_ID + "= ?";
    }


    private Cursor getPOISJoinedRouterPoint(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return sPOIsJoinedRoutePointQueryBuilder.query(databaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getPOIJoinedPOIContentJoinedRoutePoint(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String poiId = POIJoinedRoutePointEntry.getPOIId(uri);
        return sPOIsWithPOIContentWithRoutPoint.query(databaseHelper.getReadableDatabase(), projection, sPoiIdSelection, new String[]{poiId}, null, null, sortOrder, "1");
    }

    private Cursor getPOIJoinedPOIContent(Uri uri, String[] projection, String sortOrder, String tableName) {
        final String poiId = POIContentJoinedPOIEntry.getPOIId(uri);
        return sPOIContentsJoinedPOIQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                sPoiIdSelection,
                new String[]{poiId},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case PARSE_COLLECTION: {
                long _id = db.insert(ParseCollectionsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ParseCollectionsEntry.buildParseContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LANGUAGE: {
                long _id = db.insert(LanguageEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = LanguageEntry.buildLanguageUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case INFO: {
                long _id = db.insert(InfoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = InfoEntry.buildInfoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case AVATAR: {
                long _id = db.insert(AvatarEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AvatarEntry.buildAvatarUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ICON: {
                long _id = db.insert(IconEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = IconEntry.buildIconUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case POI: {
                long _id = db.insert(POIEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = POIEntry.buildPOIUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case POI_CONTENT: {
                long _id = db.insert(POIContentEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = POIContentEntry.buildPOIContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case POI_CONNECTION: {
                long id = db.insert(POIConnectionEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = POIConnectionEntry.buildPOIConnectionUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ROUTE: {
                long _id = db.insert(RouteEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = RouteEntry.buildRouteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ROUTE_CONTENT: {
                long _id = db.insert(RouteContentEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = RouteContentEntry.buildRouteContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ROUTE_POINT_CONTENT: {
                long _id = db.insert(RoutePointContentEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = RoutePointContentEntry.buildRoutePointContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ROUTE_POINT: {
                long _id = db.insert(RoutePointEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = RoutePointEntry.buildRoutePointUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case J_POIS_ROUTE: {
                long _id = db.insert(JoinPOIsRouteEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = JoinPOIsRouteEntry.buildJRoutePOIUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case J_CONTENTS_ROUTE: {
                long _id = db.insert(JoinContentsRouteEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = JoinContentsRouteEntry.buildRouteContentsWithRouteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case J_POIS_ROUTE_POINT: {
                long _id = db.insert(JoinPOIsRoutePointEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = JoinPOIsRoutePointEntry.buildJPOIsRoutePointsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case J_CONTENTS_ROUTE_POINT: {
                long _id = db.insert(JoinContentsRoutePointEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = JoinContentsRoutePointEntry.buildJContentsRoutepointUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case J_POI_CONTENTS_POI: {
                long _id = db.insert(JoinPOIContentsPOIEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = JoinPOIContentsPOIEntry.buildJContentsPOIUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case QUIZ: {
                long _id = db.insert(QuizEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(QuizEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case QUIZ_CONTENT: {
                long _id = db.insert(QuizContentsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(QuizContentsEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case QUESTION: {
                long _id = db.insert(QuestionEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(QuestionEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ANSWER: {
                long _id = db.insert(AnswerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ContentUris.withAppendedId(AnswerEntry.CONTENT_URI, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (sUriMatcher.match(uri)) {
            case LANGUAGE:
                count = databaseHelper.getWritableDatabase().delete(LanguageEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case J_QUIZ_CONTENTS_QUIZ:
                count = databaseHelper.getWritableDatabase().delete(JoinQuizContentsQuiz.TABLE_NAME, selection, selectionArgs);
                break;
            case J_QUESTIONS_QUIZ_CONTENT:
                count = databaseHelper.getWritableDatabase().delete(JoinQuestionsQuizContent.TABLE_NAME, selection, selectionArgs);
                break;
            case J_ANSWERS_QUESTION:
                count = databaseHelper.getWritableDatabase().delete(JoinAnswersQuestion.TABLE_NAME, selection, selectionArgs);
                break;
            case POI_CONNECTION:
                count = databaseHelper.getWritableDatabase().delete(POIConnectionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_PARSE_ID:
                count = databaseHelper.getWritableDatabase().delete(RouteEntry.TABLE_NAME, ParseColumns.COLUMN_OBJECT_ID + " = ?", new String[]{RouteEntry.getParseId(uri)});
                break;
            case POI_PARSE_ID:
                count = databaseHelper.getWritableDatabase().delete(POIEntry.TABLE_NAME, ParseColumns.COLUMN_OBJECT_ID + " = ?", new String[]{POIEntry.getParseId(uri)});
                break;
            case ROUTE_POINT:
                count = databaseHelper.getWritableDatabase().delete(RoutePointEntry.TABLE_NAME, selection , selectionArgs);
                break;
            case ROUTE_POINT_CONTENT:
                count = databaseHelper.getWritableDatabase().delete(RoutePointContentEntry.TABLE_NAME, selection , selectionArgs);
                break;
            case J_POIS_ROUTE_POINT:
                count = databaseHelper.getWritableDatabase().delete(JoinPOIsRoutePointEntry.TABLE_NAME, selection , selectionArgs);
                break;
            case J_CONTENTS_ROUTE_POINT:
                count = databaseHelper.getWritableDatabase().delete(JoinContentsRoutePointEntry.TABLE_NAME, selection , selectionArgs);
                break;
            case QUIZ_PARSE_ID:
                count = databaseHelper.getWritableDatabase().delete(QuizEntry.TABLE_NAME, ParseColumns.COLUMN_OBJECT_ID + " = ?", new String[]{QuizEntry.getParseId(uri)});
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case INFO:
                rowsUpdated = db.update(InfoEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case LANGUAGE:
                rowsUpdated = db.update(LanguageEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case AVATAR:
                rowsUpdated = db.update(AvatarEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case ICON:
                rowsUpdated = db.update(IconEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case POI:
                rowsUpdated = db.update(POIEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case POI_CONTENT_PARSE_ID:
                rowsUpdated = db.update(POIContentEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{POIContentEntry.getParseId(uri)});
                break;
            case POI_PARSE_ID:
                rowsUpdated = db.update(POIEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{POIEntry.getParseId(uri)});
                break;
            case ROUTE:
                rowsUpdated = db.update(RouteEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case ROUTE_CONTENT:
                rowsUpdated = db.update(RouteContentEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case ROUTE_POINT:
                rowsUpdated = db.update(RoutePointEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case ROUTE_POINT_PARSE_ID:
                rowsUpdated = db.update(RoutePointEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{RoutePointEntry.getObjectId(uri)});
                break;
            case ROUTE_POINT_CONTENT:
                rowsUpdated = db.update(RoutePointContentEntry.TABLE_NAME, values, sParseObjectUpdate, selectionArgs);
                break;
            case ROUTE_POINT_CONTENT_PARSE_ID:
                rowsUpdated = db.update(RoutePointContentEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{RoutePointContentEntry.getObjectId(uri)});
                break;
            case J_POIS_ROUTE_ID: //update with real _id from query
                rowsUpdated = db.update(JoinPOIsRouteEntry.TABLE_NAME, values, BaseColumns._ID + " = '" + ContentUris.parseId(uri) + "'", null);
                break;
            case J_CONTENTS_ROUTE_ID: //update with real _id from query
                rowsUpdated = db.update(JoinContentsRouteEntry.TABLE_NAME, values, BaseColumns._ID + " = '" + ContentUris.parseId(uri) + "'", null);
                break;
            case J_POI_CONTENTS_POI_ID: //update with real _id from query
                rowsUpdated = db.update(JoinPOIContentsPOIEntry.TABLE_NAME, values, BaseColumns._ID + " = '" + ContentUris.parseId(uri) + "'", null);
                break;
            case QUIZ_PARSE_ID:
                rowsUpdated = db.update(QuizEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{uri.getPathSegments().get(2)});
                break;
            case QUIZ_CONTENT_PARSE_ID:
                rowsUpdated = db.update(QuizContentsEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{uri.getPathSegments().get(2)});
                break;
            case QUESTION_PARSE_ID:
                rowsUpdated = db.update(QuestionEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{uri.getPathSegments().get(2)});
                break;
            case ANSWER_PARSE_ID:
                rowsUpdated = db.update(AnswerEntry.TABLE_NAME, values, sParseObjectUpdate, new String[]{uri.getPathSegments().get(2)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case POI_CONTENT:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(POIContentEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case PARSE_COLLECTION:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ParseCollectionsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case LANGUAGE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(LanguageEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case INFO:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(InfoEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case AVATAR:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(AvatarEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case POI:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(POIEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case POI_CONNECTION:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(POIConnectionEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case ROUTE_CONTENT:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RouteContentEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case ICON:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(IconEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case J_POIS_ROUTE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(JoinPOIsRouteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case J_QUIZ_CONTENTS_QUIZ:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(JoinQuizContentsQuiz.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case J_QUESTIONS_QUIZ_CONTENT:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(JoinQuestionsQuizContent.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case J_ANSWERS_QUESTION:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(JoinAnswersQuestion.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor getPOIByRoute(Uri uri, String[] projection, String sortOrder) {
        String route = POIEntry.getRouteFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sRouteSelection;
        selectionArgs = new String[]{route};

        return sPOIByRouteQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getJoinedByConnection(Uri uri, String[] projection, String sortOrder, String tableName) {
        String owning = JoinEntry.getOwningFromUri(uri);
        String related = JoinEntry.getRelatedFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sJoinedSelection;
        selectionArgs = new String[]{owning, related};
        return databaseHelper.getReadableDatabase().query(tableName, projection, selection, selectionArgs, null, null, sortOrder, "1");
    }

    private Cursor getRoutesWithIconAndRoutecontent(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return sRoutesWithIconsAndRouteContentsQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getJoinRoutesWithPOIs(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return sJoinRoutesWithPOIsQueryBuilder.query(databaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public static String getLogTag() {
        return LOG_TAG;
    }
}
