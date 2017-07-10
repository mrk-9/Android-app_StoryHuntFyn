package dk.kultur.historiejagtenfyn.data.repositories;

import android.content.Context;
import android.util.Log;
import com.j256.ormlite.dao.Dao;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.data.entities.Language;
import dk.kultur.historiejagtenfyn.data.parse.models.InfoModelHis;
import dk.kultur.historiejagtenfyn.data.parse.models.LanguageModelHis;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lina on 2014.06.27.
 */
public class LanguageRepository extends AbsRepository<Language> {

    private static final String LOG_TAG = LanguageRepository.class.getSimpleName();

    public LanguageRepository(Context cnt) {
        super(cnt);
    }



    @Override
    protected Dao<Language, Integer> getEntityDao() throws SQLException {
        return mDbOpener.getLanguageDao();
    }

    public String getCurrentLanguageObjectId() {
        String objectId = getCurrentLanguage().getObjectId();
        return objectId;
    }

    public ParseObject getCurrentLanguage() {
        LanguageModelHis currentLanguage = null;
        String code = Locale.getDefault().getLanguage().toUpperCase();
        List<LanguageModelHis> activeSortedList = selectActiveSorted();
        for (LanguageModelHis language : activeSortedList) {
            if (code.equals(language.getCode())) {
                currentLanguage = language; break;
            }
        }
        //if there is no match simply take the first one
        if (currentLanguage == null && activeSortedList.size() > 0) {
            currentLanguage = activeSortedList.get(0);
        }

        return getInfo(currentLanguage);
    }

    public List<LanguageModelHis> selectActiveSorted() {
        ParseQuery<LanguageModelHis> query = ParseQuery.getQuery(LanguageModelHis.class);
//        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo(LanguageModelHis.KEY_ACTIVE, true);
        query.orderByAscending(LanguageModelHis.KEY_PRIORITY);
        final String TOP = "activeLanguage";
        List<LanguageModelHis> languageModel = null;

        try {
            languageModel = query.find();
            //saving data to offline mode
            ParseObject.unpinAll(TOP);
            ParseObject.pinAll(TOP, languageModel);

        } catch (ParseException e) {
            Log.e(getLogTag(), "Uncaught exception", e);
        }
        return languageModel;
    }

    public InfoModelHis getInfo(ParseObject languageKey) {
        ParseQuery<InfoModelHis> query = ParseQuery.getQuery(InfoModelHis.class);
//        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.whereEqualTo("language", languageKey);

        final String INFO_TEXT = "infoText";
        InfoModelHis languageModel = null;

        try {
            languageModel = query.getFirst();;
            InfoModelHis.unpinAll(INFO_TEXT);
            languageModel.pin(INFO_TEXT);
        } catch (ParseException e) {
            Log.e(getLogTag(), "Uncaught exception", e);
        }


        return languageModel;
    }

    public static String getLogTag() {
        return LOG_TAG;
    }
}
