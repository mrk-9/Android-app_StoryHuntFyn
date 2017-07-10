package dk.kultur.historiejagtenfyn.data.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName("Info")
public class InfoModelHis extends ParseObject {
    private static final String LOG_TAG = InfoModelHis.class.getSimpleName();

    public String getText() {
        return getString("text");
    }

    public String getTitle() {
        return getString("title");
    }

    public static String getLogTag() {
        return LOG_TAG;
    }


}
