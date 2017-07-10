package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.IconColumns;

/**
 * Icon info
 * https://parse.com/apps/historiejagt-fyn/collections#class/Icon
 * Created by JustinasK on 2/6/2015.
 */
@ParseClassName(ParserApiHis.KEY_ICON_COLLECTION)
public class IconHisContract extends ParseContract {
    private static final String LOG_TAG = IconHisContract.class.getSimpleName();

    //FILE
    public static final String KEY_AR_PIN = "arPin";
    public static final String KEY_AR_PIN_INACTIVE = "arPinInactive";
    public static final String KEY_AR_PIN_INACTIVE_RETINA = "arPinInactiveRetina";
    public static final String KEY_AR_PIN_RETINA = "arPinRetina";
    public static final String KEY_ICON = "icon";
    public static final String KEY_ICON_RETINA = "iconRetina";
    public static final String KEY_PIN = "pin";
    public static final String KEY_PIN_INACTIVE = "pinInactive";
    public static final String KEY_PIN_INACTIVE_RETINA = "pinInactiveRetina";
    public static final String KEY_PIN_RETINA = "pinRetina";

    public static final String KEY_NAME = "name";
    public static final String KEY_ICON_ID = "iconId";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(IconColumns.COLUMN_AR_PIN_URL, getParseFile(KEY_AR_PIN) == null ? null : getParseFile(KEY_AR_PIN).getUrl());
        values.put(IconColumns.COLUMN_AR_PIN_INACTIVE_URL, getParseFile(KEY_AR_PIN_INACTIVE) == null ? null : getParseFile(KEY_AR_PIN_INACTIVE).getUrl());
        values.put(IconColumns.COLUMN_AR_PIN_INACTIVE_RETINA_URL, getParseFile(KEY_AR_PIN_INACTIVE_RETINA) == null ? null : getParseFile(KEY_AR_PIN_INACTIVE_RETINA).getUrl());
        values.put(IconColumns.COLUMN_AR_PIN_RETINA_URL, getParseFile(KEY_AR_PIN_RETINA) == null ? null : getParseFile(KEY_AR_PIN_RETINA).getUrl());
        values.put(IconColumns.COLUMN_ICON_URL, getParseFile(KEY_ICON) == null ? null : getParseFile(KEY_ICON).getUrl());
        values.put(IconColumns.COLUMN_ICON_RETINA_URL, getParseFile(KEY_ICON_RETINA) == null ? null : getParseFile(KEY_ICON_RETINA).getUrl());
        values.put(IconColumns.COLUMN_PIN_URL, getParseFile(KEY_PIN) == null ? null : getParseFile(KEY_PIN).getUrl());
        values.put(IconColumns.COLUMN_PIN_INACTIVE_URL, getParseFile(KEY_PIN_INACTIVE) == null ? null : getParseFile(KEY_PIN_INACTIVE).getUrl());
        values.put(IconColumns.COLUMN_PIN_INACTIVE_RETINA_URL, getParseFile(KEY_PIN_INACTIVE_RETINA) == null ? null : getParseFile(KEY_PIN_INACTIVE_RETINA).getUrl());
        values.put(IconColumns.COLUMN_PIN_RETINA_URL, getParseFile(KEY_PIN_RETINA) == null ? null : getParseFile(KEY_PIN_RETINA).getUrl());
        values.put(IconColumns.COLUMN_NAME, getString(KEY_NAME));
        values.put(IconColumns.COLUMN_ICON_ID, getString(KEY_ICON_ID));
    }

    public ParseFile getIcon() {
        return getParseFile(KEY_ICON);
    }
}
