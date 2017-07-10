package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.QuestionColumns;

/**
 * Parse Question Contract
 * Created by JustinasK on 2/20/2015.
 */
@ParseClassName(ParserApiHis.KEY_QUESTION_COLLECTION)
public class QuestionHisContract extends ParseContract {
    private static final String LOG_TAG = QuestionHisContract.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    /**
     * Relation<Answer>
     */
    public static final String RELATION_ANSWERS = "answers";

    /**
     * Question text
     */
    public static final String KEY_QUESTION = "question";


    @Override
    protected void fillValues(ContentValues values) {
        values.put(QuestionColumns.COLUMN_QUESTION, getString(KEY_QUESTION));
    }
}
