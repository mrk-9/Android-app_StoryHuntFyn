package dk.kultur.historiejagtenfyn.ui.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.Helpers;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.channels.FileChannel;

public class FileUtils {
    private static final String LOG_TAG = FileUtils.class.getSimpleName();

    public static String getLogTag() {
        return LOG_TAG;
    }

    //expansion file version. this should match versionCode in gradle
    public static int EXPANSION_MAIN_VERSION = 12;

    public static long EXPANSION_FILE_SIZE = 41271192L;

    private static final String ASSET_URL_FOR_WEBVIEW = "file:///android_asset/";
    private static final String ASSET_URL_FOR_AR = "assets://";
    private static final String DATABASE_DIR = "databasefiles";
    private static final String DATABASE_FILES = DATABASE_DIR + File.separator;

    public static String getFileUrl(Context context, String ObjectId, @MediaType String type) {
        return ASSET_URL_FOR_WEBVIEW + DATABASE_FILES + ObjectId + type;
    }

    public static String getFileUrlForAR(Context context, String ObjectId, @MediaType String type) {
        return ASSET_URL_FOR_AR + DATABASE_FILES + ObjectId + type;
    }

    public static String getIconPathForAR(String ObjectId, @MediaType String type) {
        return ObjectId + type;
    }


    /**
     * @param context  app
     * @param objectId parse object
     * @param type     media type
     * @return inputStream
     * @throws IOException if file not found
     */
    public static InputStream getFileStream(Context context, String objectId, @MediaType String type) throws IOException {
        return getFileStream(context, objectId + type);
    }

    /**
     * @param context  app
     * @param path objectId + type
     * @return inputStream
     * @throws IOException if file not found
     */
    public static InputStream getFileStream(Context context, String path) throws IOException {
        InputStream is = null;
        Log.d(LOG_TAG, "get file " + DATABASE_FILES + path);
        //first look for file in external storage
        File file = new File(context.getExternalFilesDir(DATABASE_DIR), path);
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "Not found in ssd", e);
            }
        }
        if (is == null) {
            if (expansionFilesDelivered(context)) {
                ZipResourceFile expansionFile = null;
                try {
                    expansionFile = APKExpansionSupport.getAPKExpansionZipFile(context, EXPANSION_MAIN_VERSION, 0);
                    if (expansionFile != null) {
                        is = expansionFile.getInputStream(DATABASE_FILES + path);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Not found in obb", e);
                }
            } else {
                is = context.getAssets().open(DATABASE_FILES + path);
            }
        }
        Log.d(LOG_TAG, "input stream " + is);
        return is;
    }

    /**
     * @param objectId parse object
     * @param type     media type
     * @return relative path
     */
    public static String getRelativeName(String objectId, @MediaType String type) {
        return objectId + type;
    }


    public static void checkAppDir(Context context) throws FileNotFoundException {
        File sdDir = Environment.getExternalStorageDirectory();
        String packageName = context.getPackageName();
        File dir = new File(sdDir, "/Android/data/" + packageName + "/" + DATABASE_DIR);

        File appDir = context.getExternalFilesDir(DATABASE_DIR);
        if (appDir == null) {
            throw new FileNotFoundException("external storage not found");
        }
        createDirectory(dir);
    }

    public static void createDirectory(File dir) throws IllegalStateException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException(
                    "Check if you've added permissions in AndroidManifest.xml: \n" +
                            "<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"/> \n"
            );
        }
    }

    public static FileDescriptor getAudioFileDescriptor(Context context, String ObjectId, @MediaType String type) throws IOException {
        AssetFileDescriptor descriptor = context.getAssets().openFd(DATABASE_FILES + ObjectId + type);
        return descriptor.getFileDescriptor();

    }

    public static void copyFilesFromAssetsToSd(Context context, String assetFile) {
        AssetManager am = context.getAssets();
        AssetFileDescriptor afd = null;
        try {
            afd = am.openFd(assetFile);

            // Create new file to copy into.
            File file = new File(Environment.getExternalStorageDirectory() + java.io.File.separator + assetFile);
            file.createNewFile();

            copyFdToFile(afd.getFileDescriptor(), file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets and closes the data source
     *
     * @param player media player
     * @param fd     file descriptor
     * @throws IOException
     */
    public static void setDataSource(@NonNull MediaPlayer player, AssetFileDescriptor fd) throws IOException {
        player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
        fd.close();
    }

    /**
     * Returns file descriptor from assets
     *
     * @param mediaType media type
     * @param objectId  parse object id
     * @param context   app
     * @return file descriptor
     * @throws IOException
     */
    public static
    @Nullable
    AssetFileDescriptor getMediaFileDescriptor(@PlayerType String mediaType, String objectId, Context context) {
        AssetFileDescriptor fd = null;
        if (expansionFilesDelivered(context)) {
            switch (mediaType) {
                case AUDIO:
                    ZipResourceFile expansionFile = null;
                    try {
                        expansionFile = APKExpansionSupport.getAPKExpansionZipFile(context, EXPANSION_MAIN_VERSION, 0);
                        fd = expansionFile.getAssetFileDescriptor(DATABASE_FILES + objectId + mediaType);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Uncaught exception", e);
                    }
                    break;
            }
        }
        if (fd == null) {
            File file = new File(context.getExternalFilesDir(DATABASE_DIR), objectId + mediaType);
            fd = openFile(file);
        }
        if (fd == null) {
            try {
                fd = context.getAssets().openFd(DATABASE_FILES + objectId + mediaType);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Uncaught exception", e);
            }
        }
        return fd;
    }

    public static
    @Nullable
    InputStream getWebResource(String fileName, Context context) {
        InputStream is = null;
        if (expansionFilesDelivered(context)) {
            ZipResourceFile expansionFile = null;
            try {
                expansionFile = APKExpansionSupport.getAPKExpansionZipFile(context, EXPANSION_MAIN_VERSION, 0);
                if (expansionFile != null){
                    is = expansionFile.getInputStream(DATABASE_FILES + fileName);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Not found in obb", e);
            }
        }
        if (is == null) {
            File file = new File(context.getExternalFilesDir(DATABASE_DIR), fileName);
            if (file.exists()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    Log.e(LOG_TAG, "Not found in ssd", e);
                }
            }
        }
        if (is == null) {
            try {
                is = context.getAssets().open(DATABASE_FILES + fileName);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Not found in assets", e);
            }
        }
        return is;
    }

    /**
     * @param uri
     * @param context
     * @return
     */
    public static AssetFileDescriptor openFile(Uri uri, Context context) {
        File f = new File(context.getFilesDir(), uri.getLastPathSegment());
        AssetFileDescriptor afd;
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            afd = new AssetFileDescriptor(pfd, 0, f.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return afd;
    }

    /**
     * @param file
     * @return
     */
    public static AssetFileDescriptor openFile(File file) {
        AssetFileDescriptor afd;
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            afd = new AssetFileDescriptor(pfd, 0, file.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return afd;
    }

    public static File getExternalMediaDirFile(Context context) {
        return context.getExternalFilesDir(DATABASE_DIR);
    }

    public static void saveFileFromBytes(ParseFile parseFile, File toSave) {
//        if (parseFile != null && !toSave.exists()) {
        if (parseFile != null) {
            try {
                final byte[] data = parseFile.getData();
                if (data != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(toSave);
                    fileOutputStream.write(data);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Uncaught ParseException", e);
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "Uncaught FileNotFoundException", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Uncaught IOException", e);
                toSave.delete();
            }
        } else {
            Log.e(LOG_TAG, "parse file not found" + toSave.toString());
        }
    }


    /**
     * This is a little helper class that demonstrates simple testing of an
     * Expansion APK file delivered by Market. You may not wish to hard-code
     * things such as file lengths into your executable... and you may wish to
     * turn this code off during application development.
     */
    private static class XAPKFile {
        public final boolean mIsMain;
        public final int mFileVersion;
        public final long mFileSize;

        XAPKFile(boolean isMain, int fileVersion, long fileSize) {
            mIsMain = isMain;
            mFileVersion = fileVersion;
            mFileSize = fileSize;
        }
    }

    /**
     * Here is where you place the data that the validator will use to determine
     * if the file was delivered correctly. This is encoded in the source code
     * so the application can easily determine whether the file has been
     * properly delivered without having to talk to the server. If the
     * application is using LVL for licensing, it may make sense to eliminate
     * these checks and to just rely on the server.
     */
    private static final XAPKFile[] xAPKS = {
            new XAPKFile(
                    true, // true signifies a main file
                    EXPANSION_MAIN_VERSION, // the version of the APK that the file was uploaded against
                    EXPANSION_FILE_SIZE // the length of the file in bytes

            )
    };

    /**
     * Go through each of the APK Expansion files defined in the structure above
     * and determine if the files are present and match the required size. Free
     * applications should definitely consider doing this, as this allows the
     * application to be launched for the first time without having a network
     * connection present. Paid applications that use LVL should probably do at
     * least one LVL check that requires the network to be present, so this is
     * not as necessary.
     *
     * @return true if they are present.
     */
    public static boolean expansionFilesDelivered(Context context) {
        for (XAPKFile xf : xAPKS) {
            String fileName = Helpers.getExpansionAPKFileName(context, xf.mIsMain, xf.mFileVersion);
            Log.d(LOG_TAG, "XAPKFile name : " + fileName);
            if (!Helpers.doesFileExist(context, fileName, xf.mFileSize, false)) {
                Log.e(LOG_TAG, "ExpansionAPKFile doesn't exist or has a wrong size (" + fileName + ").");
                return false;
            }
        }
        return true;
    }

    /**
     * Copy file to destination
     *
     * @param src file
     * @param dst file
     * @throws IOException
     */
    public static void copyFdToFile(FileDescriptor src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }


    @StringDef({IMAGE, IMAGE_LARGE, AUDIO, ICON_RETINA, AVATAR_1, AVATAR_2, AVATAR_3, ICON_AR_ACTIVE_RETINA, ICON_AR_INACTIVE_RETINA,
            ICON_PIN_ACTIVE_RETINA, ICON_PIN_INACTIVE_RETINA, FACTS_IMAGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaType {
    }

    @StringDef({AUDIO})
    public @interface PlayerType {
    }

    public static final String IMAGE = "_image";
    public static final String IMAGE_LARGE = "_large";
    public static final String AUDIO = "_audio.aac";
    public static final String ICON_RETINA = "_icon_retina.png";
    public static final String AVATAR_1 = "_avatar_1.png";
    public static final String AVATAR_2 = "_avatar_2.png";
    public static final String AVATAR_3 = "_avatar_3.png";
    public static final String ICON_AR_ACTIVE_RETINA = "_ar_active_retina.png";
    public static final String ICON_AR_INACTIVE_RETINA = "_ar_inactive_retina.png";
    public static final String ICON_PIN_ACTIVE_RETINA = "_active_retina.png";
    public static final String ICON_PIN_INACTIVE_RETINA = "_inactive_retina.png";
    public static final String FACTS_IMAGE = "_facts__image";

}
