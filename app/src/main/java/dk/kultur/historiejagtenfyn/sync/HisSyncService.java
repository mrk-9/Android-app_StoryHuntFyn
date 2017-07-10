package dk.kultur.historiejagtenfyn.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.parse.ParseException;

/**
 *
 * Created by juskrt on 3/27/15.
 */
public class HisSyncService  extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static HisSyncAdapter sSunshineSyncAdapter = null;
    @Override
    public void onCreate() {
        Log.d("HisSyncService", "onCreate - HisSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new HisSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}