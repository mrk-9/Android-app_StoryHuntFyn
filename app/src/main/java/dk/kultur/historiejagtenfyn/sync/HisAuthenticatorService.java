package dk.kultur.historiejagtenfyn.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 *
 * Created by juskrt on 3/27/15.
 */
public class HisAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private HisAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
// Create a new authenticator object
        mAuthenticator = new HisAuthenticator(this);
    }
    /*
    * When the system binds to this Service to make the RPC call
    * return the authenticator's IBinder.
    */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}