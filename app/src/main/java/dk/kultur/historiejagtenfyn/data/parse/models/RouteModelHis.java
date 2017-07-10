package dk.kultur.historiejagtenfyn.data.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName("Route")
public class RouteModelHis extends ParseObject {
    private static final String LOG_TAG = RouteModelHis.class.getSimpleName();
    public static final String KEY_NAME = "name";

    public static String getLogTag() {
        return LOG_TAG;
    }
}
