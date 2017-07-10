package dk.kultur.historiejagtenfyn.data.parse.entries;

import com.parse.ParseObject;

/**
 * Created by JustinasK on 12/11/2014.
 */
public class LanguageEntry {

    private static final String LOG_TAG = LanguageEntry.class.getSimpleName();
    private final String objectId;
    private final String code;
    private final String priority;
    private final String updatedAt;

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static final String KEY_CODE = "code";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_UPDATED_AT = "updatedAt";
    public static final String KEY_ACTIVE = "KEY_ACTIVE";

    public LanguageEntry(ParseObject his) {
        objectId = his.getObjectId();
        code = his.getString(KEY_CODE);
        priority = his.getString(KEY_PRIORITY);
        updatedAt = his.getString(KEY_UPDATED_AT);
    }

    public String getObjectId() {
        return objectId;
    }

    public String getCode() {
        return code;
    }

    public String getPriority() {
        return priority;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
