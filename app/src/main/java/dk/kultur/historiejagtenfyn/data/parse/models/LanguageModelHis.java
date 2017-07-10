package dk.kultur.historiejagtenfyn.data.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseRelation;

/**
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName("Language")
public class LanguageModelHis extends ParseObject {
    private static final String LOG_TAG = LanguageModelHis.class.getSimpleName();
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_PRIORITY = "priority";

    public static String getLogTag() {
        return LOG_TAG;
    }

    public String getCode() {
        return getString("code");
    }

    public int getPriority() {
        return getInt(KEY_PRIORITY);
    }

    public boolean getActive() {
        return getBoolean(KEY_ACTIVE);
    }
}
