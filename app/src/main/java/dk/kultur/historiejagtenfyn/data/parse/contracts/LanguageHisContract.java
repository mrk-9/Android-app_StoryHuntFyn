package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.sync.StartUpService;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.LanguageColumns;
import java.util.List;
import java.util.Locale;

/**
 * Language contract https://parse.com/apps/historiejagt-fyn/collections#class/Language
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_LANGUAGE_COLLECTION)
public class LanguageHisContract extends ParseContract {
    private static final String LOG_TAG = LanguageHisContract.class.getSimpleName();
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_CODE = "code";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_PRIORITY_LIST = "priority_list";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(LanguageColumns.COLUMN_ACTIVE, getBoolean(KEY_ACTIVE));
        values.put(LanguageColumns.COLUMN_CODE, getString(KEY_CODE));
        values.put(LanguageColumns.COLUMN_LANGUAGE, getString(KEY_LANGUAGE));
        values.put(LanguageColumns.COLUMN_PRIORITY, getInt(KEY_PRIORITY));
    }

    public String getCode() {
        return getString(KEY_CODE);
    }

    /**
     * @return user language by locale and database match
     */
    public static LanguageHisContract getActiveLanguageId() throws ParseException {
        ParseQuery<LanguageHisContract> query = ParseQuery.getQuery(LanguageHisContract.class);
        query.whereEqualTo(LanguageHisContract.KEY_ACTIVE, true);
        query.orderByAscending(LanguageHisContract.KEY_PRIORITY);
        final String code = Locale.getDefault().getLanguage().toUpperCase();
        final List<LanguageHisContract> languageHisContracts = query.find();
        for (LanguageHisContract o : languageHisContracts) {
            if (o.getCode().equals(code)) {
                return o;
            }
        }
        if (languageHisContracts.size() > 0) {
            //if there is no match simply take the first one
            return languageHisContracts.get(0);
        } else throw new RuntimeException("0 size result");
    }

    /**
     * @return user language by locale and database match
     */
    public static LanguageHisContract getFakeActiveLanguage(@NonNull String ObjectId) {
        return ParseObject.createWithoutData(LanguageHisContract.class, ObjectId);
    }

    /**
     * @return user language by locale and database match
     */
    @SuppressWarnings("UnusedDeclaration")
    public static LanguageHisContract getActiveLanguage(@NonNull String objectId) throws ParseException {
        ParseQuery<LanguageHisContract> query = new ParseQuery<>(LanguageHisContract.class);
        query.whereEqualTo("objectId", objectId);
        return query.fromLocalDatastore().getFirst();
    }


    /**
     * @param context app
     * @return user language by locale and database match or null
     */
    @Nullable
    public static LanguageHisContract getSavedActiveLanguage(Context context) throws ParseException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String string = preferences.getString(StartUpService.KEY_ACTIVE_LANGUAGE_ID, null);
        if (string != null) {
            return getFakeActiveLanguage(string);
        }
        return null;
    }

    public static void saveActiveLangaugeId(@NonNull LanguageHisContract contract, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(StartUpService.KEY_ACTIVE_LANGUAGE_ID, contract.getObjectId()).apply();
    }
}
