package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.QuizColumns;

/**
 * parse from https://parse.com/apps/historiejagt-fyn/collections#class/Quiz
 * Created by JustinasK on 2/20/2015.
 */
@ParseClassName(ParserApiHis.KEY_QUIZ_COLLECTION)
public class QuizHisContract  extends ParseContract{
    private static final String LOG_TAG = QuizHisContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final String KEY_NAME = "name";
    public static final String KEY_RELATION_CONTENT = "contents";

    @Override
    protected void fillValues(ContentValues values) {
        values.put(QuizColumns.COLUMN_NAME, getString(KEY_NAME));
    }
}
