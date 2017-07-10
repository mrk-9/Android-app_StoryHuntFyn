package dk.kultur.historiejagtenfyn.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import dk.kultur.historiejagtenfyn.data.entities.Language;
import dk.kultur.historiejagtenfyn.data.entities.Route;
import dk.kultur.historiejagtenfyn.data.entities.RouteContent;

/**
 * Created by Lina on 2014.06.27.
 */
public class DataBaseOpener_ extends OrmLiteSqliteOpenHelper {

    public final static String DATABASE_NAME = "Database.db";
    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_PATH = "/data/data/dk.kultur.historiejagtenfyn/databases/";

    private Dao<Language, Integer> mLanguageDao;
    private Dao<Route, Integer> mRouteDao;
    private Dao<RouteContent, Integer> mRouteContentDao;

    private Context mContext;


    public DataBaseOpener_(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;

        // copy db from existing file
        /*if (!checkDataBase()) {
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                db.close();
            }

        }*/

    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        boolean exist = false;
        try {
            String dbPath = DATABASE_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
            if (checkDB != null) {
                exist = true;
                checkDB.close();
            }
        } catch (SQLiteException e) {
            Log.v("db log", "database doesn't exist");
        } finally {
            if (checkDB != null) {
                checkDB.close();
            }
        }

        return exist;
    }

    // copy db from assets
    private void copyDataBase() throws IOException {

        int bufferSize = 32 * 1024; // 32 KB

        BufferedInputStream bufferedInput = new BufferedInputStream(mContext.getAssets().open(DATABASE_NAME), bufferSize);
        String outFileName = DATABASE_PATH + DATABASE_NAME;

        BufferedOutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(outFileName), bufferSize);

        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = bufferedInput.read(buffer)) > 0) {
            bufferedOut.write(buffer, 0, length);
        }
        bufferedOut.flush();
        bufferedOut.close();

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {

        try {
            TableUtils.createTable(connectionSource, Language.class);
            TableUtils.createTable(connectionSource, Route.class);
            TableUtils.createTable(connectionSource, RouteContent.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {

    }

    public Dao<Language, Integer> getLanguageDao() throws SQLException {
        if (mLanguageDao == null) {
            mLanguageDao = getDao(Language.class);
        }
        return mLanguageDao;
    }

    public Dao<Route, Integer> getRouteDao() throws SQLException {
        if (mRouteDao == null) {
            mRouteDao = getDao(Route.class);
        }
        return mRouteDao;
    }

    public Dao<RouteContent, Integer> getRouteContentDao() throws SQLException {
        if (mRouteContentDao == null) {
            mRouteContentDao = getDao(RouteContent.class);
        }
        return mRouteContentDao;
    }
}
