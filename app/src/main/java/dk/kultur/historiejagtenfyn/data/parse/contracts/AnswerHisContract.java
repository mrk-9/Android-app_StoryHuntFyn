package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.AnswerColumns;

/**
 * Created by JustinasK on 2/20/2015.
 */
@ParseClassName(ParserApiHis.KEY_ANSWER_COLLECTION)
public class AnswerHisContract extends ParseContract {
    private static final String LOG_TAG = AnswerHisContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final String KEY_ANSWER = "answer";
    public static final String KEY_CORRECT = "correct";

    @Override
    protected void fillValues(ContentValues values) {
        values.put(AnswerColumns.COLUMN_ANSWER, getString(KEY_ANSWER));
        values.put(AnswerColumns.COLUMN_CORRECT, getBoolean(KEY_CORRECT));
    }
}
