package dk.kultur.historiejagtenfyn.data.parse.entries;

import com.parse.ParseObject;

/**
 * Created by JustinasK on 12/11/2014.
 */
public class RouteEntry {
    private static final String LOG_TAG = RouteEntry.class.getSimpleName();

    public RouteEntry(ParseObject language) {
    }

    private static final String KEY_KANGUAGE_CODE = "languageCode";
    private static final String KEY_CONTENTUPDATE_AT = "contentUpdatedAt";
    private static final String KEY_UPDATED_AT = "updatedAt";
    private static final String KEY_NAMES = "names";
    private static final String KEY_INFOS = "infos";
    private static final String KEY_POINT_OF_INTEREST_IDS = "pointOfInterestIds";
    private static final String KEY_ICON_RETINA = "iconRetina";
    private static final String KEY_PIN_RETINA = "pinRetina";
    private static final String KEY_PIN_INACTIVE_RETINA = "pinInactiveRetina";
    private static final String KEY_ICON_NON_RETINA = "iconNonRetina";
    private static final String KEY_PIN_NON_RETINA = "pinNonRetina";
    private static final String KEY_PIN_INACTIVE_NON_RETINA = "pinInactiveNonRetina";

    private static final String KEY_AR_PIN_RETINA = "arPinRetina";
    private static final String KEY_AR_PIN_INACTIVE_RETINA = "arPinInactiveRetina";
    private static final String KEY_AR_PIN_NON_RETINA = "arPinNonRetina";
    private static final String KEY_AR_PIN_INACTIVE_NON_RETINA = "arPinInactiveNonRetina";

    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_AVATAR_ID = "avatarId";

    public static String getLogTag() {
        return LOG_TAG;
    }
}
