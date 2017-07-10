package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.data.sql.Columns.ImageColumns;

/**
 * Image info
 * https://parse.com/apps/historiejagt-fyn/collections#class/Image
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_IMAGE_COLLECTION)
public class ImageHisContract extends ParseContract {
    private static final String LOG_TAG = ImageHisContract.class.getSimpleName();
    public static final String CROPPED = "cropped";
    public static final String IMAGE = "image";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(ImageColumns.COLUMN_CROPPED_URL, getParseFile(CROPPED) == null ? null : getParseFile(CROPPED).getUrl());
        values.put(ImageColumns.COLUMN_IMAGE_URL, getParseFile(IMAGE) == null ? null : getParseFile(IMAGE).getUrl());
    }

    public ParseFile getCropped() {return getParseFile(CROPPED);}
    public ParseFile getImage() {return getParseFile(IMAGE);}
}
