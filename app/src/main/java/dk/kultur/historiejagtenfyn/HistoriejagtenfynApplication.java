package dk.kultur.historiejagtenfyn;

import android.app.Application;
import android.graphics.Typeface;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.parse.Parse;
import dk.kultur.historiejagtenfyn.data.parse.ParserApiHis;
import dk.kultur.historiejagtenfyn.sync.HisSyncAdapter;
import io.fabric.sdk.android.Fabric;
import java.util.HashMap;

/**
 * Main application
 * Created by Lina on 2014.06.27.
 */
public class HistoriejagtenfynApplication extends Application {

    private HashMap<String, Typeface> mTypeFaces;
    private boolean inForeground = true;
    private int resumed = 0;
    private int paused = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);
        } else {
            Parse.setLogLevel(Parse.LOG_LEVEL_ERROR);
            Fabric.with(this, new Crashlytics());
        }
        MultiDex.install(this);
        ParserApiHis.registerParseModels();
        //original parse server
        //Parse.initialize(this, ParserApiHis.PARSE_APPLICATION_KEY, ParserApiHis.PARSE_CLIENT_KEY);
        //new Parse server
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(ParserApiHis.PARSE_APPLICATION_KEY)
                .clientKey(ParserApiHis.PARSE_CLIENT_KEY)
                .server("http://historiejagt.portaplay.dk:1338/parse")
                .build()
        );
        FlurryAgent.init(this, getResources().getString(R.string.flurry_api_key));
        //Starting database sync operation, SyncAdapter assures only one update thread work in app(account)
        HisSyncAdapter.syncImmediately(this);
    }

    public Typeface getTypeface(String name) {
        if (mTypeFaces == null) {
            mTypeFaces = new HashMap<>();
        }

        if (mTypeFaces.containsKey(name)) {
            return mTypeFaces.get(name);
        } else {
            Typeface t = Typeface.createFromAsset(getAssets(), name);
            mTypeFaces.put(name, t);
            return t;
        }
    }

    public void onActivityResumed() {

        ++resumed;

        if (!inForeground) {
            // Don't check for foreground or background right away
            // finishing an activity and starting a new one will trigger to many
            // foreground <---> background switches
            //
            // In half a second call foregroundOrBackground
        }
    }

    public void onActivityPaused() {
        ++paused;

        if (inForeground) {
            // Don't check for foreground or background right away
            // finishing an activity and starting a new one will trigger to many
            // foreground <---> background switches
            //
            // In half a second call foregroundOrBackground
        }
    }

    public boolean isForeground() {
        if (paused >= resumed && inForeground) {
            inForeground = false;
        } else if (resumed > paused && !inForeground) {
            inForeground = true;
        }
        return inForeground;
    }
}
