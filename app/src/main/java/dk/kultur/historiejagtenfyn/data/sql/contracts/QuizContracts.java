package dk.kultur.historiejagtenfyn.data.sql.contracts;

import android.net.Uri;
import android.provider.BaseColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.*;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;

/**
 * Created by JustinasK on 2/24/2015.
 */
public class QuizContracts {
    private static final String LOG_TAG = QuizContracts.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final class QuizEntry implements BaseColumns, ParseColumns, QuizColumns {
        // Table name
        public static final String TABLE_NAME = "Quiz";

        public static final Uri CONTENT_URI =
                HisContract.BASE_CONTENT_URI_CLASS.buildUpon().appendPath(HisContract.PATH_QUIZ).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + HisContract.CONTENT_AUTHORITY + "/" + HisContract.PATH_QUIZ;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + HisContract.CONTENT_AUTHORITY + "/" + HisContract.PATH_QUIZ;

        public static String getParseIdUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildWithParseId(String parseId) {
            return CONTENT_URI.buildUpon().appendPath(parseId).build();
        }

        public static String getParseId(Uri uri){
           return uri.getPathSegments().get(2);
        }

        public static final class QuizViewEntry implements BaseColumns, ParseColumns, QuizColumns, QuizContentColumns, QuestionColumns, AnswerColumns {
            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_FULL_VIEW.buildUpon().appendPath(HisContract.PATH_QUIZ).build();
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + HisContract.CONTENT_AUTHORITY + "/" + HisContract.PATH_QUIZ;

            public static String getParseIdUri(Uri uri) {
                return uri.getPathSegments().get(2);
            }

            public static Uri buildWithParseId(String parseId) {
                return CONTENT_URI.buildUpon().appendPath(parseId).build();
            }
        }

        /**
         * Class just for bulk insert
         */
        public static class JoinQuestionsQuizContent implements JoinColumns {

            public static final String TABLE_NAME = "_Join_questions_QuizContent";

            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + HisContract.CONTENT_AUTHORITY + "/" + HisContract.PATH_RELATION +
                    "/ " + HisContract.PATH_QUIZ;

            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_RELATION.buildUpon().appendPath(HisContract.PATH_QUIZ_CONTENT).build();

            public static Uri buildRelation(String ownerId) {
                return CONTENT_URI.buildUpon().appendPath(ownerId).appendPath(HisContract.PATH_QUESTIONS).build();
            }

            public static Uri buildRelation() {
                return buildRelation(HisContract.PATH_JOIN);
            }
        }

        public static class JoinAnswersQuestion implements JoinColumns {

            public static final String TABLE_NAME = "_Join_answers_Question";

            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + HisContract.CONTENT_AUTHORITY + "/" + HisContract.PATH_RELATION + "/ " + HisContract.PATH_QUESTION;

            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_RELATION.buildUpon().appendPath(HisContract.PATH_QUESTION).build();

            public static Uri buildRelation(String ownerId) {
                return CONTENT_URI.buildUpon().appendPath(ownerId).appendPath(HisContract.PATH_ANSWERS).build();
            }

            public static Uri buildRelation() {
                return buildRelation(HisContract.PATH_JOIN);
            }
        }

        public static class JoinQuizContentsQuiz implements JoinColumns {
            public static final String TABLE_NAME = "_Join_contents_Quiz";

            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + HisContract.CONTENT_AUTHORITY + "/" + HisContract.PATH_RELATION +
                    "/ " + HisContract.PATH_QUIZ_CONTENT;

            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_RELATION.buildUpon().appendPath(HisContract.PATH_QUIZ).build();

            public static Uri buildRelation(String ownerId) {
                return CONTENT_URI.buildUpon().appendPath(ownerId).appendPath(HisContract.PATH_QUIZ_CONTENTS).build();
            }

            public static Uri buildRelation() {
                return buildRelation(HisContract.PATH_JOIN);
            }
        }

        public static class QuizContentsEntry implements BaseColumns, ParseColumns, QuizColumns.QuizContentColumns {
            public static final String TABLE_NAME = "QuizContent";

            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_CLASS.buildUpon().appendPath(HisContract.PATH_QUIZ_CONTENT).build();

            public static String getParseIdUri(Uri uri) {
                return uri.getPathSegments().get(2);
            }

            public static Uri buildWithParseId(String parseId) {
                return CONTENT_URI.buildUpon().appendPath(parseId).build();
            }
        }

        public static class QuestionEntry implements BaseColumns, ParseColumns, QuestionColumns {
            public static final String TABLE_NAME = "Question";

            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_CLASS.buildUpon().appendPath(HisContract.PATH_QUESTION).build();

            public static String getParseIdUri(Uri uri) {
                return uri.getPathSegments().get(2);
            }

            public static Uri buildWithParseId(String parseId) {
                return CONTENT_URI.buildUpon().appendPath(parseId).build();
            }
        }

        public static class AnswerEntry implements BaseColumns, ParseColumns, AnswerColumns {

            public static final Uri CONTENT_URI = HisContract.BASE_CONTENT_URI_CLASS.buildUpon().appendPath(HisContract.PATH_ANSWER).build();

            public static final String TABLE_NAME = "Answer";

            public static String getParseIdUri(Uri uri) {
                return uri.getPathSegments().get(2);
            }

            public static Uri buildWithParseId(String parseId) {
                return CONTENT_URI.buildUpon().appendPath(parseId).build();
            }
        }
    }
}
