package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.InfoColumns;

/**
 * Info object https://parse.com/apps/historiejagt-fyn/collections#class/Info
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_INFO_COLLECTION)
public class InfoHisContract extends ParseContract {
    private static final String LOG_TAG = InfoHisContract.class.getSimpleName();
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TEXT = "text";

    public String getText() {
        return getString(KEY_TEXT);
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(InfoColumns.COLUMN_LANGUAGE_ID, getParseObject(KEY_LANGUAGE).getObjectId());
        values.put(InfoColumns.COLUMN_TITLE, getString(KEY_TITLE));
        values.put(InfoColumns.COLUMN_TEXT, getString(KEY_TEXT));
    }


    public static bolts.Task<InfoHisContract> getInfoTask(LanguageHisContract parseObject) {
        final ParseQuery<InfoHisContract> infoHisContractParseQuery = new ParseQuery<>(InfoHisContract.class);
        infoHisContractParseQuery.whereEqualTo(KEY_LANGUAGE, parseObject);
        return infoHisContractParseQuery.fromLocalDatastore().getFirstInBackground();
    }

}
