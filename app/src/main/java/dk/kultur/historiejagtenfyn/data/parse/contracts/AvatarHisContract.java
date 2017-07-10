package dk.kultur.historiejagtenfyn.data.parse.contracts;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseFile;

import java.io.File;

import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;

/**
 * Avatar info
 * https://parse.com/apps/historiejagt-fyn/collections#class/Avatar
 * Created by JustinasK on 12/10/2014.
 */
@ParseClassName(ParserApiHis.KEY_AVATAR_COLLECTION)
public class AvatarHisContract extends ParseContract {
    private static final String LOG_TAG = AvatarHisContract.class.getSimpleName();
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_FILE_IMAGE_1 = "image1";
    public static final String KEY_FILE_IMAGE_2 = "image2";
    public static final String KEY_FILE_IMAGE_3 = "image3";

    public static String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected void fillValues(ContentValues values) {
        values.put(KEY_AVATAR, getString(KEY_AVATAR));
        values.put(KEY_FILE_IMAGE_1 + URL, getString(KEY_FILE_IMAGE_1));
        values.put(KEY_FILE_IMAGE_2 + URL, getString(KEY_FILE_IMAGE_2));
        values.put(KEY_FILE_IMAGE_3 + URL, getString(KEY_FILE_IMAGE_3));
    }


    public void downloadFiles(@NonNull File dir) {
        final ParseFile image1 = getParseFile(AvatarHisContract.KEY_FILE_IMAGE_1);
        FileUtils.saveFileFromBytes(image1, new File(dir, FileUtils.getRelativeName(getObjectId(), FileUtils.AVATAR_1)));

        final ParseFile image2 = getParseFile(AvatarHisContract.KEY_FILE_IMAGE_2);
        FileUtils.saveFileFromBytes(image2, new File(dir, FileUtils.getRelativeName(getObjectId(), FileUtils.AVATAR_2)));

        final ParseFile image3 = getParseFile(AvatarHisContract.KEY_FILE_IMAGE_3);
        FileUtils.saveFileFromBytes(image3, new File(dir, FileUtils.getRelativeName(getObjectId(), FileUtils.AVATAR_3)));
    }
}
