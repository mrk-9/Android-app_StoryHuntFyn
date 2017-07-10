package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.QuizColumns.QuizContentColumns;

/**
 *
 * Created by JustinasK on 2/20/2015.
 */
@ParseClassName(ParserApiHis.KEY_QUIZ_CONTENT_COLLECTION)
public class QuizContentHisContract extends ParseContract {
    private static final String LOG_TAG = QuizContentHisContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final String KEY_HEADER = "header";
    public static final String KEY_POINTER_LANGUAGE = "language";
    public static final String KEY_RELATION_QUESTION = "questions";

    @Override
    protected void fillValues(ContentValues values) {
        values.put(QuizContentColumns.COLUMN_HEADER, getString(KEY_HEADER));
        values.put(QuizContentColumns.COLUMN_LANGUAGE_ID, getParseObject(KEY_POINTER_LANGUAGE) == null ? null : getParseObject(KEY_POINTER_LANGUAGE).getObjectId());
    }
}
